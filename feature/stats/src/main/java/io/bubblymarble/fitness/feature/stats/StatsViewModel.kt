package io.bubblymarble.fitness.feature.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.bubblymarble.fitness.core.common.TimeSource
import io.bubblymarble.fitness.core.data.model.StreakState
import io.bubblymarble.fitness.core.data.model.WorkoutSession
import io.bubblymarble.fitness.core.data.repository.SessionRepository
import io.bubblymarble.fitness.core.data.repository.StreakRepository
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class StatsUi(
    val streak: StreakState? = null,
    val totalSessions: Int = 0,
    val sessionsLast7d: Int = 0,
    val totalSets: Int = 0,
    val totalVolumeKg: Double = 0.0,
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    sessions: SessionRepository,
    streaks: StreakRepository,
    time: TimeSource,
) : ViewModel() {

    val ui: StateFlow<StatsUi> = combine(
        sessions.observeCompleted(),
        streaks.observe(),
    ) { completed, streak -> compute(completed, streak, time) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StatsUi())

    private fun compute(sessions: List<WorkoutSession>, streak: StreakState, time: TimeSource): StatsUi {
        val now = time.now()
        val sevenDaysAgo = now.minus(7, ChronoUnit.DAYS)
        val recent = sessions.count { it.completedAt?.isAfter(sevenDaysAgo) == true }
        val totalSets = sessions.sumOf { it.sets.size }
        val totalVolume = sessions.sumOf { s ->
            s.sets.sumOf { (it.weightKg ?: 0.0) * (it.repsActual ?: 0) }
        }
        return StatsUi(
            streak = streak,
            totalSessions = sessions.size,
            sessionsLast7d = recent,
            totalSets = totalSets,
            totalVolumeKg = totalVolume,
        )
    }
}
