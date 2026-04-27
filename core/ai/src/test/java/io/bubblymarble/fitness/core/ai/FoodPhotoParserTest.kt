package io.bubblymarble.fitness.core.ai

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class FoodPhotoParserTest {

    @Test fun parsesValidEnvelope() {
        val raw = """
            {
              "items": [
                {"name": "Chicken breast", "grams": 150, "kcal": 240, "proteinG": 45, "carbsG": 0, "fatG": 6},
                {"name": "Brown rice", "grams": 200, "kcal": 220, "proteinG": 5, "carbsG": 46, "fatG": 2}
              ]
            }
        """.trimIndent()
        val result = FoodPhotoParser.parse(raw)
        assertThat(result).hasSize(2)
        assertThat(result[0].name).isEqualTo("Chicken breast")
        assertThat(result[1].grams).isEqualTo(200.0)
    }

    @Test fun dropsBlankAndZeroPortionEntries() {
        val raw = """
            {
              "items": [
                {"name": "", "grams": 100, "kcal": 100, "proteinG": 0, "carbsG": 0, "fatG": 0},
                {"name": "Salad", "grams": 0, "kcal": 50, "proteinG": 1, "carbsG": 5, "fatG": 1},
                {"name": "Apple", "grams": 180, "kcal": 95, "proteinG": 0, "carbsG": 25, "fatG": 0}
              ]
            }
        """.trimIndent()
        val result = FoodPhotoParser.parse(raw)
        assertThat(result.map { it.name }).containsExactly("Apple")
    }

    @Test fun garbageInput_returnsEmpty() {
        assertThat(FoodPhotoParser.parse("not json")).isEmpty()
        assertThat(FoodPhotoParser.parse("")).isEmpty()
        assertThat(FoodPhotoParser.parse("{}")).isEmpty()
    }

    @Test fun missingItemsField_returnsEmpty() {
        assertThat(FoodPhotoParser.parse("""{"foo": []}""")).isEmpty()
    }
}
