package io.bubblymarble.fitness.core.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.bubblymarble.fitness.core.data.db.entities.StreakStateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StreakDao {
    @Query("SELECT * FROM streak_state WHERE id = 0 LIMIT 1")
    fun observe(): Flow<StreakStateEntity?>

    @Query("SELECT * FROM streak_state WHERE id = 0 LIMIT 1")
    suspend fun get(): StreakStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(state: StreakStateEntity)
}
