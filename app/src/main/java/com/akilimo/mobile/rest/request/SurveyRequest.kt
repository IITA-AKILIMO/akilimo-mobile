package com.akilimo.mobile.rest.request

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SurveyRequest(
    @Json(name = "device_token")
    val deviceToken: String,

    @Json(name = "akilimo_usage")
    val akilimoUsage: String,

    @Json(name = "akilimo_rec_rating")
    val akilimoRecRating: Int,

    @Json(name = "akilimo_useful_rating")
    val akilimoUsefulRating: Int,

    @Json(name = "language")
    val language: String
)
