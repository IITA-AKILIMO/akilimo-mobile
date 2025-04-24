package com.akilimo.mobile.utils

fun String?.orIfBlank(default: String): String = if (this.isNullOrBlank()) default else this
