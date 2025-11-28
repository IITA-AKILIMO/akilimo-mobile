package com.akilimo.mobile.entities


import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.akilimo.mobile.base.BaseEntity

@Entity(tableName = "investment_amounts")
data class InvestmentAmount(
    @PrimaryKey @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "country_code") val countryCode: String,
    @ColumnInfo(name = "currency_code") val currencyCode: String,
    @ColumnInfo(name = "currency_symbol") val currencySymbol: String,
    @ColumnInfo(name = "investment_amount") val investmentAmount: Double,
    @ColumnInfo(name = "exact_amount") val exactAmount: Boolean,
    @ColumnInfo(name = "active") val active: Boolean,
    @ColumnInfo(name = "area_unit") val areaUnit: String,
    @ColumnInfo(name = "sort_order") val sortOrder: Int,
    @ColumnInfo(name = "is_custom") val isCustom: Boolean = false
) : BaseEntity()