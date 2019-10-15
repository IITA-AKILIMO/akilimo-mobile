package com.iita.akilimo;

import androidx.multidex.MultiDexApplication;

import net.danlew.android.joda.JodaTimeAndroid;

import io.objectbox.BoxStore;
import io.objectbox.android.AndroidObjectBrowser;
import timber.log.Timber;

public class Akilimo extends MultiDexApplication {
    public static final String LOG_TAG = Akilimo.class.getSimpleName();
    public static final String DB_NAME = "akilimoDB";
    private BoxStore boxStore;


    @Override
    public void onCreate() {
        super.onCreate();
//        Fabric.with(this, new Crashlytics());

        BoxStore.deleteAllFiles(this, DB_NAME);
        boxStore = MyObjectBox.builder()
                .androidContext(Akilimo.this)
                .name(DB_NAME)
                .build();

        if (BuildConfig.DEBUG) {
            boolean started = new AndroidObjectBrowser(boxStore).start(this);
            Timber.i("Object box started? %s", started);
        }

        JodaTimeAndroid.init(this);
        Timber.d("Using ObjectBox" + BoxStore.getVersion() + " (" + BoxStore.getVersionNative() + ")");
    }

    public BoxStore getBoxStore() {
        return boxStore;
    }
}