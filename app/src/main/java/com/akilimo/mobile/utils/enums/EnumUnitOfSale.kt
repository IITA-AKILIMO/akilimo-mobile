package com.akilimo.mobile.utils.enums

import android.content.Context
import android.os.Parcelable
import com.akilimo.mobile.R
import kotlinx.parcelize.Parcelize

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
            return context.getString(R.string.lbl_one_kg_unit)
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
            return context.getString(R.string.lbl_50_kg_unit)
        }

        override fun unitOfSaleText(context: Context): String {
            return context.getString(R.string.lbl_50_kg_bag_unit)
        }
    },
    HUNDRED_KG {
        override fun unitWeight(): Int {
            return 100
        }

        override fun unitOfSale(context: Context): String {
            return context.getString(R.string.lbl_100_kg_unit)
        }

        override fun unitOfSaleText(context: Context): String {
            return context.getString(R.string.lbl_100_kg_bag_unit)
        }
    },
    THOUSAND_KG {
        override fun unitWeight(): Int {
            return 1000
        }

        override fun unitOfSale(context: Context): String {
            return context.getString(R.string.lbl_1000_kg_unit)
        }

        override fun unitOfSaleText(context: Context): String {
            return context.getString(R.string.lbl_1000_kg_bag_unit)
        }
    };

    abstract fun unitWeight(): Int
    abstract fun unitOfSale(context: Context): String
    abstract fun unitOfSaleText(context: Context): String
}
