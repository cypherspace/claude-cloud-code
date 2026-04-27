package io.bubblymarble.fitness.core.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.bubblymarble.fitness.core.data.db.entities.HealthSampleEntity

@Dao
interface HealthSampleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(samples: List<HealthSampleEntity>)

    @Query("SELECT * FROM health_sample WHERE sessionId = :sessionId ORDER BY timestampEpochMs ASC")
    suspend fun samplesForSession(sessionId: Long): List<HealthSampleEntity>

    @Query(
        """
        SELECT * FROM health_sample
        WHERE timestampEpochMs BETWEEN :startEpochMs AND :endEpochMs
          AND type = :type
        ORDER BY timestampEpochMs ASC
        """
    )
    suspend fun samplesIn(type: String, startEpochMs: Long, endEpochMs: Long): List<HealthSampleEntity>
}
