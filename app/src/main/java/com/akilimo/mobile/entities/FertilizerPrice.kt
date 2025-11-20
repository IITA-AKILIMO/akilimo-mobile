package com.akilimo.mobile.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.akilimo.mobile.base.BaseEntity

@Entity(tableName = "fertilizer_prices")
data class FertilizerPrice(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "fertilizer_key") val fertilizerKey: String,
    @ColumnInfo(name = "fertilizer_country") val fertilizerCountry: String,
    @ColumnInfo(name = "country_code") val countryCode: String,
    @ColumnInfo(name = "currency_code") val currencyCode: String,
    @ColumnInfo(name = "currency_symbol") val currencySymbol: String,
    @ColumnInfo(name = "sort_order") val sortOrder: Int = 0,
    @ColumnInfo(name = "min_local_price") val minLocalPrice: Double = 0.0,
    @ColumnInfo(name = "max_local_price") val maxLocalPrice: Double = 0.0,
    @ColumnInfo(name = "min_allowed_price") val minAllowedPrice: Double,
    @ColumnInfo(name = "max_allowed_price") val maxAllowedPrice: Double,
    @ColumnInfo(name = "price_per_bag") val pricePerBag: Double = 0.0,
    @ColumnInfo(name = "price_range") val priceRange: String,
    @ColumnInfo(name = "active") val isActive: Boolean = true,
    @ColumnInfo(name = "description") val description: String?,
) : BaseEntity()

