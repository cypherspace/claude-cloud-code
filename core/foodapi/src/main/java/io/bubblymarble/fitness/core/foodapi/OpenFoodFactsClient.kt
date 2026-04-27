package io.bubblymarble.fitness.core.foodapi

import io.bubblymarble.fitness.core.common.IoDispatcher
import io.bubblymarble.fitness.core.data.model.Ingredient
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request

/**
 * Thin client for the Open Food Facts public API. No auth required, but their docs ask
 * that requests carry a descriptive User-Agent so they can rate-limit abusive callers.
 *
 * Search:    https://world.openfoodfacts.org/cgi/search.pl?...
 * Barcode:   https://world.openfoodfacts.org/api/v2/product/<code>.json
 */
@Singleton
class OpenFoodFactsClient @Inject constructor(
    @IoDispatcher private val io: CoroutineDispatcher,
) {
    private val http = OkHttpClient.Builder().build()
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun lookupByBarcode(barcode: String): Ingredient? = withContext(io) {
        val req = request("$BASE_URL/api/v2/product/$barcode.json")
        http.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) return@withContext null
            val raw = resp.body?.string() ?: return@withContext null
            val parsed = runCatching { json.decodeFromString(OffProductResponse.serializer(), raw) }.getOrNull()
                ?: return@withContext null
            if (parsed.status != 1 || parsed.product == null) return@withContext null
            OpenFoodFactsMapper.toIngredient(parsed.product, source = "openfoodfacts:$barcode")
        }
    }

    suspend fun search(query: String, pageSize: Int = 20): List<Ingredient> = withContext(io) {
        if (query.isBlank()) return@withContext emptyList()
        val url = ("$BASE_URL/cgi/search.pl?search_terms=" + java.net.URLEncoder.encode(query, "UTF-8") +
            "&search_simple=1&action=process&json=1&page_size=$pageSize" +
            "&fields=code,product_name,brands,serving_quantity,nutriments")
        val req = request(url)
        http.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) return@withContext emptyList()
            val raw = resp.body?.string() ?: return@withContext emptyList()
            val parsed = runCatching { json.decodeFromString(OffSearchResponse.serializer(), raw) }.getOrNull()
                ?: return@withContext emptyList()
            parsed.products.orEmpty().mapNotNull { product ->
                OpenFoodFactsMapper.toIngredient(product, source = "openfoodfacts:search")
            }
        }
    }

    private fun request(url: String): Request = Request.Builder()
        .url(url)
        .header("User-Agent", USER_AGENT)
        .build()

    companion object {
        const val BASE_URL = "https://world.openfoodfacts.org"
        const val USER_AGENT = "Bubblymarble/0.1 (Android; personal use)"
    }
}
