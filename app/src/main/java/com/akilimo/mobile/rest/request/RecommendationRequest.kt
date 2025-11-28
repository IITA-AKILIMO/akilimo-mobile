package com.akilimo.mobile.rest.request

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Top-level payload sent to the backend.
 */
@JsonClass(generateAdapter = true)
data class RecommendationRequest(
    @param:Json(name = "user_info")
    val userInfo: UserInfo,
    @param:Json(name = "compute_request")
    val computeRequest: ComputeRequest,
    @param:Json(name = "fertilizer_list")
    val fertilizerList: List<FertilizerRequest>
)