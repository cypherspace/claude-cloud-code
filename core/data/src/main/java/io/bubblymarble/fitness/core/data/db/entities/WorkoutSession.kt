package io.bubblymarble.fitness.core.data.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "workout_session",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutTemplateEntity::class,
            parentColumns = ["id"],
            childColumns = ["templateId"],
            onDelete = ForeignKey.SET_NULL
        ),
    ],
    indices = [Index("templateId"), Index("startedAtEpochMs")]
)
data class WorkoutSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val templateId: Long?,
    val startedAtEpochMs: Long,
    val completedAtEpochMs: Long?,
    val notes: String? = null,
    val rpe: Int? = null,
)

@Entity(
    tableName = "session_set",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.RESTRICT
        ),
    ],
    indices = [Index("sessionId"), Index("exerciseId")]
)
data class SessionSetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val exerciseId: String,
    val setNumber: Int,
    val repsActual: Int? = null,
    val weightKg: Double? = null,
    val durationSec: Int? = null,
    val distanceM: Double? = null,
    val hrAvgBpm: Int? = null,
    val hrMaxBpm: Int? = null,
    val spo2MinPercent: Int? = null,
    val completedAtEpochMs: Long? = null,
)
