package io.bubblymarble.fitness.core.data.db.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "health_sample",
    indices = [Index("type", "timestampEpochMs")]
)
data class HealthSampleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,
    val timestampEpochMs: Long,
    val value: Double,
    val unit: String,
    val source: String? = null,
    val sessionId: Long? = null,
)
