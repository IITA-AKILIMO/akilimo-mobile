package com.akilimo.mobile.entities


import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.akilimo.mobile.entities.BaseEntity
import com.akilimo.mobile.enums.EnumCountry


@Entity(tableName = "cassava_market_prices")
data class CassavaMarketPrice(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "country_code") val countryCode: EnumCountry,
    @ColumnInfo(name = "currency_code") val currencyCode: String,
    @ColumnInfo(name = "currency_symbol") val currencySymbol: String,
    @ColumnInfo(name = "average_price") val averagePrice: Double,
    @ColumnInfo(name = "exact_price") val exactPrice: Boolean,
    @ColumnInfo(name = "item_tag") val itemTag: String,
    @ColumnInfo(name = "active") val active: Boolean,
    @ColumnInfo(name = "sort_order") val sortOrder: Int = 0
) : BaseEntity()
