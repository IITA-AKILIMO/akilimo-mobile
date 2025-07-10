package com.akilimo.mobile.data

import com.akilimo.mobile.entities.CassavaMarket
import com.akilimo.mobile.entities.StarchFactory
import com.akilimo.mobile.utils.enums.EnumCassavaProduceType

data class CassavaMarketUiState(
    val selectedFactory: String = "NA",
    val produceType: String = EnumCassavaProduceType.ROOTS.name,
    val unitPrice: Double = 0.0,
    val unitPriceP1: Double = 0.0,
    val unitPriceP2: Double = 0.0,
    val unitPriceM1: Double = 0.0,
    val unitPriceM2: Double = 0.0,
    val unitOfSale: String = "NA",
    val unitWeight: Double = 0.0,
    val harvestWindow: Int = 0,
    val countryCode: String = "",
    val currencyCode: String = "",
    val starchFactories: List<StarchFactory> = emptyList()
) {
    fun toEntity() = CassavaMarket(
        starchFactory = selectedFactory,
        produceType = produceType,
        unitPrice = unitPrice,
        unitPriceP1 = unitPriceP1,
        unitPriceP2 = unitPriceP2,
        unitPriceM1 = unitPriceM1,
        unitPriceM2 = unitPriceM2,
        unitOfSale = unitOfSale,
        isStarchFactoryRequired = selectedFactory != "NA"
    )
}


data class WarningMessage(
    val title: String,
    val message: String,
    val positiveButtonLabel: String = "OK"
)