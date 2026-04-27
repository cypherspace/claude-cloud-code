package io.bubblymarble.fitness.core.ai

import com.google.common.truth.Truth.assertThat
import io.bubblymarble.fitness.core.data.model.Equipment
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

    private fun profile(
        equipment: EquipmentAccess = EquipmentAccess.FULL_GYM,
        owned: Set<String> = emptySet(),
    ) = UserProfile(
        displayName = "Test",
        dob = null,
        sexAtBirth = null,
        heightCm = null,
        goal = GoalType.HYPERTROPHY,
        weeklyTargetSessions = 3,
        equipment = equipment,
        ownedEquipment = owned,
        experience = ExperienceLevel.INTERMEDIATE,
        injuryNotes = null,
    )

    /**
     * Mirrors the filtering the live PlanGenerator does before handing the catalogue to FallbackPlan
     * or the AI prompt. Keeps test assertions aligned with production behaviour.
     */
    private fun filteredFor(p: UserProfile): List<Exercise> {
        val owned = Equipment.resolve(p.equipment, p.ownedEquipment)
        return catalogue.filter { ex -> ex.equipment.lowercase().trim().let { it in owned || (it.isBlank() && Equipment.BODYWEIGHT in owned) } }
    }

    @Test fun fallback_buildsDaysWithExercisesFromCatalogue() {
        val p = profile()
        val plans = FallbackPlan.build(p, PlanRequest(3, 45), filteredFor(p), 0L)
        assertThat(plans).isNotEmpty()
        plans.forEach { tpl ->
            assertThat(tpl.items).isNotEmpty()
            tpl.items.forEach { item ->
                assertThat(catalogue.map { it.id }).contains(item.exerciseId)
            }
        }
    }

    @Test fun bodyweightOnly_excludesBarbellLifts() {
        val p = profile(EquipmentAccess.BODYWEIGHT_ONLY)
        val filtered = filteredFor(p)
        assertThat(filtered.map { it.id }).containsExactly("pushup")
        val plans = FallbackPlan.build(p, PlanRequest(3, 30), filtered, 0L)
        plans.flatMap { it.items }.forEach { item ->
            val ex = catalogue.first { it.id == item.exerciseId }
            assertThat(ex.equipment.lowercase()).isEqualTo("bodyweight")
        }
    }

    @Test fun minimalHome_filtersByOwnedSet() {
        val p = profile(EquipmentAccess.MINIMAL_HOME, owned = setOf(Equipment.DUMBBELL))
        val filtered = filteredFor(p)
        // Catalogue has nothing tagged "dumbbell"; only "bodyweight" stays via the implicit add.
        assertThat(filtered.map { it.id }).containsExactly("pushup")
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
