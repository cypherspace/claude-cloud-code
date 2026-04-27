package io.bubblymarble.fitness.core.data.repository

import io.bubblymarble.fitness.core.common.TimeSource
import io.bubblymarble.fitness.core.data.db.dao.IngredientDao
import io.bubblymarble.fitness.core.data.db.dao.MealDao
import io.bubblymarble.fitness.core.data.db.entities.MealEntity
import io.bubblymarble.fitness.core.data.db.entities.MealItemEntity
import io.bubblymarble.fitness.core.data.model.Meal
import io.bubblymarble.fitness.core.data.model.MealType
import io.bubblymarble.fitness.core.data.model.toDomain
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class MealRepository @Inject constructor(
    private val dao: MealDao,
    private val ingredientDao: IngredientDao,
    private val time: TimeSource,
) {
    fun observeForDay(day: LocalDate, zone: ZoneId = ZoneId.systemDefault()): Flow<List<Meal>> {
        val start = day.atStartOfDay(zone).toInstant().toEpochMilli()
        val end = day.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1
        return dao.observeBetween(start, end).map { meals -> hydrate(meals) }
    }

    suspend fun byId(id: Long): Meal? {
        val meal = dao.byId(id) ?: return null
        return hydrate(listOf(meal)).firstOrNull()
    }

    suspend fun saveMeal(
        id: Long?,
        name: String,
        type: MealType,
        eatenAt: Instant?,
        notes: String?,
        photoUri: String?,
        items: List<Pair<String, Double>>,
    ): Long {
        val meal = MealEntity(
            id = id ?: 0L,
            name = name.trim().ifBlank { "Meal" },
            mealType = type.name,
            eatenAtEpochMs = (eatenAt ?: time.now()).toEpochMilli(),
            notes = notes,
            photoUri = photoUri,
        )
        val itemEntities = items.map { (ingredientId, grams) ->
            MealItemEntity(mealId = 0L, ingredientId = ingredientId, grams = grams)
        }
        return dao.replaceMeal(meal, itemEntities)
    }

    suspend fun delete(id: Long) = dao.deleteMeal(id)

    private suspend fun hydrate(meals: List<MealEntity>): List<Meal> {
        if (meals.isEmpty()) return emptyList()
        val ids = meals.map { it.id }
        val items = dao.itemsForMeals(ids)
        val ingredientLookup = ingredientDao.byIds(items.map { it.ingredientId }.distinct())
            .associateBy { it.id }
        val itemsByMeal = items.groupBy { it.mealId }
        return meals.map { meal ->
            meal.toDomain(itemsByMeal[meal.id].orEmpty(), ingredientLookup)
        }
    }
}
