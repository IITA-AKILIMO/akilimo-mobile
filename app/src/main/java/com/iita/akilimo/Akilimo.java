package com.iita.akilimo;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.multidex.BuildConfig;
import androidx.multidex.MultiDexApplication;

import com.crashlytics.android.Crashlytics;
import com.iita.akilimo.views.activities.LanguagePickerActivity;

import net.danlew.android.joda.JodaTimeAndroid;

import org.jetbrains.annotations.Nullable;

import java.util.Locale;

import dev.b3nedikt.app_locale.AppLocale;
import dev.b3nedikt.app_locale.AppLocaleRepository;
import dev.b3nedikt.app_locale.SharedPrefsAppLocaleRepository;
import dev.b3nedikt.reword.RewordInterceptor;
import io.fabric.sdk.android.Fabric;
import io.github.inflationx.viewpump.ViewPump;
import io.objectbox.BoxStore;
import io.objectbox.android.AndroidObjectBrowser;

public class Akilimo extends MultiDexApplication {
    private final String DB_NAME = "AKILIMO_APR_2020_26";
    private BoxStore boxStore;


    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());

        AppLocale.setSupportedLocales(Locales.APP_LOCALES);
        SharedPrefsAppLocaleRepository prefs = new SharedPrefsAppLocaleRepository(this);


        AppLocale.setAppLocaleRepository(prefs); //persist changes
        Locale desiredLocale = prefs.getDesiredLocale();
        if (desiredLocale != null) {
            AppLocale.setDesiredLocale(desiredLocale);
        }

        ViewPump.init(ViewPump.builder()
                .addInterceptor(RewordInterceptor.INSTANCE)
                .build()
        );
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