package io.bubblymarble.fitness.core.data.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class NutritionTotalsTest {

    private val rolledOats = Ingredient(
        id = "oats", name = "Rolled oats", brand = null, barcode = null,
        source = "test", servingGrams = 40.0,
        kcalPer100g = 379.0, proteinPer100g = 13.0, carbsPer100g = 67.0, fatPer100g = 7.0,
        fibrePer100g = 10.0,
    )

    @Test fun ofComputes_proportionalToGrams() {
        val totals = NutritionTotals.of(rolledOats, grams = 50.0)
        assertThat(totals.kcal).isWithin(0.001).of(189.5)
        assertThat(totals.proteinG).isWithin(0.001).of(6.5)
        assertThat(totals.carbsG).isWithin(0.001).of(33.5)
        assertThat(totals.fatG).isWithin(0.001).of(3.5)
        assertThat(totals.fibreG).isWithin(0.001).of(5.0)
    }

    @Test fun zeroGrams_yieldsZeroTotals() {
        val totals = NutritionTotals.of(rolledOats, grams = 0.0)
        assertThat(totals).isEqualTo(NutritionTotals.ZERO)
    }

    @Test fun plus_aggregates() {
        val a = NutritionTotals(100.0, 10.0, 20.0, 5.0, 3.0)
        val b = NutritionTotals(50.0, 4.0, 6.0, 2.0, 1.0)
        val sum = a + b
        assertThat(sum.kcal).isEqualTo(150.0)
        assertThat(sum.proteinG).isEqualTo(14.0)
        assertThat(sum.carbsG).isEqualTo(26.0)
        assertThat(sum.fatG).isEqualTo(7.0)
        assertThat(sum.fibreG).isEqualTo(4.0)
    }

    @Test fun nullFibre_treatedAsZero() {
        val noFibre = rolledOats.copy(fibrePer100g = null)
        val totals = NutritionTotals.of(noFibre, grams = 100.0)
        assertThat(totals.fibreG).isEqualTo(0.0)
    }
}
