package com.akilimo.mobile;


import androidx.appcompat.app.AppCompatDelegate;
import androidx.multidex.MultiDexApplication;

import com.blongho.country_data.World;


import net.danlew.android.joda.JodaTimeAndroid;

import java.util.Locale;

import dev.b3nedikt.app_locale.AppLocale;
import dev.b3nedikt.app_locale.SharedPrefsAppLocaleRepository;
import dev.b3nedikt.reword.RewordInterceptor;
import io.github.inflationx.viewpump.ViewPump;

public class Akilimo extends MultiDexApplication {

    private String LOG_TAG = Akilimo.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();

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
    }
}
