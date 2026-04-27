package io.bubblymarble.fitness.feature.workouts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.bubblymarble.fitness.core.data.model.WorkoutTemplate
import io.bubblymarble.fitness.core.data.repository.SessionRepository
import io.bubblymarble.fitness.core.data.repository.WorkoutTemplateRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class WorkoutsViewModel @Inject constructor(
    private val templates: WorkoutTemplateRepository,
    private val sessions: SessionRepository,
) : ViewModel() {

    val templatesFlow: StateFlow<List<WorkoutTemplate>> = templates.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun startSession(templateId: Long, onStarted: (Long) -> Unit) {
        viewModelScope.launch {
            val id = sessions.startSession(templateId)
            onStarted(id)
        }
    }
}
