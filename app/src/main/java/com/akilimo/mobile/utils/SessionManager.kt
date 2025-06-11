package com.akilimo.mobile.utils

import android.content.Context
import android.content.SharedPreferences
import com.akilimo.mobile.BuildConfig
import io.sentry.Sentry
import org.threeten.bp.ZoneId
import java.util.UUID

class SessionManager(context: Context) {

    companion object {
        private const val PREF_NAME = "akilimo-config"
    }

    private val pref: SharedPreferences = context.getSharedPreferences(PREF_NAME, 0)

    private val editor: SharedPreferences.Editor = pref.edit()

    fun getAkilimoEndpoint(): String {
        return pref.getString("apiResource", "https://api.akilimo.org/") ?: ""
    }

    fun setAkilimoEndpoint(apiResource: String?) {
        editor.putString("apiResource", apiResource).apply()
    }

    fun getFuelrodEndpoint(): String {
        return pref.getString("fuelrodResource", "https://api.munywele.co.ke/") ?: ""
    }

    fun setFuelrodEndpoint(apiResource: String) {
        editor.putString("fuelrodResource", apiResource).apply()
    }

    fun setApiRefreshToken(apiToken: String) {
        editor.putString("apiRefreshToken", apiToken).apply()
    }

    fun getApiRefreshToken(): String {
        return pref.getString("apiRefreshToken", "") ?: ""
    }

    fun setApiToken(apiToken: String) {
        editor.putString("apiToken", apiToken).apply()
    }

    fun getApiToken(): String {
        return pref.getString("apiToken", "") ?: ""
    }

    fun setMapBoxApiKey(mapBoxKey: String) {
        editor.putString("mapBoxKey", mapBoxKey).apply()
    }

    fun getMapBoxApiKey(): String {
        return pref.getString("mapBoxKey", "") ?: ""
    }

    fun setLocationIqToken(locationIqToken: String) {
        editor.putString("locationIqToken", locationIqToken).apply()
    }

    fun getLocationIqToken(): String {
        return pref.getString("locationIqToken", "") ?: ""
    }

    fun setFirstRun(firstRun: Boolean) {
        editor.putBoolean("firstRun", firstRun).apply()
    }

    fun getFirstRun(): Boolean {
        return pref.getBoolean("firstRun", true)
    }

    fun getAppVersion(): String {
        return buildString {
            append("Version: ")
            append(BuildConfig.VERSION_NAME)
            append("\n")
            append("Release date: ")
            append(getAppBuildDate())
        }
    }

    private fun getAppBuildDate(): String {
        return try {
            val unixTimestamp = BuildConfig.VERSION_CODE * 1000L
            val parsedDateTime = DateHelper.unixTimeStampToDate(unixTimestamp, ZoneId.of("UTC"))
            parsedDateTime.toString()
        } catch (ex: Exception) {
            Sentry.captureException(ex)
            ""
        }
    }

    fun getNotificationCount(): Int {
        return pref.getInt("notificationCount", 3)
    }

    fun updateNotificationCount(notificationCount: Int) {
        editor.putInt("notificationCount", notificationCount - 1).apply()
    }

    fun saveDeviceToken(token: String) {
        editor.putString("deviceToken", token).apply()
    }

    fun getDeviceToken(): String {
        var token = pref.getString("deviceToken", "") ?: ""
        if (token.isEmpty()) {
            token = UUID.randomUUID().toString()
            saveDeviceToken(token)
        }
        return token
    }

    fun setTermsAccepted(termsAccepted: Boolean) {
        editor.putBoolean("termsAccepted", termsAccepted).apply()
    }

    fun getTermsAccepted(): Boolean {
        return pref.getBoolean("termsAccepted", false)
    }

    fun setTermsLink(termsLink: String) {
        editor.putString("termsLink", termsLink).apply()
    }

    fun getTermsLink(): String {
        return pref.getString("termsLink", "https://akilimo.org/index.php/akilimo-privacy-policy")
            ?: ""
    }

    fun setDisclaimerRead(disclaimerRead: Boolean) {
        editor.putBoolean("disclaimerRead", disclaimerRead).apply()
    }

    fun getDisclaimerRead(): Boolean {
        return pref.getBoolean("disclaimerRead", false)
    }
}
