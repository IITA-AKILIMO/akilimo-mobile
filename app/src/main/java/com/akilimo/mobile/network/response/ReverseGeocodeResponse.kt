package com.akilimo.mobile.network.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ReverseGeocodeResponse(
    @param:Json(name = "address")
    val address: Address
)

@JsonClass(generateAdapter = true)
data class Address(
    @param:Json(name = "road")
    val road: String? = null,

    @param:Json(name = "suburb")
    val suburb: String? = null,

    @param:Json(name = "city")
    val city: String? = null,

    @param:Json(name = "state")
    val state: String? = null,

    @param:Json(name = "country")
    val country: String? = null
)
