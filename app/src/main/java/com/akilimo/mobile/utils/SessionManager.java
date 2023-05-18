package com.akilimo.mobile.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.akilimo.mobile.BuildConfig;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.UUID;


/**
 * AppSession manager for logged in users
 */

public class SessionManager {
    private static final String PREF_NAME = "akilimo-pref";

    private SharedPreferences.Editor editor;
    private Context context;
    private SharedPreferences pref;


    @SuppressLint("CommitPrefEdits")
    public SessionManager(Context context) {
        this.context = context;
        pref = this.context.getSharedPreferences(PREF_NAME, 0);
        editor = pref.edit();
    }

    public void setApiEndPoint(String apiResource) {
        editor.putString("apiResource", apiResource);
        editor.commit();
    }

    public String getApiEndPoint() {
        return pref.getString("apiResource", "http://157.245.26.55:8099/api/");
    }

    public void setApiRefreshToken(String apiToken) {
        editor.putString("apiRefreshToken", apiToken);
        editor.commit();
    }

    public String getApiRefreshToken() {
        return pref.getString("apiRefreshToken", "");
    }


    public void setApiToken(String apiToken) {
        editor.putString("apiToken", apiToken);
        editor.commit();
    }

    public String getApiToken() {
        return pref.getString("apiToken", "");
    }

    public void setMapBoxApiKey(String mapBoxKey) {
        editor.putString("mapBoxKey", mapBoxKey);
        editor.commit();
    }

    public String getMapBoxApiKey() {
        return pref.getString("mapBoxKey", "pk.eyJ1IjoibWFzZ2VlayIsImEiOiJjanp0bm43ZmwwNm9jM29udjJod3V6dzB1In0.MevkJtANWZ8Wl9abnLu1Uw");
    }

    public void setLocationIqToken(String locationIqToken) {
        editor.putString("locationIqToken", locationIqToken);
        editor.commit();
    }

    public String getLocationIqToken() {
        return pref.getString("locationIqToken", "pk.2e825998b141366ea93c856dfc352010");
    }

    public void setNgnRate(String ngnRate) {
        editor.putString("ngnRate", ngnRate);
        editor.commit();
    }


    public double getNgnRate() {
        String rate = pref.getString("ngnRate", "390.34");
        return Double.parseDouble(rate);
    }

    public void setTzsRate(String tzsRate) {
        editor.putString("tzsRate", tzsRate);
        editor.commit();
    }

    public double getTzsRate() {
        String rate = pref.getString("tzsRate", "6.11");
        return Double.parseDouble(rate);
    }

    public void setGhsRate(String tzsRate) {
        editor.putString("ghsRate", tzsRate);
        editor.commit();
    }

    public double getGhsRate() {
        String rate = pref.getString("ghsRate", "6.11");
        return Double.parseDouble(rate);
    }

    public void setFirstRun(boolean firstRun) {
        editor.putBoolean("firstRun", firstRun);
        editor.commit();
    }

    public boolean getFirstRun() {
        return pref.getBoolean("firstRun", true);
    }

    @SuppressWarnings("StringBufferReplaceableByString")
    public String getAppVersion() {
        StringBuilder strVersion = new StringBuilder();
        strVersion.append("Version: ");
        strVersion.append(BuildConfig.VERSION_NAME);
        strVersion.append("\n");
        strVersion.append("Release date: ");
        strVersion.append(getAppBuildDate());

        return strVersion.toString();
    }

    public String getAppBuildDate() {
        String appBuildDate = "";
        try {
            long unixTimestamp = BuildConfig.VERSION_CODE * 1000L;
            DateTime parsedDateTime = DateHelper.unixTimeStampToDate(unixTimestamp, DateTimeZone.UTC);
            appBuildDate = parsedDateTime.toDate().toString();
        } catch (Exception ex) {
            Crashlytics.log(Log.ERROR, "PREFS", ex.getMessage());
            Crashlytics.logException(ex);
        }

        return appBuildDate;
    }

    public int getNotificationCount() {
        return pref.getInt("notificationCount", 3);
    }

    public void updateNotificationCount(int notificationCount) {
        notificationCount--;
        editor.putInt("notificationCount", notificationCount);
        editor.commit();
    }

    public void saveDeviceToken(String token) {
        editor.putString("deviceToken", token);
        editor.commit();
    }

    public String getDeviceToken() {
        String token = pref.getString("deviceToken", "");
        if (token.isEmpty()) {
            token = UUID.randomUUID().toString();
            saveDeviceToken(token);
        }
        return token;
    }


    public void setTermsAccepted(boolean termsAccepted) {
        editor.putBoolean("termsAccepted", termsAccepted);
        editor.commit();
    }

    public boolean termsAccepted() {
        return pref.getBoolean("termsAccepted", false);
    }

    public void setCountry(String country) {
        editor.putString("country", country);
        editor.commit();
    }


    public String getCountry() {
        return pref.getString("country", "NA");
    }

    public void setTermsLink(String termsLink) {
        editor.putString("termsLink", termsLink);
        editor.commit();
    }

    public String getTermsLink() {
        return pref.getString("termsLink", "https://akilimo.org/index.php/akilimo-privacy-policy");
    }

    public void setForward(boolean goForward) {
        editor.putBoolean("goF", goForward);
        editor.commit();
    }

    public boolean goForward() {
        return pref.getBoolean("goF", true);
    }

    public void setApiUser(String user) {
        editor.putString("apiUser", user);
        editor.commit();
    }

    public String getApiUser() {
        return pref.getString("apiUser", "");
    }


    public void setApiPass(String pass) {
        editor.putString("apiPass", pass);
        editor.commit();
    }

    public String getApiPass() {
        return pref.getString("apiPass", "");
    }

    public void setRememberUserInfo(boolean rememberUserInfo) {
        editor.putBoolean("rememberUserInfo", rememberUserInfo);
        editor.commit();
    }

    public boolean getRememberUserInfo() {
        return pref.getBoolean("rememberUserInfo", false);
    }

    public void setRememberAreaUnit(boolean rememberAreUnit) {
        editor.putBoolean("rememberAreUnit", rememberAreUnit);
        editor.commit();
    }

    public boolean getRememberAreaUnit() {
        return pref.getBoolean("rememberAreUnit", false);
    }


}
