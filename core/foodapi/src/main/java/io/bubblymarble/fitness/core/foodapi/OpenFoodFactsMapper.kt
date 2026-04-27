package io.bubblymarble.fitness.core.foodapi

import io.bubblymarble.fitness.core.data.model.Ingredient

/**
 * Open Food Facts publishes nutriments under several conventions. We prefer the per-100g values
 * because that's how the rest of the app stores nutrition. The keys are tried in priority order.
 */
internal object OpenFoodFactsMapper {

    fun toIngredient(product: OffProduct, source: String): Ingredient? {
        val name = product.productNameEn?.takeIf { it.isNotBlank() }
            ?: product.productName?.takeIf { it.isNotBlank() }
            ?: return null
        val nutriments = product.nutriments ?: return null

        val kcal = readPer100g(nutriments, ENERGY_KCAL_KEYS)
            ?: readPer100g(nutriments, ENERGY_KJ_KEYS)?.let { it / 4.184 }
            ?: return null
        val protein = readPer100g(nutriments, PROTEIN_KEYS) ?: 0.0
        val carbs = readPer100g(nutriments, CARBS_KEYS) ?: 0.0
        val fat = readPer100g(nutriments, FAT_KEYS) ?: 0.0
        val fibre = readPer100g(nutriments, FIBRE_KEYS)

        val serving = product.servingQuantity.asDouble()?.takeIf { it > 0 } ?: 100.0

        return Ingredient(
            id = product.code?.takeIf { it.isNotBlank() } ?: "off:${name.hashCode()}",
            name = name.trim(),
            brand = product.brands?.split(',')?.firstOrNull()?.trim()?.takeIf { it.isNotBlank() },
            barcode = product.code,
            source = source,
            servingGrams = serving,
            kcalPer100g = kcal,
            proteinPer100g = protein,
            carbsPer100g = carbs,
            fatPer100g = fat,
            fibrePer100g = fibre,
        )
    }

    private fun readPer100g(nutriments: Map<String, kotlinx.serialization.json.JsonElement>, keys: List<String>): Double? {
        for (key in keys) {
            val v = nutriments[key].asDouble()
            if (v != null && v >= 0) return v
        }
        return null
    }

    private val ENERGY_KCAL_KEYS = listOf("energy-kcal_100g", "energy_kcal_100g", "energy-kcal", "energy_kcal")
    private val ENERGY_KJ_KEYS = listOf("energy-kj_100g", "energy_100g", "energy-kj", "energy")
    private val PROTEIN_KEYS = listOf("proteins_100g", "proteins")
    private val CARBS_KEYS = listOf("carbohydrates_100g", "carbohydrates")
    private val FAT_KEYS = listOf("fat_100g", "fat")
    private val FIBRE_KEYS = listOf("fiber_100g", "fiber")
}
