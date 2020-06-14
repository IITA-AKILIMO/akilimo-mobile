package com.iita.akilimo.utils;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.iita.akilimo.BuildConfig;
import com.iita.akilimo.R;
import com.iita.akilimo.models.FirebaseTopic;

import java.util.List;

public class FireBaseConfig {

    private static final String LOG_TAG = FireBaseConfig.class.getSimpleName();

    private FirebaseRemoteConfig mFireBaseRemoteConfig;
    private FirebaseRemoteConfigSettings configSettings;
    private Activity activity;
    private SessionManager sessionManager;
    private int fetchIntervalInSeconds = 43200;


    public FireBaseConfig(final Activity activity) {
        this.activity = activity;
        if (BuildConfig.DEBUG) {
            fetchIntervalInSeconds = 10;
        }
        sessionManager = new SessionManager(activity);
        mFireBaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(fetchIntervalInSeconds)
                .build();
        mFireBaseRemoteConfig.setConfigSettingsAsync(configSettings);
        mFireBaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_defaults);
    }

    public void fetchNewRemoteConfig() {
        mFireBaseRemoteConfig.fetchAndActivate()
                .addOnCompleteListener(activity, task -> {
                    if (task.isSuccessful()) {

                        boolean updated = task.getResult();


                        String apiToken = mFireBaseRemoteConfig.getString("api_token");
                        String akilimo_api = mFireBaseRemoteConfig.getString("akilimo_api");

                        if (BuildConfig.DEBUG) {
                            akilimo_api = mFireBaseRemoteConfig.getString("akilimo_api_demo");
                            Toast.makeText(activity, "Data fetch succeeded and " + fetchIntervalInSeconds + " updated " + updated, Toast.LENGTH_SHORT).show();
                        }

                        Toast.makeText(activity, "Api endpoint is " + akilimo_api, Toast.LENGTH_SHORT).show();

                        String mapBoxKey = mFireBaseRemoteConfig.getString("mapBoxApiKey");
                        String locationIqToken = mFireBaseRemoteConfig.getString("locationIqToken");
                        String firebaseTopicString = mFireBaseRemoteConfig.getString("firebase_topics");

                        String ngnRate = mFireBaseRemoteConfig.getString("ngnRate");
                        String tzsRate = mFireBaseRemoteConfig.getString("tzsRate");

                        sessionManager.setApiEndPoint(akilimo_api);
                        sessionManager.setMapBoxApiKey(mapBoxKey);
                        sessionManager.setLocationIqToken(locationIqToken);
                        sessionManager.setApiToken(apiToken);
                        sessionManager.setNgnRate(ngnRate);
                        sessionManager.setTzsRate(tzsRate);
                        sessionManager.setFireBaseTopics(firebaseTopicString);

                        subscribeTopic();
                    }
                });
    }

    private void subscribeTopic() {
        List<FirebaseTopic> topics;
        try {
            topics = sessionManager.getFirebaseTopics();
            for (FirebaseTopic topic : topics) {
                FirebaseMessaging.getInstance().subscribeToTopic(topic.getTopicName());
            }
        } catch (Exception ex) {
            Crashlytics.log(Log.ERROR, LOG_TAG, ex.getMessage());
            Crashlytics.logException(ex);
        }
    }
}
