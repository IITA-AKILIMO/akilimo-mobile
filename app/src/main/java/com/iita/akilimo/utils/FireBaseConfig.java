package com.iita.akilimo.utils;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
//                .setDeveloperModeEnabled(BuildConfig.DEBUG)
//                .setMinimumFetchIntervalInSeconds(3600)
                .build();
        mFireBaseRemoteConfig.setConfigSettings(configSettings);
        mFireBaseRemoteConfig.setDefaults(R.xml.remote_config_defaults);
    }

    public void fetchNewRemoteConfig() {
        mFireBaseRemoteConfig.fetchAndActivate()
                .addOnCompleteListener(activity, new OnCompleteListener<Boolean>() {
                    @Override
                    public void onComplete(@NonNull Task<Boolean> task) {
                        if (task.isSuccessful()) {
                            String apiScheme = mFireBaseRemoteConfig.getString("api_scheme");
                            String apiUrl = mFireBaseRemoteConfig.getString("api_url");
                            String apiEndpoint = mFireBaseRemoteConfig.getString("api_endpoint");
                            String mapBoxKey = mFireBaseRemoteConfig.getString("mapBoxApiKey");
                            String locationIqToken = mFireBaseRemoteConfig.getString("locationIqToken");

                            api_resource = String.format("%s%s%s", apiScheme, apiUrl, apiEndpoint);
                            sessionManager.setApiEndPoint(api_resource);
                            sessionManager.setMapBoxApiKey(mapBoxKey);
                            sessionManager.setLocationIqToken(locationIqToken);

                            Log.i("FIREBASE",mapBoxKey);
                        } else {
                            Toast.makeText(activity, "Firebase fetch failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Deprecated
    public void fetchNewConfig() {
        mFireBaseRemoteConfig.fetch(getCacheExpiration())
                .addOnCompleteListener(activity, task -> {
                    if (task.isSuccessful()) {
                        mFireBaseRemoteConfig.activateFetched();
                    }
                    String apiScheme = mFireBaseRemoteConfig.getString("api_scheme");
                    String apiUrl = mFireBaseRemoteConfig.getString("api_url");
                    String apiEndpoint = mFireBaseRemoteConfig.getString("api_endpoint");

                    api_resource = String.format("%s%s%s", apiScheme, apiUrl, apiEndpoint);
                    sessionManager.setApiEndPoint(api_resource);

                    Log.i(LOG_TAG, "API URL is " + api_resource);

                });
    }

    private long getCacheExpiration() {
// If is developer mode, cache expiration set to 0, in order to test
        if (mFireBaseRemoteConfig.getInfo().getConfigSettings().isDeveloperModeEnabled()) {
            cacheExpiration = 0;
        }
        return cacheExpiration;
    }

}
