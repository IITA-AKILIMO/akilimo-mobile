package com.iita.akilimo.utils;

import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.mynameismidori.currencypicker.ExtendedCurrency;

public class CurrencyCode {

    private static String LOG_TAG = CurrencyCode.class.getSimpleName();

    public static ExtendedCurrency getCurrencySymbol(String currencyCode) {
        ExtendedCurrency currency = ExtendedCurrency.getCurrencyByISO(currencyCode);
        try {
            return currency;
        } catch (Exception ex) {
            Crashlytics.log(Log.ERROR, LOG_TAG, ex.getMessage());
            Crashlytics.logException(ex);
        }
        return null;
    }
}
