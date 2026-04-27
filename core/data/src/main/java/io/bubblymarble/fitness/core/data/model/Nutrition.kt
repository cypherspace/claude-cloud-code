package io.bubblymarble.fitness.core.data.model

import io.bubblymarble.fitness.core.data.db.entities.IngredientEntity
import io.bubblymarble.fitness.core.data.db.entities.MealEntity
import io.bubblymarble.fitness.core.data.db.entities.MealItemEntity
import java.time.Instant

enum class MealType { BREAKFAST, LUNCH, DINNER, SNACK }

data class Ingredient(
    val id: String,
    val name: String,
    val brand: String?,
    val barcode: String?,
    val source: String,
    val servingGrams: Double,
    val kcalPer100g: Double,
    val proteinPer100g: Double,
    val carbsPer100g: Double,
    val fatPer100g: Double,
    val fibrePer100g: Double?,
)

data class MealItem(
    val id: Long,
    val ingredient: Ingredient,
    val grams: Double,
) {
    val nutrition: NutritionTotals get() = NutritionTotals.of(ingredient, grams)
}

data class Meal(
    val id: Long,
    val name: String,
    val mealType: MealType,
    val eatenAt: Instant,
    val notes: String?,
    val photoUri: String?,
    val items: List<MealItem>,
) {
    val nutrition: NutritionTotals get() = items.fold(NutritionTotals.ZERO) { acc, it -> acc + it.nutrition }
}

data class NutritionTotals(
    val kcal: Double,
    val proteinG: Double,
    val carbsG: Double,
    val fatG: Double,
    val fibreG: Double,
) {
    operator fun plus(other: NutritionTotals) = NutritionTotals(
        kcal + other.kcal,
        proteinG + other.proteinG,
        carbsG + other.carbsG,
        fatG + other.fatG,
        fibreG + other.fibreG,
    )

    companion object {
        val ZERO = NutritionTotals(0.0, 0.0, 0.0, 0.0, 0.0)

        fun of(ingredient: Ingredient, grams: Double): NutritionTotals {
            val factor = grams / 100.0
            return NutritionTotals(
                kcal = ingredient.kcalPer100g * factor,
                proteinG = ingredient.proteinPer100g * factor,
                carbsG = ingredient.carbsPer100g * factor,
                fatG = ingredient.fatPer100g * factor,
                fibreG = (ingredient.fibrePer100g ?: 0.0) * factor,
            )
        }
    }
}

fun IngredientEntity.toDomain() = Ingredient(
    id = id,
    name = name,
    brand = brand,
    barcode = barcode,
    source = source,
    servingGrams = servingGrams,
    kcalPer100g = kcalPer100g,
    proteinPer100g = proteinPer100g,
    carbsPer100g = carbsPer100g,
    fatPer100g = fatPer100g,
    fibrePer100g = fibrePer100g,
)

fun Ingredient.toEntity() = IngredientEntity(
    id = id,
    name = name,
    brand = brand,
    barcode = barcode,
    source = source,
    servingGrams = servingGrams,
    kcalPer100g = kcalPer100g,
    proteinPer100g = proteinPer100g,
    carbsPer100g = carbsPer100g,
    fatPer100g = fatPer100g,
    fibrePer100g = fibrePer100g,
)

fun MealEntity.toDomain(items: List<MealItemEntity>, ingredientLookup: Map<String, IngredientEntity>): Meal =
    Meal(
        id = id,
        name = name,
        mealType = runCatching { MealType.valueOf(mealType) }.getOrDefault(MealType.SNACK),
        eatenAt = Instant.ofEpochMilli(eatenAtEpochMs),
        notes = notes,
        photoUri = photoUri,
        items = items.mapNotNull { item ->
            val ing = ingredientLookup[item.ingredientId] ?: return@mapNotNull null
            MealItem(id = item.id, ingredient = ing.toDomain(), grams = item.grams)
        },
    )
