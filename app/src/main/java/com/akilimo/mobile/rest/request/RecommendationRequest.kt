package com.akilimo.mobile.rest.request

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class RecommendationRequest(
    @Json(name = "user_info")
    val userInfo: UserInfo,
    @Json(name = "compute_request")
    val computeRequest: ComputeRequest,
    @Json(name = "fertilizer_list")
    val fertilizerList: List<FertilizerRequest>
)
