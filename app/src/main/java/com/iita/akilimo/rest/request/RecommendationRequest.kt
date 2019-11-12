package com.iita.akilimo.rest.request

import com.fasterxml.jackson.annotation.JsonProperty
import com.iita.akilimo.entities.ComputeRequest
import com.iita.akilimo.models.Fertilizer

class RecommendationRequest(
    @JsonProperty("computeRequest")
    var computeRequest: ComputeRequest,
    @JsonProperty("fertilizerList")
    var fertilizerList: List<Fertilizer>
)
