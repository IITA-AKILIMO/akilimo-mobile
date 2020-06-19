package com.iita.akilimo.entities

import com.iita.akilimo.utils.enums.EnumCassavaProduceType
import com.iita.akilimo.utils.enums.EnumUnitOfSale
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class CassavaMarketOutlet : RealmObject() {
    @PrimaryKey
    var id: String? = null

    var starchFactory: String? = null

    @Deprecated("Remove this")
    var exactPrice: Double = 0.0
    var unitPrice: Double = 0.0
    var unitWeight: Int = 0

    var produceType: String = EnumCassavaProduceType.ROOTS.produce()
    var unitOfSale: String? = null
    var isStarchFactoryRequired: Boolean = false

}
