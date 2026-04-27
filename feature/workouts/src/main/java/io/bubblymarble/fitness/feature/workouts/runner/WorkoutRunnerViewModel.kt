package io.bubblymarble.fitness.feature.workouts.runner

import android.content.Context
import android.content.Intent
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.bubblymarble.fitness.core.common.TimeSource
import io.bubblymarble.fitness.core.data.db.entities.SessionSetEntity
import io.bubblymarble.fitness.core.data.model.StreakState
import io.bubblymarble.fitness.core.data.model.WorkoutTemplate
import io.bubblymarble.fitness.core.data.repository.ExerciseRepository
import io.bubblymarble.fitness.core.data.repository.HealthSampleRepository
import io.bubblymarble.fitness.core.data.repository.SessionRepository
import io.bubblymarble.fitness.core.data.repository.StreakRepository
import io.bubblymarble.fitness.core.data.repository.WorkoutTemplateRepository
import io.bubblymarble.fitness.core.data.streak.StreakEngine
import io.bubblymarble.fitness.core.health.HealthConnectFacade
import io.bubblymarble.fitness.core.notifications.WorkoutForegroundService
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class WorkoutRunnerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    savedState: SavedStateHandle,
    private val sessions: SessionRepository,
    private val templates: WorkoutTemplateRepository,
    private val exercises: ExerciseRepository,
    private val streaks: StreakRepository,
    private val health: HealthConnectFacade,
    private val healthSamples: HealthSampleRepository,
    private val time: TimeSource,
) : ViewModel() {

    private val sessionId: Long = savedState.get<String>("sessionId")?.toLongOrNull() ?: -1L
    private val _state = MutableStateFlow(RunnerState())
    val state: StateFlow<RunnerState> = _state.asStateFlow()
    private var ticker: Job? = null

    init {
        viewModelScope.launch { load() }
    }

    private suspend fun load() {
        val session = sessions.getSession(sessionId) ?: run {
            _state.value = RunnerState(phase = RunnerPhase.ERROR, errorMessage = "Session not found")
            return
        }
        val template: WorkoutTemplate = session.templateId?.let { templates.byId(it) } ?: run {
            _state.value = RunnerState(phase = RunnerPhase.ERROR, errorMessage = "Template missing")
            return
        }
        val resolved = template.items
            .sortedBy { it.orderIndex }
            .mapNotNull { ti -> exercises.byId(ti.exerciseId)?.let { ResolvedItem(ti, it) } }
        if (resolved.isEmpty()) {
            _state.value = RunnerState(phase = RunnerPhase.ERROR, errorMessage = "No exercises")
            return
        }
        val first = resolved.first()
        val firstDurationSec = first.template.targetDurationSec ?: 0
        _state.value = RunnerState(
            phase = RunnerPhase.EXERCISE,
            templateName = template.name,
            items = resolved,
            currentItemIndex = 0,
            currentSet = 1,
            totalSetsForCurrent = first.template.targetSets,
            secondsLeft = firstDurationSec,
            phaseTotalSec = firstDurationSec,
        )
        startForegroundNotification(template.name, "Set 1 of ${first.template.targetSets}")
        if (firstDurationSec > 0) startTicker(firstDurationSec) {
            completeSet(repsActual = null, weightKg = null)
        }
    }

    fun completeSet(repsActual: Int?, weightKg: Double?) {
        viewModelScope.launch {
            val s = _state.value
            val item = s.currentItem ?: return@launch
            val now = time.now().toEpochMilli()
            sessions.recordSet(
                SessionSetEntity(
                    sessionId = sessionId,
                    exerciseId = item.exercise.id,
                    setNumber = s.currentSet,
                    repsActual = repsActual ?: item.template.targetReps,
                    weightKg = weightKg,
                    durationSec = item.template.targetDurationSec,
                    completedAtEpochMs = now,
                )
            )
            val moreSets = s.currentSet < s.totalSetsForCurrent
            if (moreSets) {
                enterRest(item.template.targetRestSec)
            } else {
                advanceExercise()
            }
        }
    }

    private fun enterRest(seconds: Int) {
        _state.update { it.copy(phase = RunnerPhase.REST, secondsLeft = seconds, phaseTotalSec = seconds) }
        startTicker(seconds) {
            _state.update {
                val nextDuration = it.currentItem?.template?.targetDurationSec ?: 0
                it.copy(
                    phase = RunnerPhase.EXERCISE,
                    currentSet = it.currentSet + 1,
                    secondsLeft = nextDuration,
                    phaseTotalSec = nextDuration,
                )
            }
            startSetTimerIfNeeded()
        }
    }

    private fun advanceExercise() {
        _state.update { s ->
            val nextIdx = s.currentItemIndex + 1
            if (nextIdx >= s.items.size) {
                s.copy(phase = RunnerPhase.COMPLETE, currentItemIndex = nextIdx)
            } else {
                val next = s.items[nextIdx]
                val dur = next.template.targetDurationSec ?: 0
                s.copy(
                    currentItemIndex = nextIdx,
                    currentSet = 1,
                    totalSetsForCurrent = next.template.targetSets,
                    phase = RunnerPhase.EXERCISE,
                    secondsLeft = dur,
                    phaseTotalSec = dur,
                )
            }
        }
        if (_state.value.phase == RunnerPhase.COMPLETE) {
            viewModelScope.launch { finalize() }
        } else {
            startSetTimerIfNeeded()
        }
    }

    private fun startSetTimerIfNeeded() {
        val item = _state.value.currentItem ?: return
        val secs = item.template.targetDurationSec ?: return
        startTicker(secs) { completeSet(repsActual = null, weightKg = null) }
    }

    private fun startTicker(seconds: Int, onElapsed: () -> Unit) {
        ticker?.cancel()
        ticker = viewModelScope.launch {
            var remaining = seconds
            while (remaining > 0) {
                delay(1000)
                remaining -= 1
                _state.update { it.copy(secondsLeft = remaining) }
            }
            onElapsed()
        }
    }

    fun skipRest() {
        ticker?.cancel()
        _state.update { it.copy(phase = RunnerPhase.EXERCISE, currentSet = it.currentSet + 1) }
        startSetTimerIfNeeded()
    }

    private suspend fun finalize() {
        ticker?.cancel()
        sessions.complete(sessionId)
        val sessionDomain = sessions.getSession(sessionId)
        val endedAt = sessionDomain?.completedAt
        if (sessionDomain != null && endedAt != null) {
            val samples = health.readSessionSamples(sessionId, sessionDomain.startedAt, endedAt)
            if (samples.isNotEmpty()) healthSamples.storeAll(samples)
            health.writeExerciseSession(
                title = _state.value.templateName,
                startedAt = sessionDomain.startedAt,
                endedAt = endedAt,
                notes = null,
            )
            val today = time.today(ZoneId.systemDefault())
            val updated = StreakEngine.onWorkoutCompleted(streaks.get(), today)
            streaks.save(updated)
        }
        stopForegroundNotification()
    }

    fun cancel(onCancelled: () -> Unit = {}) {
        ticker?.cancel()
        viewModelScope.launch {
            sessions.cancel(sessionId)
            stopForegroundNotification()
            onCancelled()
        }
    }

    private fun startForegroundNotification(title: String, content: String) {
        val intent = Intent(context, WorkoutForegroundService::class.java).apply {
            putExtra(WorkoutForegroundService.EXTRA_TITLE, title)
            putExtra(WorkoutForegroundService.EXTRA_CONTENT, content)
        }
        runCatching { context.startForegroundService(intent) }
    }

    private fun stopForegroundNotification() {
        runCatching { context.stopService(Intent(context, WorkoutForegroundService::class.java)) }
    }

    override fun onCleared() {
        ticker?.cancel()
        super.onCleared()
    }

    @Suppress("unused") fun streaksFlow(): kotlinx.coroutines.flow.Flow<StreakState> = streaks.observe()
}
