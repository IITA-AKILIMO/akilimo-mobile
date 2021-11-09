package com.akilimo.mobile.interfaces

import androidx.annotation.NonNull
import com.akilimo.mobile.entities.MaizePerformance
import org.jetbrains.annotations.NotNull

interface IMaizePerformanceDismissListener {
    fun onDismiss(
        @NonNull performance: @NotNull MaizePerformance,
        performanceConfirmed: Boolean
    )
}
