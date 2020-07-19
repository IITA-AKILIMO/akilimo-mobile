package com.iita.akilimo.utils.enums

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
enum class EnumCountry : Parcelable {
    Nigeria {
        override fun countryName(): String {
            return "Nigeria"
        }

        override fun countryCode(): String {
            return "NG"
        }

        override fun currency(): String {
            return "NGN"
        }

        override fun currencyName(): String {
            return "Naira"
        }
    },
    Tanzania {
        override fun countryName(): String {
            return "Tanzania"
        }

        override fun countryCode(): String {
            return "TZ"
        }

        override fun currency(): String {
            return "TZS"
        }

        override fun currencyName(): String {
            return "Tanzania shillings"
        }
    },
    Kenya {
        override fun countryName(): String {
            return "Kenya"
        }

        override fun countryCode(): String {
            return "KE"
        }

        override fun currency(): String {
            return "KES"
        }

        override fun currencyName(): String {
            return "Kenya shillings"
        }
    },
    Other {
        override fun countryName(): String {
            return "U.S.A"
        }

        override fun countryCode(): String {
            return "US"
        }

        override fun currency(): String {
            return "USD"
        }

        override fun currencyName(): String {
            return "US Dollars"
        }
    };

    abstract fun countryCode(): String
    abstract fun countryName(): String
    abstract fun currency(): String
    abstract fun currencyName(): String
}