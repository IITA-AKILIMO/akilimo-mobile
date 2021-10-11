package com.akilimo.mobile.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.akilimo.mobile.utils.enums.EnumMaizeProduceType

@Entity(tableName = "maize_market")
open class MaizeMarket {

    @PrimaryKey(autoGenerate = true)
    var id: Int? = null
    var produceRadioIndex = 0
    var grainUnitRadioIndex = 0
    var grainUnitPriceRadioIndex = 0
    var exactPrice: Double = 0.0
    var averagePrice = 0.0

    @Deprecated("To be removed in subsequent releases")
    var unitPrice: Double = 0.0
    var unitWeight: Int = 0


    var produceType: String = EnumMaizeProduceType.GRAIN.name
    var unitOfSale: String? = null
}
