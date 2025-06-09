package com.akilimo.mobile.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class InvestmentAmountResponse(
    @Json(name = "data") var data: List<InvestmentAmount>
)

@JsonClass(generateAdapter = true)
@Entity(
    tableName = "investment_amounts",
)
data class InvestmentAmount(

    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id")
    @Json(name = "id")
    var id: Int? = null,

    @ColumnInfo(name = "item_tag")
    @Json(name = "item_tag")
    var itemTag: String? = null,

    @ColumnInfo(name = "country_code")
    @Json(name = "country_code")
    var countryCode: String? = null,

    @ColumnInfo(name = "investment_amount")
    @Json(name = "investment_amount")
    var investmentAmount: Double = 0.0,

    @ColumnInfo(name = "min_investment_amount")
    @Json(name = "min_investment_amount")
    var minInvestmentAmount: Double = 0.0,

    @ColumnInfo(name = "max_investment_amount")
    @Json(name = "max_investment_amount")
    var maxInvestmentAmount: Double = 0.0,

    @ColumnInfo(name = "area_unit")
    @Json(name = "area_unit")
    var areaUnit: String? = null,

    @ColumnInfo(name = "field_size")
    @Json(name = "field_size")
    var fieldSize: Double = 0.0,

    @ColumnInfo(name = "price_active")
    @Json(name = "price_active")
    var priceActive: Boolean = false,

    @ColumnInfo(name = "sort_order")
    @Json(name = "sort_order")
    var sortOrder: Long = 0,

    @ColumnInfo(name = "created_at")
    @Json(name = "created_at")
    var createdAt: String? = null,

    @ColumnInfo(name = "updated_at")
    @Json(name = "updated_at")
    var updatedAt: String? = null
)
