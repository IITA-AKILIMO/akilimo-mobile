package com.iita.akilimo;


import androidx.appcompat.app.AppCompatDelegate;
import androidx.multidex.MultiDexApplication;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.Configuration;
import com.blongho.country_data.World;
import com.crashlytics.android.Crashlytics;

import net.danlew.android.joda.JodaTimeAndroid;

import java.util.Locale;

import dev.b3nedikt.app_locale.AppLocale;
import dev.b3nedikt.app_locale.SharedPrefsAppLocaleRepository;
import dev.b3nedikt.reword.RewordInterceptor;
import io.fabric.sdk.android.Fabric;
import io.github.inflationx.viewpump.ViewPump;

public class Akilimo extends MultiDexApplication {

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
        World.init(this);

        Configuration dbConfiguration = new Configuration.Builder(this)
                .setDatabaseName("akilimo.db")
                .setDatabaseVersion(1)
                .create();

        ActiveAndroid.initialize(dbConfiguration);
    }
}