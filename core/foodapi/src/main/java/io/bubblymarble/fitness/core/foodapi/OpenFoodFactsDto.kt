package io.bubblymarble.fitness.core.foodapi

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.doubleOrNull

@Serializable
internal data class OffProductResponse(
    val status: Int = 0,
    val code: String? = null,
    val product: OffProduct? = null,
)

@Serializable
internal data class OffSearchResponse(
    val count: Int = 0,
    val page: Int = 0,
    @SerialName("page_size") val pageSize: Int = 0,
    val products: List<OffProduct>? = null,
)

@Serializable
internal data class OffProduct(
    val code: String? = null,
    @SerialName("product_name") val productName: String? = null,
    @SerialName("product_name_en") val productNameEn: String? = null,
    val brands: String? = null,
    @SerialName("serving_quantity") val servingQuantity: JsonElement? = null,
    val nutriments: Map<String, JsonElement>? = null,
)

internal fun JsonElement?.asDouble(): Double? = when (this) {
    null, JsonNull -> null
    is JsonPrimitive -> doubleOrNull
    else -> null
}
