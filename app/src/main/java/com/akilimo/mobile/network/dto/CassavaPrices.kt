@file:Suppress("MatchingDeclarationName") // filename predates Dto suffix convention; rename tracked as follow-up
package com.akilimo.mobile.network.dto

import com.akilimo.mobile.entities.CassavaMarketPrice
import com.akilimo.mobile.enums.EnumCountry
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CassavaPriceDto(
    @param:Json(name = "id") val id: Int,
    @param:Json(name = "country_code") val countryCode: String,
    @param:Json(name = "currency") val currency: Currencies? = null,
    @param:Json(name = "average_price") val averagePrice: Double,
    @param:Json(name = "exact_price") val exactPrice: Boolean,
    @param:Json(name = "item_tag") val itemTag: String,
    @param:Json(name = "active") val active: Boolean,
    @param:Json(name = "sort_order") val sortOrder: Int
) {
    fun toEntity() = CassavaMarketPrice(
        id = this.id,
        countryCode = EnumCountry.fromCode(this.countryCode),
        currencyCode = currency?.currencyCode.orEmpty(),
        currencySymbol = currency?.currencySymbol.orEmpty(),
        averagePrice = this.averagePrice,
        exactPrice = this.exactPrice,
        itemTag = this.itemTag,
        active = this.active,
        sortOrder = this.sortOrder
    )
}
