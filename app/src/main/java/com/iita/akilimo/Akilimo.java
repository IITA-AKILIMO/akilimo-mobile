package com.iita.akilimo;

import android.util.Log;

import androidx.multidex.MultiDexApplication;

import com.crashlytics.android.Crashlytics;

import net.danlew.android.joda.JodaTimeAndroid;

import io.fabric.sdk.android.Fabric;
import io.objectbox.BoxStore;
import io.objectbox.android.AndroidObjectBrowser;

public class Akilimo extends MultiDexApplication {
    public static final String LOG_TAG = Akilimo.class.getSimpleName();
    public static final String DB_NAME = "akilimo";
    private BoxStore boxStore;


    @Override
    public void onCreate() {
        super.onCreate();
//        Fabric.with(this, new Crashlytics());

//        BoxStore.deleteAllFiles(this, DB_NAME);
        boxStore = MyObjectBox.builder()
                .androidContext(Akilimo.this)
                .name(DB_NAME)
                .build();

        if (BuildConfig.DEBUG) {
            boolean started = new AndroidObjectBrowser(boxStore).start(this);
            Log.i(LOG_TAG, "Object box started? " + started);
        }

        JodaTimeAndroid.init(this);
        Log.d(LOG_TAG, "Using ObjectBox" + BoxStore.getVersion() + " (" + BoxStore.getVersionNative() + ")");
    }

    public BoxStore getBoxStore() {
        return boxStore;
    }
}