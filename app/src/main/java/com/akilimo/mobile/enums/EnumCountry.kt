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
//    KE("Kenya", "KES"),
    GH("Ghana", "GHS"),
    RW("Rwanda", "RWF"),
    BI("Burundi", "BIF");


    fun currencyName(): String = Currency.getInstance(currencyCode).displayName
}
