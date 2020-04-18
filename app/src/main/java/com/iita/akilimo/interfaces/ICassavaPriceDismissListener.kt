package com.iita.akilimo.interfaces

import com.iita.akilimo.models.Fertilizer

interface ICassavaPriceDismissListener {
    fun onDismiss(selectedPrice: Double, averagePrice: Double)
}
