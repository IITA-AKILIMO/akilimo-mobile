package com.akilimo.mobile.network.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RecommendationResponse(
    @param:Json(name = "request_id")
    val requestId: String,

    @param:Json(name = "status")
    val status: String,

    @param:Json(name = "version")
    val version: String,

    @param:Json(name = "data")
    val data: RecommendationData
)

@JsonClass(generateAdapter = true)
data class RecommendationData(
    @param:Json(name = "rec_type")
    val recType: String,

    @param:Json(name = "recommendation")
    val recommendation: String,
)


@JsonClass(generateAdapter = true)
data class ApiErrorResponse(
    @param:Json(name = "error") val error: String,
    @param:Json(name = "status") val status: Int,
    @param:Json(name = "message") val message: String?
)
