package com.akilimo.mobile.config

import android.content.Context
import com.akilimo.mobile.BuildConfig
import com.akilimo.mobile.enums.EnumServiceType
import com.akilimo.mobile.helper.SessionManager

object AppConfig {
    private fun normalize(raw: String): String {
        val trimmed = raw.trim().removeSurrounding("\"")
        return if (trimmed.endsWith("/")) trimmed else "$trimmed/"
    }

    // Resolve order: 1) SessionManager persisted value, 2) BuildConfig fallback (guaranteed)
    fun resolveBaseUrlFor(context: Context, service: EnumServiceType): String {
        val session = SessionManager(context)
        val saved = when (service) {
            EnumServiceType.AKILIMO -> session.akilimoEndpoint
            EnumServiceType.FUELROD -> session.fuelrodEndpoint
        }.takeIf { it.isNotBlank() }

        if (!saved.isNullOrBlank()) return normalize(saved)

        val buildVal = when (service) {
            EnumServiceType.AKILIMO -> BuildConfig.AKILIMO_BASE_URL
            EnumServiceType.FUELROD -> BuildConfig.FUELROD_BASE_URL
        }

        return normalize(buildVal)
    }

    fun persistBaseUrlFor(context: Context, service: EnumServiceType, url: String) {
        val session = SessionManager(context)
        val n = normalize(url)
        when (service) {
            EnumServiceType.AKILIMO -> session.akilimoEndpoint = n
            EnumServiceType.FUELROD -> session.fuelrodEndpoint = n
        }
    }

    fun clearPersistedBaseUrlFor(context: Context, service: EnumServiceType) {
        val session = SessionManager(context)
        when (service) {
            EnumServiceType.AKILIMO -> session.akilimoEndpoint = BuildConfig.AKILIMO_BASE_URL
            EnumServiceType.FUELROD -> session.fuelrodEndpoint = BuildConfig.FUELROD_BASE_URL
        }
    }
}

