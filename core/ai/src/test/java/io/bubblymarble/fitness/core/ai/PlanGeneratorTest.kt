package io.bubblymarble.fitness.core.ai

import com.google.common.truth.Truth.assertThat
import io.bubblymarble.fitness.core.data.model.EquipmentAccess
import io.bubblymarble.fitness.core.data.model.Exercise
import io.bubblymarble.fitness.core.data.model.ExperienceLevel
import io.bubblymarble.fitness.core.data.model.GoalType
import io.bubblymarble.fitness.core.data.model.UserProfile
import org.junit.Test

class PlanGeneratorTest {

    private val catalogue = listOf(
        Exercise("squat", "Back squat", "Strength", "quadriceps", listOf("glutes"), "barbell", "intermediate", emptyList(), null),
        Exercise("bench", "Barbell bench press", "Strength", "chest", listOf("triceps"), "barbell", "intermediate", emptyList(), null),
        Exercise("row", "Barbell row", "Strength", "back", listOf("biceps"), "barbell", "intermediate", emptyList(), null),
        Exercise("pushup", "Push-up", "Strength", "chest", listOf("triceps"), "bodyweight", "beginner", emptyList(), null),
    )

    private fun profile(equipment: EquipmentAccess = EquipmentAccess.FULL_GYM) = UserProfile(
        displayName = "Test",
        dob = null,
        sexAtBirth = null,
        heightCm = null,
        goal = GoalType.HYPERTROPHY,
        weeklyTargetSessions = 3,
        equipment = equipment,
        experience = ExperienceLevel.INTERMEDIATE,
        injuryNotes = null,
    )

    @Test fun fallback_buildsDaysWithExercisesFromCatalogue() {
        val plans = FallbackPlan.build(profile(), io.bubblymarble.fitness.core.ai.PlanRequest(3, 45), catalogue, 0L)
        assertThat(plans).isNotEmpty()
        plans.forEach { tpl ->
            assertThat(tpl.items).isNotEmpty()
            tpl.items.forEach { item ->
                assertThat(catalogue.map { it.id }).contains(item.exerciseId)
            }
        }
    }

    @Test fun bodyweightOnly_excludesBarbellLifts() {
        val plans = FallbackPlan.build(
            profile(EquipmentAccess.BODYWEIGHT_ONLY),
            io.bubblymarble.fitness.core.ai.PlanRequest(3, 30),
            catalogue,
            0L,
        )
        plans.flatMap { it.items }.forEach { item ->
            val ex = catalogue.first { it.id == item.exerciseId }
            assertThat(ex.equipment.lowercase()).contains("body")
        }
    }

    @Test fun aiOutput_filtersUnknownExerciseIds() {
        val raw = GeneratedPlan(
            sessions = listOf(
                GeneratedSession(
                    name = "Day 1",
                    exercises = listOf(
                        GeneratedExercise(exerciseId = "squat", sets = 4, reps = 6),
                        GeneratedExercise(exerciseId = "made_up_thing", sets = 3, reps = 10),
                    ),
                ),
            ),
        )
        val templates = raw.toTemplates(catalogue, nowEpochMs = 123L)
        assertThat(templates).hasSize(1)
        assertThat(templates.first().items.map { it.exerciseId }).containsExactly("squat")
    }

    @Test fun aiOutput_dropsEmptySessions() {
        val raw = GeneratedPlan(
            sessions = listOf(
                GeneratedSession(name = "Empty", exercises = emptyList()),
                GeneratedSession(
                    name = "Real",
                    exercises = listOf(GeneratedExercise(exerciseId = "bench", sets = 3, reps = 8)),
                ),
            ),
        )
        val templates = raw.toTemplates(catalogue, nowEpochMs = 1L)
        assertThat(templates.map { it.name }).containsExactly("Real")
    }
}
