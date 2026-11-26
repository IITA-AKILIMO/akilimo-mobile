package com.akilimo.mobile.network.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/** `GET v1/app/version`: the release gate per platform, keyed by platform name (`android`). */
@JsonClass(generateAdapter = true)
data class AppVersionResponse(
    @param:Json(name = "data") val data: Map<String, PlatformVersionDto> = emptyMap(),
)

/** What a platform must run: below [minSupported] the app must update, below [latest] it is only offered. */
@JsonClass(generateAdapter = true)
data class PlatformVersionDto(
    @param:Json(name = "min_supported") val minSupported: Int = 1,
    @param:Json(name = "latest") val latest: Int = 1,
    @param:Json(name = "message") val message: String? = null,
    @param:Json(name = "store_url") val storeUrl: String? = null,
)
