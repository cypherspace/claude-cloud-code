package io.bubblymarble.fitness.core.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 0,
    val displayName: String,
    val dobEpochDay: Long?,
    val sexAtBirth: String?,
    val heightCm: Double?,
    val goalType: String,
    val weeklyTargetSessions: Int,
    val equipmentAccess: String,
    val experienceLevel: String,
    val injuryNotes: String? = null,
    val createdAtEpochMs: Long,
)
