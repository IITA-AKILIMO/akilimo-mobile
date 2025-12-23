package com.akilimo.mobile.dto

import com.akilimo.mobile.entities.FertilizerPrice
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FertilizerPriceResponse(
    @param:Json(name = "data") val data: List<FertilizerPriceDto>,
    @param:Json(name = "links") val links: PaginationLinks?,
    @param:Json(name = "meta") val meta: PaginationMeta?
)

@JsonClass(generateAdapter = true)
data class FertilizerPriceDto(
    val id: Int,
    @param:Json(name = "price_id") val priceId: Int,
    @param:Json(name = "fertilizer_key") val fertilizerKey: String,
    @param:Json(name = "fertilizer_country") val fertilizerCountry: String,
    @param:Json(name = "country_code") val countryCode: String,
    @param:Json(name = "currency") val currency: CurrencyDto? = null,
    @param:Json(name = "sort_order") val sortOrder: Int,
    @param:Json(name = "min_local_price") val minLocalPrice: Double,
    @param:Json(name = "max_local_price") val maxLocalPrice: Double,
    @param:Json(name = "min_allowed_price") val minAllowedPrice: Double,
    @param:Json(name = "max_allowed_price") val maxAllowedPrice: Double,
    @param:Json(name = "price_per_bag") val pricePerBag: Double,
    @param:Json(name = "price_range") val priceRange: String,
    @param:Json(name = "active") val isActive: Boolean,
    @param:Json(name = "description") val description: String?,
    @param:Json(name = "created_at") val createdAt: String?,
    @param:Json(name = "updated_at") val updatedAt: String?
) {
    fun toEntity() = FertilizerPrice(
        id = id,
        fertilizerKey = fertilizerKey,
        fertilizerCountry = fertilizerCountry,
        countryCode = countryCode,
        currencyCode = currency?.currencyCode.orEmpty(),
        currencySymbol = currency?.currencySymbol.orEmpty(),
        sortOrder = sortOrder,
        minLocalPrice = minLocalPrice,
        maxLocalPrice = maxLocalPrice,
        minAllowedPrice = minAllowedPrice,
        maxAllowedPrice = maxAllowedPrice,
        pricePerBag = pricePerBag,
        priceRange = priceRange,
        isActive = isActive,
        description = description
    )
}