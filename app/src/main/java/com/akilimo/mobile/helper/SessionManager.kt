package com.akilimo.mobile.helper

import android.content.Context
import androidx.core.content.edit
import com.akilimo.mobile.BuildConfig
import com.akilimo.mobile.utils.DateHelper
import io.sentry.Sentry
import java.util.UUID

class SessionManager(context: Context) {

    private val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "new-akilimo-config"

        private const val KEY_DEFAULT_USER = "userName"
        private const val KEY_API_RESOURCE = "apiResource"
        private const val KEY_FUELROD_RESOURCE = "fuelrodResource"
        private const val KEY_API_TOKEN = "apiToken"
        private const val KEY_API_REFRESH = "apiRefreshToken"
        private const val KEY_MAPBOX = "mapBoxKey"
        private const val KEY_LOCATION_IQ = "locationIqToken"
        private const val KEY_FIRST_RUN = "firstRun"
        private const val KEY_NOTIFICATION_COUNT = "notificationCount"
        private const val KEY_DEVICE_TOKEN = "deviceToken"
        private const val KEY_TERMS_ACCEPTED = "termsAccepted"
        private const val KEY_TERMS_LINK = "termsLink"
        private const val KEY_DISCLAIMER_READ = "disclaimerRead"

        private const val DEFAULT_USER = "akilimo_user"
        private const val DEFAULT_MAPBOX_KEY =
            "pk.eyJ1IjoibWFzZ2VlayIsImEiOiJjanp0bm43ZmwwNm9jM29udjJod3V6dzB1In0.MevkJtANWZ8Wl9abnLu1Uw"
        private const val DEFAULT_TERMS_URL =
            "https://akilimo.org/index.php/akilimo-privacy-policy"
    }

    var akilimoUser: String
        get() = pref.getString(KEY_DEFAULT_USER, DEFAULT_USER).orEmpty()
        set(value) = pref.edit { putString(KEY_DEFAULT_USER, value) }

    var akilimoEndpoint: String
        get() = pref.getString(KEY_API_RESOURCE, null).orEmpty()
        set(value) = pref.edit { putString(KEY_API_RESOURCE, value) }

    var fuelrodEndpoint: String
        get() = pref.getString(KEY_FUELROD_RESOURCE, null).orEmpty()
        set(value) = pref.edit { putString(KEY_FUELROD_RESOURCE, value) }

    var apiToken: String
        get() = pref.getString(KEY_API_TOKEN, "").orEmpty()
        set(value) = pref.edit { putString(KEY_API_TOKEN, value) }

    var apiRefreshToken: String
        get() = pref.getString(KEY_API_REFRESH, "").orEmpty()
        set(value) = pref.edit { putString(KEY_API_REFRESH, value) }

    var mapBoxApiKey: String
        get() = pref.getString(KEY_MAPBOX, DEFAULT_MAPBOX_KEY).orEmpty()
        set(value) = pref.edit { putString(KEY_MAPBOX, value) }

    var locationIqToken: String
        get() = pref.getString(KEY_LOCATION_IQ, "").orEmpty()
        set(value) = pref.edit { putString(KEY_LOCATION_IQ, value) }

    var isFirstRun: Boolean
        get() = pref.getBoolean(KEY_FIRST_RUN, true)
        set(value) = pref.edit { putBoolean(KEY_FIRST_RUN, value) }

    var notificationCount: Int
        get() = pref.getInt(KEY_NOTIFICATION_COUNT, 3)
        set(value) = pref.edit { putInt(KEY_NOTIFICATION_COUNT, value) }

    var termsAccepted: Boolean
        get() = pref.getBoolean(KEY_TERMS_ACCEPTED, false)
        set(value) = pref.edit { putBoolean(KEY_TERMS_ACCEPTED, value) }

    var disclaimerRead: Boolean
        get() = pref.getBoolean(KEY_DISCLAIMER_READ, false)
        set(value) = pref.edit { putBoolean(KEY_DISCLAIMER_READ, value) }

    var termsLink: String
        get() = pref.getString(KEY_TERMS_LINK, DEFAULT_TERMS_URL).orEmpty()
        set(value) = pref.edit { putString(KEY_TERMS_LINK, value) }

    var isFertilizerGrid: Boolean
        get() = pref.getBoolean("isFertilizerGrid", false)
        set(value) = pref.edit { putBoolean("isFertilizerGrid", value) }

    var deviceToken: String
        get() {
            val saved = pref.getString(KEY_DEVICE_TOKEN, null)
            return if (saved.isNullOrEmpty()) {
                val newToken = UUID.randomUUID().toString()
                pref.edit { putString(KEY_DEVICE_TOKEN, newToken) }
                newToken
            } else saved
        }
        set(value) = pref.edit { putString(KEY_DEVICE_TOKEN, value) }

    var rememberAreaUnit: Boolean
        get() = pref.getBoolean("rememberAreaUnit", false)
        set(value) = pref.edit { putBoolean("rememberAreaUnit", value) }


    fun decrementNotificationCount() {
        val current = notificationCount
        if (current > 0) notificationCount = current - 1
    }

    fun getAppVersionInfo(): String = buildString {
        append("Version: ${BuildConfig.VERSION_NAME}\n")
        append("Release date: ${getAppBuildDate()}")
    }

    private fun getAppBuildDate(): String = try {
        val unixTimestamp = BuildConfig.VERSION_CODE * 1000L
        DateHelper.unixTimeStampToDateTime(unixTimestamp).toString()
    } catch (ex: Exception) {
        Sentry.captureException(ex)
        ""
    }
}