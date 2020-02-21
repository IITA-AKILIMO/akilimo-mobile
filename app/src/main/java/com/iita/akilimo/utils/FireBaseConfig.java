package com.iita.akilimo.utils;

import android.app.Activity;
import android.widget.Toast;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.iita.akilimo.R;

public class FireBaseConfig {

    private static final String LOG_TAG = FireBaseConfig.class.getSimpleName();

    private FirebaseRemoteConfig mFireBaseRemoteConfig;
    private FirebaseRemoteConfigSettings configSettings;
    private Activity activity;
    private SessionManager sessionManager;
    private long cacheExpiration = 0;
    private String api_resource = "";

    public FireBaseConfig(final Activity activity) {
        this.activity = activity;
        sessionManager = new SessionManager(activity);
        mFireBaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(3600)
                .build();
        mFireBaseRemoteConfig.setConfigSettingsAsync(configSettings);
        mFireBaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_defaults);
    }

    public void fetchNewRemoteConfig() {
        mFireBaseRemoteConfig.fetchAndActivate()
                .addOnCompleteListener(activity, task -> {
                    if (task.isSuccessful()) {
                        String apiToken = mFireBaseRemoteConfig.getString("api_token");
                        String apiScheme = mFireBaseRemoteConfig.getString("api_scheme");
                        String apiUrl = mFireBaseRemoteConfig.getString("api_url_b");
                        String apiEndpoint = mFireBaseRemoteConfig.getString("api_endpoint");
                        String mapBoxKey = mFireBaseRemoteConfig.getString("mapBoxApiKey");
                        String locationIqToken = mFireBaseRemoteConfig.getString("locationIqToken");

                        String ngnRate = mFireBaseRemoteConfig.getString("ngnRate");
                        String tzsRate = mFireBaseRemoteConfig.getString("tzsRate");

                        api_resource = String.format("%s%s%s", apiScheme, apiUrl, apiEndpoint);
                        sessionManager.setApiEndPoint(api_resource);
                        sessionManager.setMapBoxApiKey(mapBoxKey);
                        sessionManager.setLocationIqToken(locationIqToken);
                        sessionManager.setApiToken(apiToken);
                        sessionManager.setNgnRate(ngnRate);
                        sessionManager.setTzsRate(tzsRate);
                    } else {
                        Toast.makeText(activity, "Firebase fetch failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private long getCacheExpiration() {
        return cacheExpiration;
    }

}
