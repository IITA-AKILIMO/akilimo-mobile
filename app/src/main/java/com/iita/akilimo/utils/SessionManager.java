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

    @Deprecated
    public void saveUnitOfSale(String unitOfSale, String unitOfSaleText) {
        editor.putString("unitOfSale", unitOfSale);
        editor.putString("unitOfSaleText", unitOfSaleText);
        editor.commit();
    }

    @Deprecated
    public void saveAreaUnit(String area_unit) {
        editor.putString("areaUnit", area_unit);
        editor.commit();
    }

    @Deprecated
    public String getUnitOfSale() {
        return pref.getString("unitOfSale", null);
    }

    @Deprecated
    public String getUnitOfSaleText() {
        return pref.getString("unitOfSaleText", null);
    }

    @Deprecated
    public String getAreaUnit() {
        return pref.getString("areaUnit", "acre");
    }


    public void setApiEndPoint(String apiResource) {
        editor.putString("apiResource", apiResource);
        editor.commit();
    }

    public String getApiEndPoint() {
        return pref.getString("apiResource", "https://google.com/");
    }

    //set api tokens
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

}
