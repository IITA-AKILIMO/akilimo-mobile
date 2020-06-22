package com.iita.akilimo.entities

import com.iita.akilimo.utils.enums.EnumCassavaProduceType
import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable


@DatabaseTable(tableName = "cassava_market")
open class CassavaMarketOutlet {
    @DatabaseField(columnName = "id", generatedId = true)
    var id: Int? = null

    var starchFactory: String? = null

    @Deprecated("Remove this")
    var exactPrice: Double = 0.0
    var unitPrice: Double = 0.0
    var unitWeight: Int = 0

    var produceType: String = EnumCassavaProduceType.ROOTS.produce()
    var unitOfSale: String? = null
    var isStarchFactoryRequired: Boolean = false

}
