package com.akilimo.mobile.rest.request

import com.fasterxml.jackson.annotation.JsonProperty

data class SurveyRequest(
    @JsonProperty("device_token")
    val deviceToken: String,

    @JsonProperty("akilimo_usage")
    val akilimoUsage: String,

    @JsonProperty("akilimo_rec_rating")
    val akilimoRecRating: Int,

    @JsonProperty("akilimo_useful_rating")
    val akilimoUsefulRating: Int,

    @JsonProperty("language")
    val language: String
)
