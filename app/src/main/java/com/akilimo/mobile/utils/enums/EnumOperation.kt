package com.akilimo.mobile.utils.enums

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
enum class EnumOperation : Parcelable {
    TILLAGE, HARROWING, RIDGING, WEEDING
}
