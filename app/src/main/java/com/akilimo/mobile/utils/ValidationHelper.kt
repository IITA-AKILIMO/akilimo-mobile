package com.akilimo.mobile.utils

import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import com.crashlytics.android.Crashlytics
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.Phonenumber

class ValidationHelper {
    private val phoneUtil: PhoneNumberUtil = PhoneNumberUtil.getInstance()
    private val emailPattern =
        "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9]+\\.([a-zA-Z]{3,5}|[a-zA-z]{2,5}\\.[a-zA-Z]{2,5})"

    companion object {
        private val LOG_TAG = ValidationHelper::class.java.simpleName
    }

    fun isValidPhoneNumber(userPhoneNumber: String, country: String): Boolean {

        try {
            val phoneNumber = phoneUtil.parse(userPhoneNumber, country)
            return phoneUtil.isValidNumber(phoneNumber)
        } catch (ex: NumberParseException) {
            Crashlytics.log(
                Log.ERROR,
                LOG_TAG,
                ex.message
            )
            Crashlytics.logException(ex)
        }

        return false
    }

    fun convertPhoneNumber(userPhoneNumber: String, country: String): Phonenumber.PhoneNumber? {
        var phoneNumber: Phonenumber.PhoneNumber? = null
        try {
            phoneNumber = phoneUtil.parse(userPhoneNumber, country)
        } catch (ex: NumberParseException) {
            Crashlytics.log(Log.ERROR, LOG_TAG, ex.message)
            Crashlytics.logException(ex)
        }

        return phoneNumber
    }

    fun isValidEmail(email: String): Boolean {
        if (email.matches(emailPattern.toRegex())) {
            return true
        }
        return false
    }
}
