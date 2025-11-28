package com.akilimo.mobile.enums

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.akilimo.mobile.R

enum class EnumMaizePerformance(
    @param:StringRes val label: Int,
    val performanceValue: Int,
    val description: String,
    @param:StringRes val performanceDesc: Int? = null,
    @param:DrawableRes val imageRes: Int
) {
    KNEE_HEIGHT(
        label = R.string.lbl_knee_height,
        performanceDesc = R.string.lbl_maize_performance_poor,
        description = "50",
        performanceValue = 1,
        imageRes = R.drawable.ic_maize_1
    ),
    CHEST_HEIGHT(
        label = R.string.lbl_chest_height, description = "150", // approx cm
        performanceValue = 2, imageRes = R.drawable.ic_maize_2
    ),
    YELLOWISH_LEAVES(
        label = R.string.lbl_yellowish_leaves,
        description = "yellow",
        performanceValue = 3,
        imageRes = R.drawable.ic_maize_3
    ),
    GREEN_LEAVES(
        label = R.string.lbl_green_leaves,
        description = "green",
        performanceValue = 4,
        imageRes = R.drawable.ic_maize_4
    ),
    DARK_GREEN_LEAVES(
        label = R.string.lbl_dark_green_leaves,
        performanceDesc = R.string.lbl_maize_performance_rich,
        description = "dark green",
        performanceValue = 5,
        imageRes = R.drawable.ic_maize_5
    )
}
