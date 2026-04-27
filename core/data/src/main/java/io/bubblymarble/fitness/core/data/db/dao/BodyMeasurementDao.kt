package io.bubblymarble.fitness.core.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.bubblymarble.fitness.core.data.db.entities.BodyMeasurementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BodyMeasurementDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<BodyMeasurementEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: BodyMeasurementEntity): Long

    @Query("SELECT * FROM body_measurement ORDER BY timestampEpochMs DESC")
    fun observeAll(): Flow<List<BodyMeasurementEntity>>

    @Query("SELECT * FROM body_measurement WHERE type = :type ORDER BY timestampEpochMs DESC")
    fun observeByType(type: String): Flow<List<BodyMeasurementEntity>>

    @Query("SELECT * FROM body_measurement WHERE type = :type ORDER BY timestampEpochMs DESC LIMIT 1")
    suspend fun latestByType(type: String): BodyMeasurementEntity?

    @Query(
        """
        SELECT * FROM body_measurement
        WHERE type = :type AND timestampEpochMs >= :sinceEpochMs
        ORDER BY timestampEpochMs ASC
        """
    )
    suspend fun seriesSince(type: String, sinceEpochMs: Long): List<BodyMeasurementEntity>

    @Query("DELETE FROM body_measurement WHERE id = :id")
    suspend fun delete(id: Long)

    /** Used by the sync flow to dedupe imports from Health Connect. */
    @Query(
        """
        SELECT EXISTS(
          SELECT 1 FROM body_measurement
          WHERE type = :type AND timestampEpochMs = :timestampEpochMs AND source = :source
        )
        """
    )
    suspend fun exists(type: String, timestampEpochMs: Long, source: String): Boolean
}
