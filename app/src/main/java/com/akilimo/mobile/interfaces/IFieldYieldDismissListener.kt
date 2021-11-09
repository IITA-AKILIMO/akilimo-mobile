package com.akilimo.mobile.interfaces

import androidx.annotation.NonNull
import com.akilimo.mobile.entities.FieldYield
import org.jetbrains.annotations.NotNull

interface IFieldYieldDismissListener {
    fun onDismiss(
        @NonNull fertilizer: @NotNull FieldYield,
        yieldConfirmed: Boolean
    )
}
