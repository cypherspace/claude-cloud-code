package io.bubblymarble.fitness.core.ai

import io.bubblymarble.fitness.core.common.IoDispatcher
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

@Singleton
class GeminiClient @Inject constructor(
    @IoDispatcher private val io: CoroutineDispatcher,
) {
    private val http = OkHttpClient.Builder().build()
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    /**
     * Calls the Gemini `generateContent` endpoint with [prompt] and a JSON [responseSchema].
     * Returns the raw JSON text emitted by the model. Throws on transport/HTTP failures or when
     * the model returns no candidates.
     */
    suspend fun generateJson(
        apiKey: String,
        model: String = DEFAULT_MODEL,
        prompt: String,
        responseSchema: JsonElement,
    ): String = withContext(io) {
        val body = GeminiRequest(
            contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt)))),
            generationConfig = GenerationConfig(
                responseMimeType = "application/json",
                responseSchema = responseSchema,
                temperature = 0.6f,
            ),
        )
        val payload = json.encodeToString(GeminiRequest.serializer(), body)
        val req = Request.Builder()
            .url("$BASE_URL/$model:generateContent?key=$apiKey")
            .post(payload.toRequestBody(JSON_MEDIA))
            .build()
        http.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) error("Gemini HTTP ${resp.code}: ${resp.body?.string().orEmpty()}")
            val raw = resp.body?.string().orEmpty()
            val parsed = json.decodeFromString(GeminiResponse.serializer(), raw)
            parsed.candidates.firstOrNull()
                ?.content?.parts?.firstOrNull()?.text
                ?: error("Gemini returned no candidates")
        }
    }

    companion object {
        const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"
        const val DEFAULT_MODEL = "gemini-2.0-flash"
        private val JSON_MEDIA = "application/json".toMediaType()
    }
}

@Serializable
private data class GeminiRequest(
    val contents: List<GeminiContent>,
    val generationConfig: GenerationConfig? = null,
)

@Serializable
private data class GeminiContent(val parts: List<GeminiPart>)

@Serializable
private data class GeminiPart(val text: String)

@Serializable
private data class GenerationConfig(
    val responseMimeType: String? = null,
    val responseSchema: JsonElement? = null,
    val temperature: Float? = null,
)

@Serializable
private data class GeminiResponse(val candidates: List<GeminiCandidate> = emptyList())

@Serializable
private data class GeminiCandidate(val content: GeminiContent? = null)
