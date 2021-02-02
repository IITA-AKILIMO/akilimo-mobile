package com.iita.akilimo.interfaces

import androidx.annotation.NonNull
import com.iita.akilimo.entities.Fertilizer
import com.iita.akilimo.entities.FieldYield
import com.iita.akilimo.entities.MaizePerformance
import org.jetbrains.annotations.NotNull

interface IMaizePerformanceDismissListener {
    fun onDismiss(
        @NonNull performance: @NotNull MaizePerformance,
        performanceConfirmed: Boolean
    )
}
