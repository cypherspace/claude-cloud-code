package io.bubblymarble.fitness.core.foodapi

import com.google.common.truth.Truth.assertThat
import kotlinx.serialization.json.Json
import org.junit.Test

class OpenFoodFactsMapperTest {
    private val json = Json { ignoreUnknownKeys = true }

    private fun product(raw: String): OffProduct =
        json.decodeFromString(OffProduct.serializer(), raw)

    @Test fun mapsKcalDirectly() {
        val p = product(
            """
            {
              "code": "12345",
              "product_name": "Test bar",
              "brands": "TestCo",
              "serving_quantity": 40,
              "nutriments": {
                "energy-kcal_100g": 412,
                "proteins_100g": 22.0,
                "carbohydrates_100g": 38.0,
                "fat_100g": 18.0,
                "fiber_100g": 4.0
              }
            }
            """.trimIndent()
        )
        val ing = OpenFoodFactsMapper.toIngredient(p, "test")!!
        assertThat(ing.id).isEqualTo("12345")
        assertThat(ing.name).isEqualTo("Test bar")
        assertThat(ing.brand).isEqualTo("TestCo")
        assertThat(ing.servingGrams).isEqualTo(40.0)
        assertThat(ing.kcalPer100g).isEqualTo(412.0)
        assertThat(ing.proteinPer100g).isEqualTo(22.0)
        assertThat(ing.fibrePer100g).isEqualTo(4.0)
    }

    @Test fun fallsBackToKilojoulesAndConverts() {
        val p = product(
            """
            {
              "code": "9", "product_name": "Energy",
              "nutriments": { "energy-kj_100g": 1000.0, "proteins_100g": 0, "carbohydrates_100g": 0, "fat_100g": 0 }
            }
            """.trimIndent()
        )
        val ing = OpenFoodFactsMapper.toIngredient(p, "test")!!
        assertThat(ing.kcalPer100g).isWithin(0.5).of(239.0)
    }

    @Test fun rejectsProductWithoutEnergy() {
        val p = product(
            """
            {
              "code": "x", "product_name": "Mystery",
              "nutriments": { "proteins_100g": 5 }
            }
            """.trimIndent()
        )
        assertThat(OpenFoodFactsMapper.toIngredient(p, "test")).isNull()
    }

    @Test fun rejectsProductWithoutName() {
        val p = product(
            """
            { "code": "x", "nutriments": { "energy-kcal_100g": 100 } }
            """.trimIndent()
        )
        assertThat(OpenFoodFactsMapper.toIngredient(p, "test")).isNull()
    }

    @Test fun usesEnglishNameWhenAvailable() {
        val p = product(
            """
            {
              "code": "1", "product_name": "Fromage", "product_name_en": "Cheese",
              "nutriments": { "energy-kcal_100g": 350 }
            }
            """.trimIndent()
        )
        val ing = OpenFoodFactsMapper.toIngredient(p, "test")!!
        assertThat(ing.name).isEqualTo("Cheese")
    }

    @Test fun handlesStringServingQuantity() {
        val p = product(
            """
            {
              "code": "1", "product_name": "X", "serving_quantity": "30",
              "nutriments": { "energy-kcal_100g": 200 }
            }
            """.trimIndent()
        )
        val ing = OpenFoodFactsMapper.toIngredient(p, "test")!!
        assertThat(ing.servingGrams).isEqualTo(30.0)
    }
}
