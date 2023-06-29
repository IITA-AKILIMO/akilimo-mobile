package com.akilimo.mobile.utils;

import android.util.Log;


import com.mynameismidori.currencypicker.ExtendedCurrency;

import io.sentry.Sentry;

public class CurrencyCode {

    private static String LOG_TAG = CurrencyCode.class.getSimpleName();

    public static ExtendedCurrency getCurrencySymbol(String currencyCode) {
        ExtendedCurrency currency = ExtendedCurrency.getCurrencyByISO(currencyCode);
        try {
            return currency;
        } catch (Exception ex) {
            Sentry.captureException(ex);
        }
        return null;
    }
}
