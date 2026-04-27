package io.bubblymarble.fitness.core.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercise")
data class ExerciseEntity(
    @PrimaryKey val id: String,
    val name: String,
    val category: String,
    val primaryMuscle: String,
    val secondaryMuscles: String,
    val equipment: String,
    val level: String,
    val instructions: String,
    val mediaUrl: String? = null,
)
