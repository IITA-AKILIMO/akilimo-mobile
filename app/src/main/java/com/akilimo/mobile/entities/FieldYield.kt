package com.akilimo.mobile.entities

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "field_yields")
data class FieldYield(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Int? = null,

    @ColumnInfo(name = "yield_amount")
    var yieldAmount: Double = 0.0,

    @ColumnInfo(name = "field_yield_label")
    var fieldYieldLabel: String? = null,

    @ColumnInfo(name = "field_yield_desc")
    var fieldYieldDesc: String? = null,

    @ColumnInfo(name = "field_yield_amount_label")
    var fieldYieldAmountLabel: String? = null,

    @ColumnInfo(name = "image_id")
    var imageId: Int = 0
) : Parcelable
