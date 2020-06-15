package com.iita.akilimo.entities

import com.iita.akilimo.utils.enums.EnumPotatoProduceType
import com.iita.akilimo.utils.enums.EnumUnitOfSale
import com.orm.SugarRecord

class PotatoMarketOutlet : SugarRecord<PotatoMarketOutlet?>() {
    var id: Long = 0
    var produceTypeRadioIndex = 0
    var potatoUnitOfSaleRadioIndex = 0
    var potatoUnitPriceRadioIndex = 0
    var exactPrice = 0.0
    var enumPotatoProduceType: EnumPotatoProduceType = EnumPotatoProduceType.TUBERS
    var enumUnitOfSale: EnumUnitOfSale = EnumUnitOfSale.UNIT_ONE_KG
}