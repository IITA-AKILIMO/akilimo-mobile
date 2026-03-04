package com.akilimo.mobile.helper

import android.content.Context
import androidx.core.content.edit
import com.akilimo.mobile.Locales
import com.akilimo.mobile.utils.DateHelper
import io.sentry.Sentry
import java.util.UUID

class SessionManager private constructor(private val pref: android.content.SharedPreferences) {

    companion object {
        private const val PREF_NAME = "new-akilimo-config"

        // FIXED: removed trailing space
        private const val KEY_LANG_CODE = "languageCode"
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

        @Volatile
        private var INSTANCE: SessionManager? = null

        /** Lazily initialize and return a singleton backed by applicationContext prefs. */
        fun get(context: Context?): SessionManager {
            // Prefer the provided context; fall back to application singleton if null
            val appContext = context?.applicationContext ?: run {
                // If you have an Application singleton, use it here:
                // AkilimoApp.instance?.applicationContext
                throw IllegalArgumentException("Context must not be null when obtaining SessionManager")
            }

            val existing = INSTANCE
            if (existing != null) return existing

            return synchronized(this) {
                val again = INSTANCE
                if (again != null) again
                else {
                    val prefs = appContext.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                    val created = SessionManager(prefs)
                    INSTANCE = created
                    created
                }
            }
        }

        /** Non-singleton helper for tests or short-lived usage. */
        fun from(context: Context): SessionManager {
            val prefs = context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            return SessionManager(prefs)
        }
    }

    var languageCode: String
        get() = pref.getString(KEY_LANG_CODE, Locales.english.language).orEmpty()
        set(value) = pref.edit { putString(KEY_LANG_CODE, value) }

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
        get() = pref.getString(KEY_LOCATION_IQ, "pk.2e825998b141366ea93c856dfc352010").orEmpty()
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
        append("Version: ${com.akilimo.mobile.BuildConfig.VERSION_NAME}\n")
        append("Release date: ${getAppBuildDate()}")
    }

    private fun getAppBuildDate(): String = try {
        val unixTimestamp = com.akilimo.mobile.BuildConfig.VERSION_CODE * 1000L
        DateHelper.unixTimeStampToDateTime(unixTimestamp).toString()
    } catch (ex: Exception) {
        Sentry.captureException(ex)
        ""
    }
}