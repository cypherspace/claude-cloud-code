package io.bubblymarble.fitness.core.ai

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Parses the JSON envelope returned by Gemini Vision for food-photo recognition.
 * Filters out blank or zero-portion suggestions so the UI only shows usable rows.
 */
internal object FoodPhotoParser {
    private val json = Json { ignoreUnknownKeys = true }

    fun parse(raw: String): List<PhotoFoodSuggestion> {
        val parsed = runCatching { json.decodeFromString(Envelope.serializer(), raw) }.getOrNull()
            ?: return emptyList()
        return parsed.items.filter { it.name.isNotBlank() && it.grams > 0 }
    }

    @Serializable
    private data class Envelope(val items: List<PhotoFoodSuggestion> = emptyList())
}
