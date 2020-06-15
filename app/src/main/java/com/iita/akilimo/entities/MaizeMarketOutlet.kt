package com.iita.akilimo.entities

import com.iita.akilimo.utils.enums.EnumMaizeProduceType
import com.iita.akilimo.utils.enums.EnumUnitOfSale
import com.iita.akilimo.utils.enums.EnumUnitPrice
import com.orm.SugarRecord

class MaizeMarketOutlet : SugarRecord<MaizeMarketOutlet>() {
    var id: Long = 0
    var produceRadioIndex = 0
    var grainUnitRadioIndex = 0
     var grainUnitPriceRadioIndex = 0
     var exactPrice = 0.0
     var averagePrice = 0.0
     var enumMaizeProduceType: EnumMaizeProduceType = EnumMaizeProduceType.GRAIN
     var enumUnitOfSale: EnumUnitOfSale = EnumUnitOfSale.UNIT_ONE_KG
     var enumUnitPrice: EnumUnitPrice = EnumUnitPrice.UNKNOWN
}