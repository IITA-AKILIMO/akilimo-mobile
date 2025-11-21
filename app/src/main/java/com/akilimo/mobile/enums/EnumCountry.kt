package com.akilimo.mobile.enums

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Currency

@Parcelize
enum class EnumCountry(
    val countryName: String,
    val currencyCode: String,
) : Parcelable {

    NG("Nigeria", "NGN"),
    TZ("Tanzania", "TZS"),
    GH("Ghana", "GHS"),
    RW("Rwanda", "RWF"),
    BI("Burundi", "BIF"),
    Unsupported("", "");


    fun currencyName(): String = Currency.getInstance(currencyCode).displayName

    companion object {
        fun fromCode(code: String?): EnumCountry {
            if (code.isNullOrBlank()) return Unsupported
            return entries.firstOrNull { it.name.equals(code, ignoreCase = true) } ?: Unsupported
        }
    }
}
