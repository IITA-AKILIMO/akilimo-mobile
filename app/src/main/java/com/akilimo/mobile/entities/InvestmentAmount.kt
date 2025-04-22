package com.akilimo.mobile.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class InvestmentAmountResponse(
    @JsonProperty("data") var data: List<InvestmentAmount>
)

@JsonIgnoreProperties(ignoreUnknown = true)
@Entity(
    tableName = "investment_amounts",
)
data class InvestmentAmount(

    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id")
    @JsonProperty("id")
    var id: Int? = null,

    @ColumnInfo(name = "country_code")
    @JsonProperty("country_code")
    var countryCode: String? = null,

    @ColumnInfo(name = "investment_amount")
    @JsonProperty("investment_amount")
    var investmentAmount: Double = 0.0,

    @ColumnInfo(name = "min_investment_amount")
    @JsonProperty("min_investment_amount")
    var minInvestmentAmount: Double = 0.0,

    @ColumnInfo(name = "max_investment_amount")
    @JsonProperty("max_investment_amount")
    var maxInvestmentAmount: Double = 0.0,

    @ColumnInfo(name = "area_unit")
    @JsonProperty("area_unit")
    var areaUnit: String? = null,

    @ColumnInfo(name = "field_size")
    @JsonProperty("field_size")
    var fieldSize: Double = 0.0,

    @ColumnInfo(name = "price_active")
    @JsonProperty("price_active")
    var priceActive: Boolean = false,

    @ColumnInfo(name = "sort_order")
    @JsonProperty("sort_order")
    var sortOrder: Long = 0,

    @ColumnInfo(name = "created_at")
    @JsonProperty("created_at")
    var createdAt: String? = null,

    @ColumnInfo(name = "updated_at")
    @JsonProperty("updated_at")
    var updatedAt: String? = null
)
