package com.akilimo.mobile.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.akilimo.mobile.utils.enums.EnumCassavaProduceType


@Entity(tableName = "cassava_market")
open class CassavaMarket {
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null

    var produceType: String = EnumCassavaProduceType.ROOTS.produce()
    var unitOfSale: String? = null

    var unitWeight: Int = 0
    var unitPrice: Double = 0.0
    var unitPriceP1: Double = 0.0
    var unitPriceP2: Double = 0.0
    var unitPriceM1: Double = 0.0
    var unitPriceM2: Double = 0.0

    var starchFactory: String? = null
    var isStarchFactoryRequired: Boolean = false

}
