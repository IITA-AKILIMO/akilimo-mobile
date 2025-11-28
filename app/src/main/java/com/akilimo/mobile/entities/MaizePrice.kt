package com.akilimo.mobile.entities


import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.akilimo.mobile.base.BaseEntity

@Entity(tableName = "maize_prices")
data class MaizePrice(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "country_code") val countryCode: String,
    @ColumnInfo(name = "produce_type") val produceType: String,
    @ColumnInfo(name = "min_local_price") val minLocalPrice: Int,
    @ColumnInfo(name = "max_local_price") val maxLocalPrice: Int,
    @ColumnInfo(name = "average_price") val averagePrice: Int,
    @ColumnInfo(name = "exact_price") val exactPrice: Boolean,
    @ColumnInfo(name = "item_tag") val itemTag: String,
    @ColumnInfo(name = "min_allowed_price") val minAllowedPrice: Int,
    @ColumnInfo(name = "max_allowed_price") val maxAllowedPrice: Int,
    @ColumnInfo(name = "active") val active: Boolean,
    @ColumnInfo(name = "sort_order") val sortOrder: Int
): BaseEntity()
