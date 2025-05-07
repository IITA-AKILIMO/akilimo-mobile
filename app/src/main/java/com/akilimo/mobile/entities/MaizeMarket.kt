package com.akilimo.mobile.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.akilimo.mobile.utils.enums.EnumMaizeProduceType

@Entity(tableName = "maize_market")
data class MaizeMarket(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Int? = null,

    @ColumnInfo(name = "produce_idx")
    var produceIdx: Int = 0,

    @ColumnInfo(name = "grain_unit_idx")
    var grainUnitIdx: Int = 0,

    @ColumnInfo(name = "grain_unit_price_idx")
    var grainUnitPriceIdx: Int = 0,

    @ColumnInfo(name = "exact_price")
    var exactPrice: Double = 0.0,

    @ColumnInfo(name = "average_price")
    var averagePrice: Double = 0.0,

    @ColumnInfo(name = "unit_price")
    @Deprecated("To be removed in subsequent releases")
    var unitPrice: Double = 0.0,

    @ColumnInfo(name = "unit_weight")
    var unitWeight: Double = 0.0,

    @ColumnInfo(name = "produce_type")
    var produceType: String = EnumMaizeProduceType.GRAIN.name,

    @ColumnInfo(name = "unit_of_sale")
    var unitOfSale: String? = null
)
