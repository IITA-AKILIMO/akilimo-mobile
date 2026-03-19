package com.akilimo.mobile.config

import android.content.Context
import com.akilimo.mobile.BuildConfig
import com.akilimo.mobile.data.AppSettingsDataStore
import com.akilimo.mobile.enums.EnumServiceType

object AppConfig {
    private fun normalize(raw: String): String {
        val trimmed = raw.trim().removeSurrounding("\"")
        return if (trimmed.endsWith("/")) trimmed else "$trimmed/"
    }

    // Resolve order: 1) DataStore persisted value, 2) BuildConfig fallback (guaranteed)
    fun resolveBaseUrlFor(context: Context, service: EnumServiceType): String {
        val saved = AppSettingsDataStore.readEndpointSync(context, service)
        if (saved.isNotBlank()) return normalize(saved)
        val buildVal = when (service) {
            EnumServiceType.AKILIMO -> BuildConfig.AKILIMO_BASE_URL
            EnumServiceType.FUELROD -> BuildConfig.FUELROD_BASE_URL
        }
        return normalize(buildVal)
    }

    fun persistBaseUrlFor(context: Context, service: EnumServiceType, url: String) {
        AppSettingsDataStore.writeEndpointSync(context, service, normalize(url))
    }

    fun clearPersistedBaseUrlFor(context: Context, service: EnumServiceType) {
        val default = when (service) {
            EnumServiceType.AKILIMO -> BuildConfig.AKILIMO_BASE_URL
            EnumServiceType.FUELROD -> BuildConfig.FUELROD_BASE_URL
        }
        AppSettingsDataStore.writeEndpointSync(context, service, default)
    }
}
