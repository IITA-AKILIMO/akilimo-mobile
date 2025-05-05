package com.akilimo.mobile.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.akilimo.mobile.utils.enums.EnumPotatoProduceType

@Entity(tableName = "potato_markets")
data class PotatoMarket(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Int? = null,

    @ColumnInfo(name = "unit_price")
    var unitPrice: Double = 0.0,

    @ColumnInfo(name = "unit_weight")
    var unitWeight: Double = 0.0,

    @ColumnInfo(name = "produce_type")
    var produceType: String = EnumPotatoProduceType.TUBERS.produce(),

    @ColumnInfo(name = "unit_of_sale")
    var unitOfSale: String? = null,

    @ColumnInfo(name = "produce_type_idx")
    var produceTypeIdx: Int = 0,

    @ColumnInfo(name = "potato_unit_of_sale_idx")
    var potatoUnitOfSaleIdx: Int = 0,

    @ColumnInfo(name = "potato_unit_price_idx")
    var potatoUnitPriceIdx: Int = 0
)
