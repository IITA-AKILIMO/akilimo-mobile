package com.akilimo.mobile.utils

import android.content.Context
import android.telephony.TelephonyManager
import io.michaelrocks.libphonenumber.android.NumberParseException
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import java.util.Locale

/** A country the phone field can dial: [region] is the ISO 3166 code ("TZ"), [dialCode] e.g. 255. */
data class PhoneCountry(val region: String, val dialCode: Int)

/**
 * Phone numbers for any country, backed by Google's libphonenumber. Behind an interface so
 * ViewModel tests can fake it (the real one loads metadata from the app's assets).
 */
interface PhoneNumberHelper {
    /** Every region libphonenumber knows, with its dialling code (about 245) */
    fun allCountries(): List<PhoneCountry>

    /** Dialling code for [region], or 0 if unknown */
    fun dialCode(region: String): Int

    /** Best guess for the user's own country: SIM, then mobile network, then the phone's region */
    fun defaultRegion(): String

    /** Is [number] (typed without the country code) a valid number in [region]? */
    fun isValid(number: String, region: String): Boolean

    /** [number] in [region] as +<code><number> (E.164), or null if it can't be parsed */
    fun toE164(number: String, region: String): String?

    /**
     * Splits a stored number into (region, national number). Returns null for numbers saved
     * before the country picker existed (no leading +), which the caller keeps as typed.
     */
    fun split(stored: String): Pair<String, String>?
}

class LibPhoneNumberHelper(private val context: Context) : PhoneNumberHelper {

    // Loading the metadata takes a moment; only do it when the phone field is first used
    private val util: PhoneNumberUtil by lazy { PhoneNumberUtil.createInstance(context) }

    override fun allCountries(): List<PhoneCountry> =
        util.supportedRegions.map { PhoneCountry(it, util.getCountryCodeForRegion(it)) }

    override fun dialCode(region: String): Int = util.getCountryCodeForRegion(region)

    override fun defaultRegion(): String {
        val telephony = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
        return listOfNotNull(
            telephony?.simCountryIso,
            telephony?.networkCountryIso,
            Locale.getDefault().country,
        )
            .map { it.uppercase(Locale.ROOT) }
            .firstOrNull { it in util.supportedRegions }
            ?: FALLBACK_REGION
    }

    override fun isValid(number: String, region: String): Boolean =
        parse(number, region)?.let { util.isValidNumberForRegion(it, region) } ?: false

    override fun toE164(number: String, region: String): String? =
        parse(number, region)?.let { util.format(it, PhoneNumberUtil.PhoneNumberFormat.E164) }

    override fun split(stored: String): Pair<String, String>? {
        if (!stored.startsWith("+")) return null
        val parsed = parse(stored, null) ?: return null
        val region = util.getRegionCodeForNumber(parsed) ?: return null
        return region to util.getNationalSignificantNumber(parsed)
    }

    private fun parse(number: String, region: String?) = try {
        util.parse(number, region)
    } catch (_: NumberParseException) {
        null
    }

    private companion object {
        /** Used only when neither the SIM, the network nor the phone's region gives a country */
        const val FALLBACK_REGION = "NG"
    }
}
