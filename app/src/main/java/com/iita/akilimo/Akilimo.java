package com.iita.akilimo;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.multidex.MultiDexApplication;

import com.crashlytics.android.Crashlytics;

import net.danlew.android.joda.JodaTimeAndroid;

import io.fabric.sdk.android.Fabric;
import io.objectbox.BoxStore;
import io.objectbox.android.AndroidObjectBrowser;

public class Akilimo extends MultiDexApplication {
    private final String DB_NAME = "AkilimoApr2020";
    private BoxStore boxStore;


    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        //@FIX This is used to enable proper vector support for android 4.4 and below, it causes crashing when firing up the application
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        JodaTimeAndroid.init(this);

        if (!BuildConfig.DEBUG) {
            BoxStore.deleteAllFiles(this, DB_NAME);
        }
        boxStore = MyObjectBox.builder()
                .androidContext(Akilimo.this)
                .name(DB_NAME)
                .build();

        if (BuildConfig.DEBUG) {
            new AndroidObjectBrowser(boxStore).start(this);
        }

    }

    public BoxStore getBoxStore() {
        return boxStore;
    }
}