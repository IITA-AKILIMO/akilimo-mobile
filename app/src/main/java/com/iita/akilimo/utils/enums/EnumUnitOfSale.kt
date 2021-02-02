package com.iita.akilimo.utils.enums

import android.content.Context
import android.os.Parcelable
import com.iita.akilimo.R
import kotlinx.android.parcel.Parcelize

@Parcelize
enum class EnumUnitOfSale : Parcelable {
    NA {
        override fun unitWeight(): Int {
            return 0
        }

        override fun unitOfSale(context: Context): String {
            return "NA"
        }

        override fun unitOfSaleText(context: Context): String {
            return "NA"
        }
    },
    FRESH_COB {
        override fun unitWeight(): Int {
            return 1000
        }

        override fun unitOfSale(context: Context): String {
            return "fresh cob"
        }

        override fun unitOfSaleText(context: Context): String {
            return context.getString(R.string.lbl_fresh_cob)
        }
    },
    ONE_KG {
        override fun unitWeight(): Int {
            return 1
        }

        override fun unitOfSale(context: Context): String {
            return "kg"
        }

        override fun unitOfSaleText(context: Context): String {
            return context.getString(R.string.lbl_one_kg_bag_unit)
        }
    },
    FIFTY_KG {
        override fun unitWeight(): Int {
            return 50
        }

        override fun unitOfSale(context: Context): String {
            return "50 kg bag"
        }

        override fun unitOfSaleText(context: Context): String {
            return "a 50 kg bag"
        }
    },
    HUNDRED_KG {
        override fun unitWeight(): Int {
            return 100
        }

        override fun unitOfSale(context: Context): String {
            return "100 kg bag"
        }

        override fun unitOfSaleText(context: Context): String {
            return "a 100 kg bag"
        }
    },
    THOUSAND_KG {
        override fun unitWeight(): Int {
            return 1000
        }

        override fun unitOfSale(context: Context): String {
            return "tonne"
        }

        override fun unitOfSaleText(context: Context): String {
            return "1 tonne"
        }
    };

    abstract fun unitWeight(): Int
    abstract fun unitOfSale(context: Context): String
    abstract fun unitOfSaleText(context: Context): String
}