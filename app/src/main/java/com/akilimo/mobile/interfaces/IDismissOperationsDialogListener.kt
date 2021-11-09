package com.akilimo.mobile.interfaces

import com.akilimo.mobile.utils.enums.EnumOperationType

interface IDismissOperationsDialogListener {
    fun onDismiss(operation: String, enumOperationType: EnumOperationType, cancelled: Boolean)
}
