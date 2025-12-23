package com.akilimo.mobile.dto

import com.akilimo.mobile.entities.MaizePrice
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class MaizePriceResponse(
    @param:Json(name = "data") val data: List<MaizePriceDto>,
    @param:Json(name = "links") val links: PaginationLinks?,
    @param:Json(name = "meta") val meta: PaginationMeta?
)

@JsonClass(generateAdapter = true)
data class MaizePriceDto(
    @param:Json(name = "id") val id: Int,
    @param:Json(name = "country_code") val countryCode: String,
    @param:Json(name = "produce_type") val produceType: String,
    @param:Json(name = "min_local_price") val minLocalPrice: Int,
    @param:Json(name = "max_local_price") val maxLocalPrice: Int,
    @param:Json(name = "average_price") val averagePrice: Int,
    @param:Json(name = "exact_price") val exactPrice: Boolean,
    @param:Json(name = "item_tag") val itemTag: String,
    @param:Json(name = "min_allowed_price") val minAllowedPrice: Int,
    @param:Json(name = "max_allowed_price") val maxAllowedPrice: Int,
    @param:Json(name = "active") val active: Boolean,
    @param:Json(name = "sort_order") val sortOrder: Int
) {
    fun toEntity() = MaizePrice(
        id = this.id,
        countryCode = this.countryCode,
        produceType = this.produceType,
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