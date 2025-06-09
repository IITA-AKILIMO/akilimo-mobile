package com.akilimo.mobile.rest.request

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class FertilizerRequest {

    @Json(name = "name")
    var name: String = "NA"

    @Json(name = "key")
    var key: String = "NA"

    @Json(name = "fertilizer_type")
    var fertilizerType: String = "NA"

    @Json(name = "weight")
    var weight: Double = 0.0

    @Json(name = "price")
    var price: Double = 0.0

    @Json(name = "selected")
    var selected: Boolean = false
}
