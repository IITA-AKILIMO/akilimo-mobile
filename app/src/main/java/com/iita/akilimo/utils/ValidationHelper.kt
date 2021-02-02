package com.iita.akilimo.utils

import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import com.crashlytics.android.Crashlytics
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.Phonenumber

class ValidationHelper {
    private var isNumberValid: Boolean = false
    private val phoneUtil: PhoneNumberUtil

    companion object {
        private val LOG_TAG = ValidationHelper::class.java.simpleName
    }

    init {
        phoneUtil = PhoneNumberUtil.getInstance()
    }

    fun isValidPhoneNumber(userPhoneNumber: String, country: String): Boolean {

        try {
            val phoneNumber = phoneUtil.parse(userPhoneNumber, country)
            isNumberValid = phoneUtil.isValidNumber(phoneNumber)

        } catch (ex: NumberParseException) {
            Crashlytics.log(
                Log.ERROR,
                LOG_TAG,
                ex.message
            )
            Crashlytics.logException(ex)
        }

        return isNumberValid
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

    fun isValidEmail(target: CharSequence): Boolean {
        return !TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches()
    }
}
