package com.akilimo.mobile.utils.enums

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
enum class EnumOperation : Parcelable {
    TILLAGE, HARROWING, RIDGING, WEEDING
}
