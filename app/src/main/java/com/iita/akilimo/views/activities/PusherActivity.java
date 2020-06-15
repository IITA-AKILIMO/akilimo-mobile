package com.iita.akilimo.views.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;

import com.iita.akilimo.R;
import com.iita.akilimo.inherit.BaseActivity;
import com.iita.akilimo.utils.SessionManager;

@SuppressLint("LogNotTimber")
public class PusherActivity extends BaseActivity {

    private static final String TAG = PusherActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pusher);

        initComponent();
    }

    @Override
    protected void initToolbar() {

    }

    @Override
    protected void initComponent() {

        fetchFireBaseConfig(this);
        initializePushNotification(new SessionManager(this));

    }

    @Override
    protected void validate(boolean backPressed) {

    }
}
