package com.iita.akilimo.utils.objectbox;

import android.content.Context;

import com.iita.akilimo.BuildConfig;
import com.iita.akilimo.MyObjectBox;

import io.objectbox.BoxStore;
import io.objectbox.android.AndroidObjectBrowser;

public class ObjectBox {
    private static BoxStore boxStore;
    private static final String DB_NAME = "AKILIMO_MAY_2020_15";

    public static void init(Context context) {

        if (!BuildConfig.DEBUG) {
            BoxStore.deleteAllFiles(context, DB_NAME);
        }
        boxStore = MyObjectBox.builder()
                .name(DB_NAME)
                .androidContext(context.getApplicationContext())
                .build();

        if (BuildConfig.DEBUG) {
            new AndroidObjectBrowser(boxStore).start(context);
        }
    }

    public static BoxStore get() {
        return boxStore;
    }
}