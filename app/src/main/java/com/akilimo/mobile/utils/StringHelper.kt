package com.akilimo.mobile.utils

import android.content.Context

object StringHelper {

    fun Context.formatWithLandSize(
        resId: Int,
        farmSize: Double?,
        sizeUnit: String
    ): String {
        return getString(resId)
            .replace("{farm_size}", farmSize.toString())
            .replace("{size_unit}", sizeUnit)
    }
}

fun String?.orUnavailable(default: String): String {
    return this?.takeIf { it.isNotBlank() } ?: default
}
