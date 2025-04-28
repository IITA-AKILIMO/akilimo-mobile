package com.akilimo.mobile.data

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class RemoteConfigResponse(
    @JsonProperty("app_name")
    val appName: String,
    @JsonProperty("config_name")
    val configName: String,
    @JsonProperty("config_value")
    val configValue: String
)
