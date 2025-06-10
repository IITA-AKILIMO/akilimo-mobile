package com.akilimo.mobile.utils.enums

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
enum class EnumOperationMethod : Parcelable {
    MANUAL,
    TRACTOR,
    NONE
}

@Parcelize
enum class EnumWeedControlMethod : Parcelable {
    MANUAL,
    HERBICIDE,
    HERBICIDE_MANUAL,
    NONE
}