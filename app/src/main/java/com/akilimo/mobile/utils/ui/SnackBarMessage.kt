package com.akilimo.mobile.utils.ui

import androidx.annotation.StringRes

sealed class SnackBarMessage {
    data class Text(val message: String) : SnackBarMessage()
    data class Resource(@StringRes val resId: Int) : SnackBarMessage()
}