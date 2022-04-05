package com.akilimo.mobile.utils.enums

import android.content.Context
import android.os.Parcelable
import com.akilimo.mobile.R
import kotlinx.android.parcel.Parcelize
import org.jetbrains.annotations.NotNull

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

        override fun currencyName(context: Context): String {
            return context.getString(R.string.lbl_ngn_currency_name)
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

        override fun currencyName(context: Context): String {
            return context.getString(R.string.lbl_tzs_currency_name)
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

        override fun currencyName(context: Context): String {
            return "Kenya shillings"
        }
    },
    Ghana {
        override fun countryName(): String {
            return "Ghana"
        }

        override fun countryCode(): String {
            return "GH"
        }

        override fun currency(): String {
            return "GHS"
        }

        override fun currencyName(context: Context): String {
            return context.getString(R.string.lbl_ghs_currency_name)
        }
    },
    Rwanda {
        override fun countryName(): String {
            return "Rwanda"
        }

        override fun countryCode(): String {
            return "RW"
        }

        override fun currency(): String {
            return "RWF"
        }

        override fun currencyName(context: Context): String {
            return context.getString(R.string.lbl_rwf_currency_name)
        }
    },
    Burundi {
        override fun countryName(): String {
            return "Burundi"
        }

        override fun countryCode(): String {
            return "BI"
        }

        override fun currency(): String {
            return "BIF"
        }

        override fun currencyName(context: Context): String {
            return context.getString(R.string.lbl_bif_currency_name)
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

        override fun currencyName(context: Context): String {
            return "US Dollars"
        }
    };

    abstract fun countryCode(): String
    abstract fun countryName(): String
    abstract fun currency(): String
    abstract fun currencyName(context: Context): @NotNull String
}
