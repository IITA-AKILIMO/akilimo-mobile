package com.iita.akilimo;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.multidex.MultiDexApplication;

import com.crashlytics.android.Crashlytics;

import net.danlew.android.joda.JodaTimeAndroid;

import io.fabric.sdk.android.Fabric;
import io.objectbox.BoxStore;
import io.objectbox.android.AndroidObjectBrowser;
import leakcanary.LeakCanary;
import timber.log.Timber;

public class Akilimo extends MultiDexApplication {
    private final String DB_NAME = "AkilimoDB";
    private BoxStore boxStore;


    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        //@HACK This is used to enable proper vector support for android 4.4 and below, it causes crashing when firing up the application
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        JodaTimeAndroid.init(this);

//        BoxStore.deleteAllFiles(this, DB_NAME);
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