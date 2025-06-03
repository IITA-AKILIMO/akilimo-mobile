package com.akilimo.mobile.models

import android.graphics.drawable.Drawable
import com.akilimo.mobile.utils.enums.EnumAdvice

data class Recommendation(
    val recommendationName: String,
    val recCode: EnumAdvice,
    val background: Drawable? = null
)
