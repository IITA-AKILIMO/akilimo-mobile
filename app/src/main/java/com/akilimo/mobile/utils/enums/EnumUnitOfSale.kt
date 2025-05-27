package com.akilimo.mobile.utils.enums

import android.content.Context
import android.os.Parcelable
import com.akilimo.mobile.R
import kotlinx.parcelize.Parcelize

@Parcelize
enum class EnumUnitOfSale : Parcelable {
    NA {
        override fun unitWeight(): Double {
            return 0.0
        }

        override fun unitOfSale(context: Context): String {
            return "NA"
        }

        override fun unitOfSaleText(context: Context): String {
            return "NA"
        }
    },
    FRESH_COB {
        override fun unitWeight(): Double {
            return 1000.0
        }

        override fun unitOfSale(context: Context): String {
            return "fresh cob"
        }

        override fun unitOfSaleText(context: Context): String {
            return context.getString(R.string.lbl_fresh_cob)
        }
    },
    ONE_KG {
        override fun unitWeight(): Double {
            return 1.0
        }

        override fun unitOfSale(context: Context): String {
            return context.getString(R.string.lbl_one_kg_unit)
        }

        override fun unitOfSaleText(context: Context): String {
            return context.getString(R.string.lbl_one_kg_bag_unit)
        }
    },
    FIFTY_KG {
        override fun unitWeight(): Double {
            return 50.0
        }

        override fun unitOfSale(context: Context): String {
            return context.getString(R.string.lbl_50_kg_unit)
        }

        override fun unitOfSaleText(context: Context): String {
            return context.getString(R.string.lbl_50_kg_bag_unit)
        }
    },
    HUNDRED_KG {
        override fun unitWeight(): Double {
            return 100.0
        }

        override fun unitOfSale(context: Context): String {
            return context.getString(R.string.lbl_100_kg_unit)
        }

        override fun unitOfSaleText(context: Context): String {
            return context.getString(R.string.lbl_100_kg_bag_unit)
        }
    },
    TONNE {
        override fun unitWeight(): Double {
            return 1000.0
        }

        override fun unitOfSale(context: Context): String {
            return context.getString(R.string.lbl_1000_kg_unit)
        }

        override fun unitOfSaleText(context: Context): String {
            return context.getString(R.string.lbl_1000_kg_bag_unit)
        }
    };

    abstract fun unitWeight(): Double
    abstract fun unitOfSale(context: Context): String
    abstract fun unitOfSaleText(context: Context): String
}
