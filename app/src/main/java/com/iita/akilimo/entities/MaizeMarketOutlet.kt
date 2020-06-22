package com.iita.akilimo.entities

import com.iita.akilimo.utils.enums.EnumMaizeProduceType
import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable

@DatabaseTable
open class MaizeMarketOutlet {

    @DatabaseField(columnName = "id", generatedId = true)
    var id: Int? = null
    var produceRadioIndex = 0
    var grainUnitRadioIndex = 0
    var grainUnitPriceRadioIndex = 0
    var exactPrice = 0.0
    var averagePrice = 0.0
    var unitPrice: Double = 0.0
    var unitWeight: Int = 0


    var produceType: String = EnumMaizeProduceType.GRAIN.name
    var unitOfSale: String? = null
}