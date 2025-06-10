package com.akilimo.mobile.interfaces

import com.akilimo.mobile.utils.enums.EnumOperationType

fun interface IDismissOperationsDialogListener {
    fun onDismiss(enumOperationType: EnumOperationType, cancelled: Boolean)
}
