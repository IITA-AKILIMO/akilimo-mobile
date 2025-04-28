package com.akilimo.mobile.interfaces

import com.akilimo.mobile.entities.CropPerformance
import org.jetbrains.annotations.NotNull

interface ICropPerformanceListener {
    fun onDismiss(
        performance: @NotNull CropPerformance,
        performanceConfirmed: Boolean
    )
}
