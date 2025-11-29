package com.akilimo.mobile.utils

fun Double?.orZero(): Double = this ?: 0.0
fun Double?.isPositive(): Boolean = (this ?: 0.0) > 0.0