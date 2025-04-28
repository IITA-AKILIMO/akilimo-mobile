package com.akilimo.mobile.utils

import com.mynameismidori.currencypicker.ExtendedCurrency
import io.sentry.Sentry

object CurrencyCode {

    fun getCurrencySymbol(currencyCode: String?): ExtendedCurrency? {
        val currency = ExtendedCurrency.getCurrencyByISO(currencyCode)
        try {
            return currency
        } catch (ex: Exception) {
            Sentry.captureException(ex)
        }
        return null
    }
}
