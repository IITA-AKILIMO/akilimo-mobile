package com.akilimo.mobile.interfaces

import com.akilimo.mobile.entities.FieldYield
import org.jetbrains.annotations.NotNull

interface IFieldYieldDismissListener {
    fun onDismiss(
        fieldYield: @NotNull FieldYield,
        yieldConfirmed: Boolean
    )
}
