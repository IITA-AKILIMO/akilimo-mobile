package com.iita.akilimo.utils;

import android.util.Log;

import com.crashlytics.android.Crashlytics;

import java.util.Currency;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CurrencyCode {

    private static String LOG_TAG = CurrencyCode.class.getSimpleName();

    public static Currency getCurrencySymbol(Locale locale) {
        try {
            return Currency.getInstance(locale);
        } catch (Exception ex) {
            Crashlytics.log(Log.ERROR, LOG_TAG, ex.getMessage());
            Crashlytics.logException(ex);
        }
        return null;
    }
}
