package io.bubblymarble.fitness.core.data.seed

import kotlinx.serialization.Serializable

@Serializable
data class SeedExercise(
    val id: String,
    val name: String,
    val category: String,
    val primaryMuscle: String,
    val secondaryMuscles: List<String> = emptyList(),
    val equipment: String,
    val level: String,
    val instructions: List<String> = emptyList(),
    val mediaUrl: String? = null,
)
