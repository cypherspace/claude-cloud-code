package io.bubblymarble.fitness.core.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "streak_state")
data class StreakStateEntity(
    @PrimaryKey val id: Int = 0,
    val lastCompletedEpochDay: Long?,
    val currentLength: Int,
    val longestLength: Int,
    val ruleType: String,
    val ruleTargetPerWeek: Int,
)
