package com.akilimo.mobile.utils

import android.content.Context
import android.content.SharedPreferences
import com.akilimo.mobile.BuildConfig
import io.sentry.Sentry
import org.threeten.bp.ZoneId
import java.util.UUID

@Suppress(
    "kotlin:S6291",
    "kotlin:S2068"
) // Unencrypted preferences (justified: no sensitive info stored)
class PreferenceManager(context: Context) {

    companion object {
        private const val PREF_NAME = "akilimo-pref-data"
        private const val AKILIMO_API_ENDPOINT = "https://api.akilimo.org/"
        private const val FUELROD_API_ENDPOINT = "https://api.munywele.co.ke/"
        private const val PRIVACY_POLICY_LINK =
            "https://akilimo.org/akilimo-privacy-policy.html"
    }

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    // region API Settings

    var akilimoEndpoint: String
        get() = prefs.getString("apiResource", AKILIMO_API_ENDPOINT) ?: ""
        set(value) = prefs.edit().putString("apiResource", value).apply()

    var fuelrodEndpoint: String
        get() = prefs.getString("fuelrodResource", FUELROD_API_ENDPOINT) ?: ""
        set(value) = prefs.edit().putString("fuelrodResource", value).apply()

    var apiToken: String
        get() = prefs.getString("apiToken", "") ?: ""
        set(value) = prefs.edit().putString("apiToken", value).apply()

    var apiRefreshToken: String
        get() = prefs.getString("apiRefreshToken", "") ?: ""
        set(value) = prefs.edit().putString("apiRefreshToken", value).apply()

    // endregion

    // region Third-party Keys

    var mapBoxApiKey: String
        get() = prefs.getString("mapBoxKey", "") ?: ""
        set(value) = prefs.edit().putString("mapBoxKey", value).apply()

    var locationIqToken: String
        get() = prefs.getString("locationIqToken", "") ?: ""
        set(value) = prefs.edit().putString("locationIqToken", value).apply()

    // endregion

    // region Terms & Disclaimer

    var privacyPolicyRead: Boolean
        get() = prefs.getBoolean("termsAccepted", false)
        set(value) = prefs.edit().putBoolean("termsAccepted", value).apply()

    var termsRead: Boolean
        get() = prefs.getBoolean("disclaimerRead", false)
        set(value) = prefs.edit().putBoolean("disclaimerRead", value).apply()

    var privacyPolicyLink: String
        get() = prefs.getString("termsLink", PRIVACY_POLICY_LINK) ?: PRIVACY_POLICY_LINK
        set(value) = prefs.edit().putString("termsLink", value).apply()

    // endregion

    // region App Lifecycle & Notification

    var firstRun: Boolean
        get() = prefs.getBoolean("firstRun", true)
        set(value) = prefs.edit().putBoolean("firstRun", value).apply()

    var notificationCount: Int
        get() = prefs.getInt("notificationCount", 3)
        set(value) = prefs.edit().putInt("notificationCount", value).apply()

    fun decrementNotificationCount() {
        notificationCount = (notificationCount - 1).coerceAtLeast(0)
    }

    val appVersion: String
        get() = buildString {
            append("Version: ")
            append(BuildConfig.VERSION_NAME)
            append("\nRelease date: ")
            append(appBuildDate)
        }

    private val appBuildDate: String
        get() = try {
            val timestamp = BuildConfig.VERSION_CODE * 1000L
            DateHelper.unixTimeStampToDate(timestamp, ZoneId.of("UTC")).toString()
        } catch (ex: Exception) {
            Sentry.captureException(ex)
            ""
        }

    // endregion

    // region Device Token

    var deviceToken: String
        get() {
            var token = prefs.getString("deviceToken", "") ?: ""
            if (token.isBlank()) {
                token = UUID.randomUUID().toString()
                deviceToken = token
            }
            return token
        }
        set(value) = prefs.edit().putString("deviceToken", value).apply()

    // endregion
}