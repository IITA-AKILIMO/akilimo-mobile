package com.iita.akilimo.entities

import com.iita.akilimo.utils.enums.EnumCassavaProduceType
import com.iita.akilimo.utils.enums.EnumUnitOfSale
import com.iita.akilimo.utils.enums.EnumUnitPrice
import com.orm.SugarRecord

class CassavaMarketOutlet : SugarRecord<CassavaMarketOutlet?>() {
    var starchFactory: String? = null
    var exactPrice = 0.0
    var averagePrice = 0.0
    var isStarchFactoryRequired = false
    var enumCassavaProduceType: EnumCassavaProduceType = EnumCassavaProduceType.ROOTS
    var enumUnitOfSale: EnumUnitOfSale = EnumUnitOfSale.UNIT_ONE_KG
    var enumUnitPrice: EnumUnitPrice = EnumUnitPrice.UNKNOWN
}