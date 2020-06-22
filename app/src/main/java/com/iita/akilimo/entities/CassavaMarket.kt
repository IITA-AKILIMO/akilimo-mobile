package com.iita.akilimo.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.iita.akilimo.utils.enums.EnumCassavaProduceType


@Entity(tableName = "cassava_market")
open class CassavaMarket {
    @PrimaryKey(autoGenerate = true)
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
