package com.akilimo.mobile.utils.enums

import android.content.Context
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
enum class EnumOperationType : Parcelable {
    MANUAL,
    TRACTOR,
    HERBICIDE,
    NONE;

    fun getLocalizedName(context: Context): String {
        return when (this) {
//            MANUAL -> context.getString(R.string.operation_manual)
//            TRACTOR -> context.getString(R.string.operation_tractor)
//            HERBICIDE -> context.getString(R.string.operation_herbicide)
//            NONE -> context.getString(R.string.operation_none)
            else -> ""
        }
    }
}