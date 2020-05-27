package com.iita.akilimo.views.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.iita.akilimo.R;
import com.iita.akilimo.inherit.BaseActivity;
import com.iita.akilimo.models.FirebaseTopic;
import com.iita.akilimo.utils.SessionManager;

import java.util.List;

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
