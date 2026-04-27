package io.bubblymarble.fitness.feature.plans

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.bubblymarble.fitness.core.ai.PlanGenerator
import io.bubblymarble.fitness.core.ai.PlanRequest
import io.bubblymarble.fitness.core.data.model.WorkoutTemplate
import io.bubblymarble.fitness.core.data.repository.UserProfileRepository
import io.bubblymarble.fitness.core.data.repository.WorkoutTemplateRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PlansUiState(
    val templates: List<WorkoutTemplate> = emptyList(),
    val generating: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class PlansViewModel @Inject constructor(
    private val templates: WorkoutTemplateRepository,
    private val profile: UserProfileRepository,
    private val planner: PlanGenerator,
) : ViewModel() {

    val templatesFlow: StateFlow<List<WorkoutTemplate>> = templates.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _state = kotlinx.coroutines.flow.MutableStateFlow(PlansUiState())
    val state: StateFlow<PlansUiState> = _state

    fun generate(sessionsPerWeek: Int, minutesPerSession: Int) {
        if (_state.value.generating) return
        _state.value = _state.value.copy(generating = true, error = null)
        viewModelScope.launch {
            try {
                val p = profile.get() ?: error("Complete onboarding first")
                val plans = planner.generate(p, PlanRequest(sessionsPerWeek, minutesPerSession))
                plans.forEach { templates.save(it) }
            } catch (t: Throwable) {
                _state.value = _state.value.copy(error = t.message ?: "Plan generation failed")
            } finally {
                _state.value = _state.value.copy(generating = false)
            }
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch { templates.delete(id) }
    }
}
