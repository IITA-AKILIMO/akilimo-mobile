package com.akilimo.mobile.rest.request

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
class RecommendationRequest(
    @JsonProperty("user_info")
    val userInfo: UserInfo,
    @JsonProperty("compute_request")
    val computeRequest: ComputeRequest,
    @JsonProperty("fertilizer_list")
    val fertilizerList: List<FertilizerRequest>
)
