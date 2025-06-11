package com.akilimo.mobile.utils.enums

import android.content.Context
import android.os.Parcelable
import com.akilimo.mobile.R
import kotlinx.parcelize.Parcelize

@Parcelize
enum class EnumCountry(
    val code: String,
    val currencyCode: String,
    private val currencyNameResId: Int? = null,
    private val fallbackCurrencyName: String
) : Parcelable {

    Nigeria("NG", "NGN", R.string.lbl_ngn_currency_name, "Naira"),
    Tanzania("TZ", "TZS", R.string.lbl_tzs_currency_name, "Tanzanian Shilling"),
    Ghana("GH", "GHS", R.string.lbl_ghs_currency_name, "Ghanaian Cedi"),
    Rwanda("RW", "RWF", R.string.lbl_rwf_currency_name, "Rwandan Franc"),
    Burundi("BI", "BIF", R.string.lbl_bif_currency_name, "Burundian Franc"),
    Other("US", "USD", null, "US Dollars");

    fun countryCode(): String = code

    fun currencyName(context: Context): String {
        return currencyNameResId?.let { context.getString(it) } ?: fallbackCurrencyName
    }
}
