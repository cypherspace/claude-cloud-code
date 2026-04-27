package io.bubblymarble.fitness.core.data.repository

import io.bubblymarble.fitness.core.data.db.dao.IngredientDao
import io.bubblymarble.fitness.core.data.model.Ingredient
import io.bubblymarble.fitness.core.data.model.toDomain
import io.bubblymarble.fitness.core.data.model.toEntity
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class IngredientRepository @Inject constructor(
    private val dao: IngredientDao,
) {
    fun observeAll(): Flow<List<Ingredient>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    suspend fun byId(id: String): Ingredient? = dao.byId(id)?.toDomain()
    suspend fun byBarcode(barcode: String): Ingredient? = dao.byBarcode(barcode)?.toDomain()
    suspend fun search(query: String, limit: Int = 50): List<Ingredient> =
        dao.search(query.trim(), limit).map { it.toDomain() }

    suspend fun upsert(ingredient: Ingredient) = dao.upsert(ingredient.toEntity())
    suspend fun upsertAll(ingredients: List<Ingredient>) = dao.upsertAll(ingredients.map { it.toEntity() })
    suspend fun delete(id: String) = dao.delete(id)
}
