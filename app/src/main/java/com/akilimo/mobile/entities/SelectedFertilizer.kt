package com.akilimo.mobile.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.akilimo.mobile.base.BaseEntity

@Entity(
    tableName = "selected_fertilizers",
    foreignKeys = [
        ForeignKey(
            entity = AkilimoUser::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Fertilizer::class,
            parentColumns = ["id"],
            childColumns = ["fertilizer_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = FertilizerPrice::class,
            parentColumns = ["id"],
            childColumns = ["fertilizer_price_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("user_id"), Index("fertilizer_id")]
)
data class SelectedFertilizer(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "user_id")
    val userId: Int,
    @ColumnInfo(name = "fertilizer_id")
    val fertilizerId: Int,
    @ColumnInfo(name = "fertilizer_price_id")
    val fertilizerPriceId: Int?,
    @ColumnInfo(name = "fertilizer_price")
    val fertilizerPrice: Double = 0.0,
    @ColumnInfo(name = "display_price")
    val displayPrice: String? = null,
    @ColumnInfo(name = "exact_price")
    val isExactPrice: Boolean = false
) : BaseEntity()
