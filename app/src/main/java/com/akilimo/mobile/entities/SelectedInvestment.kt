package com.akilimo.mobile.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.akilimo.mobile.base.BaseEntity


@Entity(
    tableName = "selected_investments",
    foreignKeys = [
        ForeignKey(
            entity = AkilimoUser::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = InvestmentAmount::class,
            parentColumns = ["id"],
            childColumns = ["investment_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("user_id"), Index("investment_id")]
)
data class SelectedInvestment(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "user_id")
    val userId: Int,
    @ColumnInfo(name = "investment_id")
    val investmentId: Int,
    @ColumnInfo(name = "chosen_amount")
    val chosenAmount: Double,
    @ColumnInfo(name = "exact_amount")
    val isExactAmount: Boolean = false
) : BaseEntity()
