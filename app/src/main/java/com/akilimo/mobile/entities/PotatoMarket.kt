package com.akilimo.mobile.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.akilimo.mobile.utils.enums.EnumPotatoProduceType

@Entity(tableName = "potato_market")
open class PotatoMarket {

    @PrimaryKey(autoGenerate = true)
    var id: Int? = null
    var produceTypeRadioIndex = 0
    var potatoUnitOfSaleRadioIndex = 0
    var potatoUnitPriceRadioIndex = 0

    var unitPrice = 0.0
    var unitWeight: Int = 0

    var produceType: String = EnumPotatoProduceType.TUBERS.produce()
    var unitOfSale: String? = null
}
