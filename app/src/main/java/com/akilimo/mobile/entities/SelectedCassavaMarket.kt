package com.akilimo.mobile.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.akilimo.mobile.base.BaseEntity
import com.akilimo.mobile.enums.EnumCassavaProduceType
import com.akilimo.mobile.enums.EnumUnitOfSale

@Entity(
    tableName = "selected_cassava_markets",
    foreignKeys = [
        ForeignKey(
            entity = AkilimoUser::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CassavaMarketPrice::class,
            parentColumns = ["id"],
            childColumns = ["market_price_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CassavaUnit::class,
            parentColumns = ["id"],
            childColumns = ["cassava_unit_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = StarchFactory::class,
            parentColumns = ["id"],
            childColumns = ["starch_factory_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CassavaYield::class,
            parentColumns = ["id"],
            childColumns = ["yield_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["user_id"]),
        Index(value = ["starch_factory_id"]),
        Index(value = ["market_price_id"]),
        Index(value = ["cassava_unit_id"]),
        Index(value = ["yield_id"]),
    ]
)
data class SelectedCassavaMarket(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int? = null,

    @ColumnInfo(name = "user_id")
    val userId: Int,

    @ColumnInfo(name = "market_price_id")
    val marketPriceId: Int? = null,

    @ColumnInfo(name = "starch_factory_id")
    val starchFactoryId: Int? = null,

    @ColumnInfo(name = "cassava_unit_id")
    val cassavaUnitId: Int? = null,

    @ColumnInfo(name = "yield_id")
    val yieldId: Int? = null,

    @ColumnInfo(name = "produce_type")
    val produceType: EnumCassavaProduceType = EnumCassavaProduceType.ROOTS,

    @ColumnInfo(name = "unit_of_sale")
    val unitOfSale: EnumUnitOfSale = EnumUnitOfSale.THOUSAND_KG,

    @ColumnInfo(name = "unit_price")
    val unitPrice: Double = 0.0,

    @ColumnInfo(name = "unit_price_p1")
    val unitPriceP1: Double = 0.0,

    @ColumnInfo(name = "unit_price_p2")
    val unitPriceP2: Double = 0.0,

    @ColumnInfo(name = "unit_price_m1")
    val unitPriceM1: Double = 0.0,

    @ColumnInfo(name = "unit_price_m2")
    val unitPriceM2: Double = 0.0
) : BaseEntity()
