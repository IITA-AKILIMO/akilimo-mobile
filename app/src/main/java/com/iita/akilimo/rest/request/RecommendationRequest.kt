package com.iita.akilimo.rest.request

import com.fasterxml.jackson.annotation.JsonProperty
import com.iita.akilimo.entities.Fertilizer
import com.iita.akilimo.models.ComputeRequest

class RecommendationRequest(
    @JsonProperty("userInfo")
    var userInfo: UserInfo,

    @JsonProperty("computeRequest")
    var computeRequest: ComputeRequest,

    @JsonProperty("fertilizerList")
    var fertilizerList: List<Fertilizer>
)
