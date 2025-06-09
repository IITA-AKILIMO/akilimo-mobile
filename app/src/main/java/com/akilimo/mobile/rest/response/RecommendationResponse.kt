package com.akilimo.mobile.rest.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RecommendationResponse(
    @Json(name = "recommendation") var recommendation: String? = null,
    @Json(name = "rec_type") var recType: String? = null,
)

@JsonClass(generateAdapter = true)
data class ApiErrorResponse(
    @Json(name = "error") val error: String,
    @Json(name = "status") val status: Int,
    @Json(name = "message") val message: String?
)
