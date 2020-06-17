package com.iita.akilimo.entities

import com.iita.akilimo.utils.enums.EnumCassavaProduceType
import com.iita.akilimo.utils.enums.EnumUnitOfSale
import io.realm.RealmObject

open class CassavaMarketOutlet : RealmObject() {
    var starchFactory: String? = null
    var exactPrice: Double = 0.0
    var unitPrice: Double = 0.0
    var unitWeight: Double = 0.0

    var produceType: String = EnumCassavaProduceType.ROOTS.produce()
    var unitOfSale: String = EnumUnitOfSale.ONE_KG.unitOfSale()
    var isStarchFactoryRequired: Boolean = false

}
