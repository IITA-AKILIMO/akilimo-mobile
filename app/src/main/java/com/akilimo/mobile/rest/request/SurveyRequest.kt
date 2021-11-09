package com.akilimo.mobile.rest.request

import com.fasterxml.jackson.annotation.JsonProperty

class SurveyRequest(
    @JsonProperty("deviceToken")
    val deviceToken: String,

    @JsonProperty("akilimoUsage")
    val akilimoUsage: String,

    @JsonProperty("akilimoRecRating")
    val akilimoRecRating: Int,

    @JsonProperty("akilimoUsefulRating")
    val akilimoUsefulRating: Int,

    @JsonProperty("language")
    val language: String
)
