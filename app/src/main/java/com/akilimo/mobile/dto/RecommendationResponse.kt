package com.akilimo.mobile.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RecommendationResponse(
    @param:Json(name = "recommendation") var recommendation: String? = null,
    @param:Json(name = "rec_type") var recType: String? = null,
)

@JsonClass(generateAdapter = true)
data class ApiErrorResponse(
    @param:Json(name = "error") val error: String,
    @param:Json(name = "status") val status: Int,
    @param:Json(name = "message") val message: String?
)
