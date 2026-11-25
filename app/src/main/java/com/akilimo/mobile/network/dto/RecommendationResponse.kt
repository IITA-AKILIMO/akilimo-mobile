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
    val data: RecommendationData,

    /** Signed PDF download links, or null when the API made no report for this request. */
    @param:Json(name = "report")
    val report: ReportDto? = null,
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
    /** Machine-readable reason of the account endpoints, e.g. `consent_required` or `email_unverified`. */
    @param:Json(name = "code") val code: String? = null,
) {
    /**
     * True when the API refused the farm location as outside the supported area: either with the machine code
     * `outside_coverage` (HTTP 400), or as a validation error on the farm's latitude (HTTP 422).
     */
    val isOutsideCoverage: Boolean
        get() = code == OUTSIDE_COVERAGE_CODE ||
            errors.orEmpty().keys.any { it.endsWith("farmInformation.map_lat") }

    companion object {
        const val OUTSIDE_COVERAGE_CODE = "outside_coverage"
    }
}
