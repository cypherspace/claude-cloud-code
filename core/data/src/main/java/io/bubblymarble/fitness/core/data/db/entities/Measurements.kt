package io.bubblymarble.fitness.core.data.db.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "body_measurement",
    indices = [Index("type", "timestampEpochMs")]
)
data class BodyMeasurementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,
    val timestampEpochMs: Long,
    val value: Double,
    val unit: String,
    val source: String? = null,
)
