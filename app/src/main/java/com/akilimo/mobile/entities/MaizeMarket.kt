package com.akilimo.mobile.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.akilimo.mobile.base.BaseEntity
import com.akilimo.mobile.enums.EnumMaizeProduceType
import com.akilimo.mobile.enums.EnumUnitOfSale

@Entity(
    tableName = "maize_markets",
    foreignKeys = [
        ForeignKey(
            entity = AkilimoUser::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["user_id"]),
        Index(value = ["user_id", "produce_type"], unique = true)
    ]
)
data class MaizeMarket(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @ColumnInfo(name = "user_id")
    val userId: Int,

    @ColumnInfo(name = "unit_price")
    val unitPrice: Double,

    @ColumnInfo(name = "produce_type")
    val produceType: EnumMaizeProduceType,

    @ColumnInfo(name = "unit_of_sale")
    val unitOfSale: EnumUnitOfSale
) : BaseEntity()
