package com.akilimo.mobile.utils.enums

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
enum class EnumOperationType : Parcelable {
    MANUAL,
    TRACTOR,
    HERBICIDE,
    NONE
}