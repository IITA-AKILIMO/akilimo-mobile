package com.iita.akilimo.views.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.iita.akilimo.inherit.BaseActivity;
import com.iita.akilimo.utils.FireBaseConfig;

public class SplashActivity extends BaseActivity {
    private String LOG_TAG = SplashActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread background = new Thread() {
            public void run() {
                try {
//                    Intent intent = new Intent(SplashActivity.this, MapBoxActivity.class);
                    Intent intent = new Intent(SplashActivity.this, HomeActivity.class);
//                    Intent intent = new Intent(SplashActivity.this, FertilizersActivity.class);
//                    Intent intent = new Intent(SplashActivity.this, MarketOutletActivity.class);
                    startActivity(intent);
                    closeActivity(false);

                } catch (Exception ex) {
//                    Crashlytics.log(Log.ERROR, LOG_TAG, ex.getStepTitle());
//                    Crashlytics.logException(ex);
                    Log.e(LOG_TAG, ex.getMessage());

                }
            }
        };
        background.start();

        FireBaseConfig fireBaseConfig = new FireBaseConfig(this);
        fireBaseConfig.fetchNewRemoteConfig();
    }

    @Override
    protected void initToolbar() {
    }

    @Override
    protected void initComponent() {
    }

    @Override
    protected void closeActivity(boolean backPressed) {
        finish();
    }
}
