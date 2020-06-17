package com.iita.akilimo.utils.enums

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
enum class EnumUnitOfSale : Parcelable {
    NA {
        override fun unitWeight(): Int {
            return 0
        }

        override fun unitOfSale(): String {
            return "NA"
        }

        override fun unitOfSaleText(): String {
            return "NA"
        }
    },
    ONE_KG {
        override fun unitWeight(): Int {
            return 1
        }

        override fun unitOfSale(): String {
            return "kg"
        }

        override fun unitOfSaleText(): String {
            return "a 1 kg bag"
        }
    },
    FIFTY_KG {
        override fun unitWeight(): Int {
            return 50
        }

        override fun unitOfSale(): String {
            return "50 kg bag"
        }

        override fun unitOfSaleText(): String {
            return "a 50 kg bag"
        }
    },
    HUNDRED_KG {
        override fun unitWeight(): Int {
            return 100
        }

        override fun unitOfSale(): String {
            return "100 kg bag"
        }

        override fun unitOfSaleText(): String {
            return "a 100 kg bag"
        }
    },
    THOUSAND_KG {
        override fun unitWeight(): Int {
            return 1000
        }

        override fun unitOfSale(): String {
            return "tonne"
        }

        override fun unitOfSaleText(): String {
            return "1 tonne"
        }
    };

    abstract fun unitWeight(): Int
    abstract fun unitOfSale(): String
    abstract fun unitOfSaleText(): String
}