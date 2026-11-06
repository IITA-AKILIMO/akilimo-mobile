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


/**
 * Error body of the AKILIMO API. Laravel validation errors (422) carry only `message` and
 * `errors` (field → messages), so every field is optional.
 */
@JsonClass(generateAdapter = true)
data class ApiErrorResponse(
    @param:Json(name = "error") val error: String? = null,
    @param:Json(name = "status") val status: Int? = null,
    @param:Json(name = "message") val message: String? = null,
    @param:Json(name = "errors") val errors: Map<String, List<String>>? = null,
) {
    /** True when the API refused the farm location as outside the supported area. */
    val isOutsideCoverage: Boolean
        get() = errors.orEmpty().keys.any { it.endsWith("farmInformation.map_lat") }
}
