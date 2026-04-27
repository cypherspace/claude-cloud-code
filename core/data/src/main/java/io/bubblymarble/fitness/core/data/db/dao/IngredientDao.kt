package io.bubblymarble.fitness.core.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.bubblymarble.fitness.core.data.db.entities.IngredientEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IngredientDao {
    @Query("SELECT * FROM ingredient ORDER BY name ASC")
    fun observeAll(): Flow<List<IngredientEntity>>

    @Query("SELECT * FROM ingredient WHERE id = :id")
    suspend fun byId(id: String): IngredientEntity?

    @Query("SELECT * FROM ingredient WHERE id IN (:ids)")
    suspend fun byIds(ids: List<String>): List<IngredientEntity>

    @Query("SELECT * FROM ingredient WHERE barcode = :barcode LIMIT 1")
    suspend fun byBarcode(barcode: String): IngredientEntity?

    @Query("SELECT * FROM ingredient WHERE name LIKE '%' || :q || '%' OR brand LIKE '%' || :q || '%' ORDER BY name ASC LIMIT :limit")
    suspend fun search(q: String, limit: Int = 50): List<IngredientEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(ingredient: IngredientEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(ingredients: List<IngredientEntity>)

    @Query("DELETE FROM ingredient WHERE id = :id")
    suspend fun delete(id: String)
}
