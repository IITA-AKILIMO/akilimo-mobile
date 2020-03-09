package com.iita.akilimo.interfaces

import com.iita.akilimo.utils.enums.EnumOperationType

interface IDismissOperationsDialogListener {
    fun onDismiss(operation: String, enumOperationType: EnumOperationType, cancelled: Boolean)
}