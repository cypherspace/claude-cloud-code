package io.bubblymarble.fitness.core.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import io.bubblymarble.fitness.core.data.db.entities.WorkoutTemplateEntity
import io.bubblymarble.fitness.core.data.db.entities.WorkoutTemplateExerciseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutTemplateDao {
    @Query("SELECT * FROM workout_template ORDER BY createdAtEpochMs DESC")
    fun observeTemplates(): Flow<List<WorkoutTemplateEntity>>

    @Query("SELECT * FROM workout_template WHERE id = :id")
    suspend fun templateById(id: Long): WorkoutTemplateEntity?

    @Query("SELECT * FROM workout_template_exercise WHERE templateId = :templateId ORDER BY orderIndex ASC")
    suspend fun exercisesForTemplate(templateId: Long): List<WorkoutTemplateExerciseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: WorkoutTemplateEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercises(items: List<WorkoutTemplateExerciseEntity>)

    @Query("DELETE FROM workout_template_exercise WHERE templateId = :templateId")
    suspend fun clearExercises(templateId: Long)

    @Transaction
    suspend fun replaceTemplate(
        template: WorkoutTemplateEntity,
        exercises: List<WorkoutTemplateExerciseEntity>,
    ): Long {
        val id = insertTemplate(template)
        clearExercises(id)
        insertExercises(exercises.map { it.copy(templateId = id) })
        return id
    }

    @Query("DELETE FROM workout_template WHERE id = :id")
    suspend fun delete(id: Long)
}
