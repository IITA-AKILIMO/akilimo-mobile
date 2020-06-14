package com.iita.akilimo.utils.objectbox;

import android.content.Context;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.iita.akilimo.BuildConfig;
import com.iita.akilimo.MyObjectBox;

import io.objectbox.BoxStore;
import io.objectbox.android.AndroidObjectBrowser;

public class ObjectBox {
    private static BoxStore boxStore;
    private static final String DB_NAME = "AKILIMO_JUN_2020_11";

    public static void init(Context context) {

        if (!BuildConfig.DEBUG) {
            BoxStore.deleteAllFiles(context, DB_NAME);
        }
        boxStore = MyObjectBox.builder()
                .androidContext(context.getApplicationContext())
                /*.androidReLinker(ReLinker.log(new ReLinker.Logger() {
                    @Override
                    public void log(String message) {
                        Crashlytics.log(Log.INFO, DB_NAME, message);
                    }
                }))*/
                .name(DB_NAME)
                .build();

        if (BuildConfig.DEBUG) {
            new AndroidObjectBrowser(boxStore).start(context);
        }
    }

    public static BoxStore get() {
        return boxStore;
    }
}