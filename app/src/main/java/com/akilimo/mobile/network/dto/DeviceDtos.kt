package com.akilimo.mobile.network.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// Push devices: PUT / DELETE v1/devices (see the API's docs/MESSAGES_API.md)

/** Registers or refreshes this phone's Firebase token under its install id ([deviceToken]). */
@JsonClass(generateAdapter = true)
data class DeviceRegistrationRequest(
    /** The install id the app already sends with account and compute requests. */
    @param:Json(name = "device_token") val deviceToken: String,
    @param:Json(name = "fcm_token") val fcmToken: String,
    @param:Json(name = "platform") val platform: String = "android",
    @param:Json(name = "app_version") val appVersion: String?,
    @param:Json(name = "country") val country: String?,
    @param:Json(name = "language") val language: String?,
)

@JsonClass(generateAdapter = true)
data class DeviceUnregisterRequest(
    @param:Json(name = "device_token") val deviceToken: String,
)
