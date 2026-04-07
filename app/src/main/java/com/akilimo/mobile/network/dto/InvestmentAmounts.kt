package com.akilimo.mobile.network.dto

import com.akilimo.mobile.entities.InvestmentAmount
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class InvestmentAmounts(
    @param:Json(name = "id") val id: Int,
    @param:Json(name = "investment_amount") val investmentAmount: Double,
    @param:Json(name = "country_code") val countryCode: String,
    @param:Json(name = "currency") val currency: Currencies? = null,
    @param:Json(name = "exact_amount") val exactAmount: Boolean,
    @param:Json(name = "active") val active: Boolean,
    @param:Json(name = "area_unit") val areaUnit: String,
    @param:Json(name = "sort_order") val sortOrder: Int
) {
    fun toEntity(): InvestmentAmount = InvestmentAmount(
        id = id,
        investmentAmount = investmentAmount,
        countryCode = countryCode,
        exactAmount = exactAmount,
        active = active,
        areaUnit = areaUnit,
        sortOrder = sortOrder,
        currencySymbol = currency?.currencySymbol.orEmpty(),
        currencyCode = currency?.currencyCode.orEmpty()
    )
}