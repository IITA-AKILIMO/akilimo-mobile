package com.akilimo.mobile.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CurrencyDto(
    @param:Json(name = "currency_name")
    val currencyName: String,

    @param:Json(name = "currency_code")
    val currencyCode: String,

    @param:Json(name = "currency_symbol")
    val currencySymbol: String,

    @param:Json(name = "country_code")
    val countryCode: String? = null,
)