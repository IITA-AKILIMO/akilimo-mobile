package com.akilimo.mobile.rest.response

import com.squareup.moshi.Json

data class ReverseGeocodeResponse(
    @param:Json(name = "address")
    val address: Address
)

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
