package com.iita.akilimo.utils.enums

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
enum class EnumOperationType : Parcelable {
    MANUAL {
        override fun operationName(): String {
            return "manual"
        }
    },
    MECHANICAL {
        override fun operationName(): String {
            return "tractor"
        }
    },
    HERBICIDE {
        override fun operationName(): String {
            return "herbicide"
        }
    },
    NONE {
        override fun operationName(): String {
            return "NA"
        }
    };

    abstract fun operationName(): String
}
