package com.akilimo.mobile.utils;

import android.util.Log;


import com.mynameismidori.currencypicker.ExtendedCurrency;

public class CurrencyCode {

    private static String LOG_TAG = CurrencyCode.class.getSimpleName();

    public static ExtendedCurrency getCurrencySymbol(String currencyCode) {
        ExtendedCurrency currency = ExtendedCurrency.getCurrencyByISO(currencyCode);
        try {
            return currency;
        } catch (Exception ex) {
            //TODO  send this to third party logs tracker
        }
        return null;
    }
}
