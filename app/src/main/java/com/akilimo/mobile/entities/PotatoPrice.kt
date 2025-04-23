package com.akilimo.mobile.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty


@JsonIgnoreProperties(ignoreUnknown = true)
data class PotatoPriceResponse(
    @JsonProperty("data") val data: List<PotatoPrice>
)

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity(tableName = "potato_prices")
data class PotatoPrice(

    @PrimaryKey(autoGenerate = false)
    @JsonProperty("id")
    @ColumnInfo(name = "id")
    val id: Long,

    @ColumnInfo(name = "sort_order")
    @JsonProperty("sort_order")
    var sortOrder: Int = 0,

    @JsonProperty("country_code")
    @ColumnInfo(name = "country_code")
    val countryCode: String? = null,

    @JsonProperty("country_price")
    @ColumnInfo(name = "country_price")
    val countryPrice: String? = null,

    @JsonProperty("min_local_price")
    @ColumnInfo(name = "min_local_price")
    val minLocalPrice: Double = 0.0,

    @JsonProperty("max_local_price")
    @ColumnInfo(name = "max_local_price")
    val maxLocalPrice: Double = 0.0,

    @ColumnInfo(name = "min_allowed_price")
    @JsonProperty("min_allowed_price")
    var minAllowedPrice: Double = 0.0,

    @ColumnInfo(name = "max_allowed_price")
    @JsonProperty("max_allowed_price")
    var maxAllowedPrice: Double = 0.0,

    @ColumnInfo(name = "price_range")
    @JsonProperty("price_range")
    var priceRange: String? = null,

    @JsonProperty("active")
    @ColumnInfo(name = "active")
    val active: Boolean = false,

    @JsonProperty("average_price")
    @ColumnInfo(name = "average_price")
    val averagePrice: Double = 0.0,

    @ColumnInfo(name = "description")
    @JsonProperty("description")
    var description: String? = null
)
