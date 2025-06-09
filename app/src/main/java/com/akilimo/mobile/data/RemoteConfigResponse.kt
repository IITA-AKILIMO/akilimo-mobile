package com.akilimo.mobile.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RemoteConfigResponse(
    @field:Json(name = "app_name")
    val appName: String,
    @field:Json(name = "config_name")
    val configName: String,
    @field:Json(name = "config_value")
    val configValue: String
)
