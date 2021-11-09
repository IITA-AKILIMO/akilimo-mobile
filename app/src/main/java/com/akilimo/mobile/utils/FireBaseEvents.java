package com.akilimo.mobile.utils;

import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;

public class FireBaseEvents {
    private FirebaseAnalytics mFirebaseAnalytics;

    public FireBaseEvents(Context context) {
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
    }

    public static FireBaseEvents newInstance(Context context) {
        return new FireBaseEvents(context);
    }

    public void logEvent(String eventName, Bundle bundle) {
        mFirebaseAnalytics.logEvent(eventName, bundle);
    }
}
