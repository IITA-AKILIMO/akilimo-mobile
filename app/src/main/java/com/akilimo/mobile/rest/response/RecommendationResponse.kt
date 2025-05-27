package com.akilimo.mobile.rest.response

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class RecommendationResponse(
    @JsonProperty("recommendation") var recommendation: String? = null,
    @JsonProperty("rec_type") var recType: String? = null,
)


@JsonIgnoreProperties(ignoreUnknown = true)
data class ApiErrorResponse(
    @JsonProperty("error") val error: String,
    @JsonProperty("status") val status: Int,
    @JsonProperty("message") val message: String?
)