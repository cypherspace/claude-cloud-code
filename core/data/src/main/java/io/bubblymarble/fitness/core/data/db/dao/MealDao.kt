package io.bubblymarble.fitness.core.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import io.bubblymarble.fitness.core.data.db.entities.MealEntity
import io.bubblymarble.fitness.core.data.db.entities.MealItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MealDao {
    @Query("SELECT * FROM meal WHERE eatenAtEpochMs BETWEEN :startEpochMs AND :endEpochMs ORDER BY eatenAtEpochMs ASC")
    fun observeBetween(startEpochMs: Long, endEpochMs: Long): Flow<List<MealEntity>>

    @Query("SELECT * FROM meal WHERE id = :id")
    suspend fun byId(id: Long): MealEntity?

    @Query("SELECT * FROM meal_item WHERE mealId = :mealId")
    suspend fun itemsForMeal(mealId: Long): List<MealItemEntity>

    @Query("SELECT * FROM meal_item WHERE mealId IN (:mealIds)")
    suspend fun itemsForMeals(mealIds: List<Long>): List<MealItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeal(meal: MealEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<MealItemEntity>)

    @Query("DELETE FROM meal_item WHERE mealId = :mealId")
    suspend fun clearItems(mealId: Long)

    @Query("DELETE FROM meal WHERE id = :id")
    suspend fun deleteMeal(id: Long)

    @Transaction
    suspend fun replaceMeal(meal: MealEntity, items: List<MealItemEntity>): Long {
        val id = insertMeal(meal)
        clearItems(id)
        insertItems(items.map { it.copy(mealId = id) })
        return id
    }
}
