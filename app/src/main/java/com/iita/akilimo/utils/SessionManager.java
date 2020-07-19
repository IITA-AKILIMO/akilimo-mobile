package com.iita.akilimo.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iita.akilimo.BuildConfig;
import com.iita.akilimo.models.FirebaseTopic;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.List;


/**
 * AppSession manager for logged in users
 */

public class SessionManager {
    private static final String PREF_NAME = "akilimo_pref";

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
        return pref.getString("deviceToken", "");
    }

    public void setFireBaseTopics(String firebaseTopicString) {
        editor.putString("firebaseTopics", firebaseTopicString);
        editor.commit();
    }

    public List<FirebaseTopic> getFirebaseTopics() throws JsonProcessingException {

        String topics = pref.getString("firebaseTopics", "[]");
        ObjectMapper mapper = new ObjectMapper();

        return mapper.readValue(topics, new TypeReference<List<FirebaseTopic>>() {
        });
    }


    public void setAdToggle(boolean showAd) {
        editor.putBoolean("showAd", showAd);
        editor.commit();
    }

    public boolean showAd() {
        return pref.getBoolean("showAd", false);
    }

    public void setTermsAccepted(boolean termsAccepted) {
        editor.putBoolean("termsAccepted", termsAccepted);
        editor.commit();
    }

    public boolean termsAccepted() {
        return pref.getBoolean("termsAccepted", false);
    }

    public void setTermsLink(String termsAccepted) {
        editor.putString("termsLink", termsAccepted);
        editor.commit();
    }

    public String getTermsLink() {
        return pref.getString("termsLink", "https://www.akilimo.org/blog/categories/fertilizer-recommendations");
    }
}
