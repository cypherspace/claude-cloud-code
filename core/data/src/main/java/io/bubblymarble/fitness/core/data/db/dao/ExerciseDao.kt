package io.bubblymarble.fitness.core.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.bubblymarble.fitness.core.data.db.entities.ExerciseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {
    @Query("SELECT * FROM exercise ORDER BY name ASC")
    fun observeAll(): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercise WHERE id IN (:ids)")
    suspend fun byIds(ids: List<String>): List<ExerciseEntity>

    @Query("SELECT * FROM exercise WHERE id = :id")
    suspend fun byId(id: String): ExerciseEntity?

    @Query(
        """
        SELECT * FROM exercise
        WHERE (:query = '' OR name LIKE '%' || :query || '%')
          AND (:muscle = '' OR primaryMuscle = :muscle OR secondaryMuscles LIKE '%' || :muscle || '%')
          AND (:equipment = '' OR equipment = :equipment)
        ORDER BY name ASC
        """
    )
    fun search(query: String, muscle: String, equipment: String): Flow<List<ExerciseEntity>>

    @Query("SELECT COUNT(*) FROM exercise")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<ExerciseEntity>)
}
