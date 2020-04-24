package com.iita.akilimo.interfaces

import com.iita.akilimo.models.Fertilizer

interface IPriceDialogDismissListener {
    fun onDismiss(selectedPrice: Double, averagePrice: Double)
}
