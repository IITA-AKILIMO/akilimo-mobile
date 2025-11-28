package com.akilimo.mobile.dto

import com.akilimo.mobile.entities.CassavaMarketPrice
import com.akilimo.mobile.enums.EnumCountry
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CassavaPriceResponse(
    @param:Json(name = "data") val data: List<CassavaPriceDto>,
    @param:Json(name = "links") val links: PaginationLinks?,
    @param:Json(name = "meta") val meta: PaginationMeta?
)

@JsonClass(generateAdapter = true)
data class CassavaPriceDto(
    @param:Json(name = "id") val id: Int,
    @param:Json(name = "country_code") val countryCode: String,
    @param:Json(name = "currency") val currency: CurrencyDto? = null,
    @param:Json(name = "min_local_price") val minLocalPrice: Double,
    @param:Json(name = "max_local_price") val maxLocalPrice: Double,
    @param:Json(name = "average_price") val averagePrice: Double,
    @param:Json(name = "exact_price") val exactPrice: Boolean,
    @param:Json(name = "item_tag") val itemTag: String,
    @param:Json(name = "min_allowed_price") val minAllowedPrice: Double,
    @param:Json(name = "max_allowed_price") val maxAllowedPrice: Double,
    @param:Json(name = "active") val active: Boolean,
    @param:Json(name = "sort_order") val sortOrder: Int
) {
    fun toEntity() = CassavaMarketPrice(
        id = this.id,
        countryCode = EnumCountry.fromCode(this.countryCode),
        currencyCode = currency?.currencyCode.orEmpty(),
        currencySymbol = currency?.currencySymbol.orEmpty(),
        minLocalPrice = this.minLocalPrice,
        maxLocalPrice = this.maxLocalPrice,
        averagePrice = this.averagePrice,
        exactPrice = this.exactPrice,
        itemTag = this.itemTag,
        minAllowedPrice = this.minAllowedPrice,
        maxAllowedPrice = this.maxAllowedPrice,
        active = this.active,
        sortOrder = this.sortOrder
    )
}