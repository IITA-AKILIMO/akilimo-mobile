package com.iita.akilimo.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.iita.akilimo.BuildConfig;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.Currency;
import java.util.Locale;
import java.util.UUID;


/**
 * AppSession manager for logged in users
 */

public class SessionManager {
    private static final String PREF_NAME = "FET";

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
        return pref.getString("apiResource", "https://google.com/");
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
        return pref.getString("mapBoxKey", "");
    }

    public void setLocationIqToken(String locationIqToken) {
        editor.putString("locationIqToken", locationIqToken);
        editor.commit();
    }

    public String getLocationIqToken() {
        return pref.getString("locationIqToken", "");
    }

    public void setNgnRate(String ngnRate) {
        editor.putString("ngnRate", ngnRate);
        editor.commit();
    }


    public double getNgnRate() {
        String rate = pref.getString("ngnRate", "0");
        return Double.parseDouble(rate);
    }

    public void setTzsRate(String tzsRate) {
        editor.putString("tzsRate", tzsRate);
        editor.commit();
    }

    public double getTzsRate() {
        String rate = pref.getString("tzsRate", "0");
        return Double.parseDouble(rate);
    }

    public void setFirstRun(boolean firstRun) {
        editor.putBoolean("firstRun", firstRun);
        editor.commit();
    }

    public boolean getFirstRun() {
        return pref.getBoolean("firstRun", true);
    }

    public String getDeviceId() {
        String uniqueID = pref.getString("uuid", null);

        if (uniqueID == null) {
            uniqueID = UUID.randomUUID().toString();
            editor.putString("uuid", uniqueID);
            editor.commit();
        }

        return uniqueID;
    }

    @SuppressWarnings("StringBufferReplaceableByString")
    public String getAppVersion() {
        StringBuilder strVersion = new StringBuilder();
        strVersion.append("Version: ");
        strVersion.append(BuildConfig.VERSION_NAME);
        strVersion.append("\n");
        strVersion.append("Release title: ");
        strVersion.append(getAppBuildDate());

        return strVersion.toString();
    }

    private String getAppBuildDate() {
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

    public String getDeviceCountry() {
        Locale current = context.getResources().getConfiguration().locale;
        return current.getCountry();
    }

    public String getDeviceLocaleCurrency() {
        Locale current = context.getResources().getConfiguration().locale;
        return Currency.getInstance(current).getCurrencyCode();
    }

    public int getNotificationCount() {
        //notification wil be shown a maximum of 3 times
        return pref.getInt("notificationCount", 3);
    }

    public void updateNotificationCount(int notificationCount) {
        notificationCount--;
        editor.putInt("notificationCount", notificationCount);
        editor.commit();
    }
}
