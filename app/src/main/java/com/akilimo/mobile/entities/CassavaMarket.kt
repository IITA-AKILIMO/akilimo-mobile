package com.akilimo.mobile.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.akilimo.mobile.utils.enums.EnumCassavaProduceType


@Entity(tableName = "cassava_markets")
data class CassavaMarket(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Int? = null,

    @ColumnInfo(name = "produce_type")
    var produceType: String = EnumCassavaProduceType.ROOTS.produce(),

    @ColumnInfo(name = "unit_of_sale")
    var unitOfSale: String? = null,

    @ColumnInfo(name = "unit_weight")
    var unitWeight: Int = 0,

    @ColumnInfo(name = "unit_price")
    var unitPrice: Double = 0.0,

    @ColumnInfo(name = "unit_price_p1")
    var unitPriceP1: Double = 0.0,

    @ColumnInfo(name = "unit_price_p2")
    var unitPriceP2: Double = 0.0,

    @ColumnInfo(name = "unit_price_m1")
    var unitPriceM1: Double = 0.0,

    @ColumnInfo(name = "unit_price_m2")
    var unitPriceM2: Double = 0.0,

    @ColumnInfo(name = "starch_factory")
    var starchFactory: String? = null,

    @ColumnInfo(name = "is_starch_factory_required")
    var isStarchFactoryRequired: Boolean = false
)
