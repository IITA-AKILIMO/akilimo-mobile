package com.akilimo.mobile.interfaces

fun interface IPriceDialogDismissListener {
    fun onDismiss(selectedPrice: Double, isExactPrice: Boolean)
}
