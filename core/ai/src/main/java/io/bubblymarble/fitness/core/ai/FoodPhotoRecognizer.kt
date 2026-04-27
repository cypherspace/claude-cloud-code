package io.bubblymarble.fitness.core.ai

import io.bubblymarble.fitness.core.data.prefs.SecurePrefs
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Suggestion returned by the photo recognizer. Nutrition is per the [grams] portion (not per 100g)
 * because the model is making a portion-aware estimate from a single picture.
 */
@Serializable
data class PhotoFoodSuggestion(
    val name: String,
    val grams: Double,
    val kcal: Double,
    val proteinG: Double,
    val carbsG: Double,
    val fatG: Double,
)

@Singleton
class FoodPhotoRecognizer @Inject constructor(
    private val gemini: GeminiClient,
    private val securePrefs: SecurePrefs,
) {

    /** Returns the recognizer's best-effort breakdown of foods visible in [imageBytes]. */
    suspend fun recognize(imageBytes: ByteArray, mimeType: String = "image/jpeg"): List<PhotoFoodSuggestion> {
        val key = securePrefs.geminiKey() ?: return emptyList()
        val raw = runCatching {
            gemini.generateJsonWithImage(
                apiKey = key,
                prompt = PROMPT,
                imageBytes = imageBytes,
                imageMimeType = mimeType,
                responseSchema = schema(),
            )
        }.getOrElse { return emptyList() }
        return FoodPhotoParser.parse(raw)
    }

    private fun schema(): JsonObject = buildJsonObject {
        put("type", "object")
        put("properties", buildJsonObject {
            put("items", buildJsonObject {
                put("type", "array")
                put("items", buildJsonObject {
                    put("type", "object")
                    put("properties", buildJsonObject {
                        put("name", buildJsonObject { put("type", "string") })
                        put("grams", buildJsonObject { put("type", "number") })
                        put("kcal", buildJsonObject { put("type", "number") })
                        put("proteinG", buildJsonObject { put("type", "number") })
                        put("carbsG", buildJsonObject { put("type", "number") })
                        put("fatG", buildJsonObject { put("type", "number") })
                    })
                    put("required", buildJsonArray {
                        add(JsonPrimitive("name"))
                        add(JsonPrimitive("grams"))
                        add(JsonPrimitive("kcal"))
                    })
                })
            })
        })
        put("required", buildJsonArray { add(JsonPrimitive("items")) })
    }

    companion object {
        private const val PROMPT = """
You are a nutrition assistant. Analyse the food in this photo. Identify each distinct food item,
estimate the portion size in grams, and estimate calories and macros for that portion.
Be conservative: prefer reasonable averages over precision claims. Return JSON only.
"""
    }
}
