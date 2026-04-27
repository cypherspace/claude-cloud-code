package io.bubblymarble.fitness.core.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import io.bubblymarble.fitness.core.data.db.entities.SessionSetEntity
import io.bubblymarble.fitness.core.data.db.entities.WorkoutSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Insert
    suspend fun insertSession(session: WorkoutSessionEntity): Long

    @Update
    suspend fun updateSession(session: WorkoutSessionEntity)

    @Query("SELECT * FROM workout_session WHERE id = :id")
    suspend fun sessionById(id: Long): WorkoutSessionEntity?

    @Query("SELECT * FROM workout_session WHERE completedAtEpochMs IS NOT NULL ORDER BY completedAtEpochMs DESC")
    fun observeCompleted(): Flow<List<WorkoutSessionEntity>>

    @Query(
        """
        SELECT * FROM workout_session
        WHERE completedAtEpochMs IS NOT NULL
          AND completedAtEpochMs >= :sinceEpochMs
        ORDER BY completedAtEpochMs DESC
        """
    )
    suspend fun completedSince(sinceEpochMs: Long): List<WorkoutSessionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSet(set: SessionSetEntity): Long

    @Update
    suspend fun updateSet(set: SessionSetEntity)

    @Query("SELECT * FROM session_set WHERE sessionId = :sessionId ORDER BY setNumber ASC")
    suspend fun setsForSession(sessionId: Long): List<SessionSetEntity>

    @Query("SELECT * FROM session_set WHERE sessionId = :sessionId ORDER BY setNumber ASC")
    fun observeSetsForSession(sessionId: Long): Flow<List<SessionSetEntity>>
}
