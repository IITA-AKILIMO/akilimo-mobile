package com.iita.akilimo.utils;

import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.mynameismidori.currencypicker.ExtendedCurrency;

import java.util.Currency;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CurrencyCode {

    private static String LOG_TAG = CurrencyCode.class.getSimpleName();

    public static String getCurrencySymbol(String currencyCode) {
        ExtendedCurrency currency = ExtendedCurrency.getCurrencyByISO(currencyCode);
        try {
            return currency.getSymbol();
        } catch (Exception ex) {
            Crashlytics.log(Log.ERROR, LOG_TAG, ex.getMessage());
            Crashlytics.logException(ex);
        }
        return currencyCode;
    }
}
