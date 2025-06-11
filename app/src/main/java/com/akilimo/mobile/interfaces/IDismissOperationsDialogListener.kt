package com.akilimo.mobile.interfaces

import com.akilimo.mobile.utils.enums.EnumOperationMethod

fun interface IDismissOperationsDialogListener {
    fun onDismiss(enumOperationMethod: EnumOperationMethod, cancelled: Boolean)
}
