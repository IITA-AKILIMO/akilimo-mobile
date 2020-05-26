package com.iita.akilimo.views.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;
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
        //register firebase instance
        fetchFireBaseConfig(this);
        sessionManager = new SessionManager(this);
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (task.isSuccessful()) {
                            //get the tokens
                            String token = task.getResult().getToken();
                            sessionManager.saveDeviceToken(token);
                            Log.d(TAG, "FCM token i: " + token);
                        }
                    }
                });

        FirebaseMessaging.getInstance().subscribeToTopic("akilimo-updates")
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getApplicationContext(), "Success, subscribed to akilimo updates", Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    protected void validate(boolean backPressed) {

    }
}
