package com.iita.akilimo.interfaces

import androidx.annotation.NonNull
import com.iita.akilimo.entities.Fertilizer
import com.iita.akilimo.entities.FieldYield
import org.jetbrains.annotations.NotNull

interface IFieldYieldDismissListener {
    fun onDismiss(
        @NonNull fertilizer: @NotNull FieldYield,
        yieldConfirmed: Boolean
    )
}
