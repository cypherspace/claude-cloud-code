package io.bubblymarble.fitness.core.data.model

import com.google.common.truth.Truth.assertThat
import io.bubblymarble.fitness.core.data.db.entities.ExerciseEntity
import org.junit.Test

class MappersTest {
    @Test fun exerciseRoundTrip() {
        val original = Exercise(
            id = "id",
            name = "Push-up",
            category = "Strength",
            primaryMuscle = "chest",
            secondaryMuscles = listOf("triceps", "shoulders"),
            equipment = "bodyweight",
            level = "beginner",
            instructions = listOf("Plank.", "Lower.", "Press."),
            mediaUrl = null,
        )
        val entity: ExerciseEntity = original.toEntity()
        val back = entity.toDomain()
        assertThat(back).isEqualTo(original)
    }

    @Test fun emptySecondaryMuscles_roundTripCleanly() {
        val original = Exercise(
            id = "x", name = "x", category = "c", primaryMuscle = "m",
            secondaryMuscles = emptyList(), equipment = "e", level = "l",
            instructions = emptyList(), mediaUrl = null,
        )
        val back = original.toEntity().toDomain()
        assertThat(back.secondaryMuscles).isEmpty()
        assertThat(back.instructions).isEmpty()
    }
}
