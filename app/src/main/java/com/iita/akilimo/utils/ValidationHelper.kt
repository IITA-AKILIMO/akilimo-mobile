package com.iita.akilimo.utils

import android.text.TextUtils
import android.util.Patterns
import com.google.firebase.crashlytics.FirebaseCrashlytics
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

    fun isValidPhoneNumber(userPhoneNumber: String, allowedCountry: String): Boolean {

        try {
            val phoneNumber = phoneUtil.parse(userPhoneNumber, allowedCountry)
            isNumberValid = phoneUtil.isValidNumber(phoneNumber)

        } catch (ex: NumberParseException) {
            FirebaseCrashlytics.getInstance().log(ex.message!!)
            FirebaseCrashlytics.getInstance().recordException(ex)
        }

        return isNumberValid
    }

    fun convertPhoneNumber(userPhoneNumber: String, country: String): Phonenumber.PhoneNumber? {
        var phoneNumber: Phonenumber.PhoneNumber? = null
        try {
            phoneNumber = phoneUtil.parse(userPhoneNumber, country)
        } catch (ex: NumberParseException) {
            FirebaseCrashlytics.getInstance().log(ex.message!!)
            FirebaseCrashlytics.getInstance().recordException(ex)
        }

        return phoneNumber
    }

    fun isValidEmail(target: CharSequence): Boolean {
        return !TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches()
    }
}
