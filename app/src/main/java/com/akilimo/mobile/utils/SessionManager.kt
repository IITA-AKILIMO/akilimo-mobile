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
        return "https://stag-emerging-dodo.ngrok-free.app/api/"
        // return pref.getString("apiResource", "https://api.akilimo.org/") ?: ""
    }

    fun setAkilimoEndpoint(apiResource: String?) {
        editor.putString("apiResource", apiResource).apply()
    }

    fun getFuelrodEndpoint(): String {
        return pref.getString("fuelrodResource", "https://api.munywele.co.ke/") ?: ""
    }

    fun setFuelrodEndpoint(apiResource: String) {
        editor.putString("apiResource", apiResource).apply()
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

    fun setNgnRate(ngnRate: String) {
        editor.putString("ngnRate", ngnRate).apply()
    }

    fun getNgnRate(): Double {
        val rate = pref.getString("ngnRate", "390.34") ?: "390.34"
        return rate.toDoubleOrNull() ?: 390.34
    }

    fun setTzsRate(tzsRate: String) {
        editor.putString("tzsRate", tzsRate).apply()
    }

    fun getTzsRate(): Double {
        val rate = pref.getString("tzsRate", "6.11") ?: "6.11"
        return rate.toDoubleOrNull() ?: 6.11
    }

    fun setGhsRate(ghsRate: String) {
        editor.putString("ghsRate", ghsRate).apply()
    }

    fun getGhsRate(): Double {
        val rate = pref.getString("ghsRate", "6.11") ?: "6.11"
        return rate.toDoubleOrNull() ?: 6.11
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

    fun setCountry(country: String) {
        editor.putString("country", country).apply()
    }

    fun getCountry(): String {
        return pref.getString("country", "NA") ?: "NA"
    }

    fun setTermsLink(termsLink: String) {
        editor.putString("termsLink", termsLink).apply()
    }

    fun getTermsLink(): String {
        return pref.getString("termsLink", "https://akilimo.org/index.php/akilimo-privacy-policy")
            ?: ""
    }

    fun setForward(goForward: Boolean) {
        editor.putBoolean("goF", goForward).apply()
    }

    fun goForward(): Boolean {
        return pref.getBoolean("goF", true)
    }

    fun setApiUser(user: String) {
        editor.putString("apiUser", user).apply()
    }

    fun getApiUser(): String {
        return pref.getString("apiUser", "") ?: ""
    }

    fun setApiPass(pass: String) {
        editor.putString("apiPass", pass).apply()
    }

    fun getApiPass(): String {
        return pref.getString("apiPass", "") ?: ""
    }

    fun setRememberUserInfo(rememberUserInfo: Boolean) {
        editor.putBoolean("rememberUserInfo", rememberUserInfo).apply()
    }

    fun getRememberUserInfo(): Boolean {
        return pref.getBoolean("rememberUserInfo", false)
    }

    fun setRememberAreaUnit(rememberAreaUnit: Boolean) {
        editor.putBoolean("rememberAreaUnit", rememberAreaUnit).apply()
    }

    fun getRememberAreaUnit(): Boolean {
        return pref.getBoolean("rememberAreaUnit", false)
    }

    fun setRememberInvestmentPref(rememberInvestmentPref: Boolean) {
        editor.putBoolean("rememberInvestmentPref", rememberInvestmentPref).apply()
    }

    fun getRememberInvestmentPref(): Boolean {
        return pref.getBoolean("rememberInvestmentPref", false)
    }

    fun setDisclaimerRead(disclaimerRead: Boolean) {
        editor.putBoolean("disclaimerRead", disclaimerRead).apply()
    }

    fun getDisclaimerRead(): Boolean {
        return pref.getBoolean("disclaimerRead", false)
    }
}
