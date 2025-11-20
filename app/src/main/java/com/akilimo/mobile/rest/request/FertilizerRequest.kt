package com.akilimo.mobile.rest.request

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FertilizerRequest(

    @param:Json(name = "name")
    val name: String,

    @param:Json(name = "key")
    val key: String,

    @param:Json(name = "fertilizer_type")
    val fertilizerType: String,

    @param:Json(name = "weight")
    val weight: Double = 0.0,

    @param:Json(name = "price")
    val price: Double = 0.0,

    @param:Json(name = "selected")
    val selected: Boolean = false
)
