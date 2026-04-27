package io.bubblymarble.fitness.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.bubblymarble.fitness.core.data.model.EquipmentAccess
import io.bubblymarble.fitness.core.data.model.ExperienceLevel
import io.bubblymarble.fitness.core.data.model.GoalType
import io.bubblymarble.fitness.core.data.model.UserProfile
import io.bubblymarble.fitness.core.data.repository.ExerciseRepository
import io.bubblymarble.fitness.core.data.repository.UserProfileRepository
import io.bubblymarble.fitness.core.data.seed.ExerciseSeeder
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OnboardingState(
    val displayName: String = "",
    val goal: GoalType = GoalType.GENERAL_FITNESS,
    val experience: ExperienceLevel = ExperienceLevel.BEGINNER,
    val equipment: EquipmentAccess = EquipmentAccess.MINIMAL_HOME,
    val weeklyTarget: Int = 3,
    val saving: Boolean = false,
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val profiles: UserProfileRepository,
    private val exercises: ExerciseRepository,
    private val seeder: ExerciseSeeder,
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    fun setName(v: String) = _state.update { it.copy(displayName = v) }
    fun setGoal(g: GoalType) = _state.update { it.copy(goal = g) }
    fun setExperience(e: ExperienceLevel) = _state.update { it.copy(experience = e) }
    fun setEquipment(e: EquipmentAccess) = _state.update { it.copy(equipment = e) }
    fun setWeeklyTarget(n: Int) = _state.update { it.copy(weeklyTarget = n.coerceIn(1, 7)) }

    fun finish(onDone: () -> Unit) {
        val s = _state.value
        if (s.displayName.isBlank()) return
        _state.update { it.copy(saving = true) }
        viewModelScope.launch {
            seeder.seedIfEmpty()
            // touch the repo so the cold flow is materialized once
            exercises.count()
            profiles.save(
                UserProfile(
                    displayName = s.displayName.trim(),
                    dob = null,
                    sexAtBirth = null,
                    heightCm = null,
                    goal = s.goal,
                    weeklyTargetSessions = s.weeklyTarget,
                    equipment = s.equipment,
                    experience = s.experience,
                    injuryNotes = null,
                )
            )
            _state.update { it.copy(saving = false) }
            onDone()
        }
    }
}
