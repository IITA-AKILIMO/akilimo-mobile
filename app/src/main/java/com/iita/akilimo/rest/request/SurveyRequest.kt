package com.iita.akilimo.rest.request

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder

class SurveyRequest(
    @JsonProperty("akilimoUsage")
    deviceToken: String,
    @JsonProperty("akilimoUsage")
    val akilimoUsage: String,

    @JsonProperty("akilimoRecRating")
    val akilimoRecRating: Int,

    @JsonProperty("akilimoUsefulRating")
    val akilimoUsefulRating: Int,

    @JsonProperty("language")
    val language: String
)