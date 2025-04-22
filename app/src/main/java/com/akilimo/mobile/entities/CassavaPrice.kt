package com.akilimo.mobile.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.akilimo.mobile.entities.FertilizerPrice
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty


@JsonIgnoreProperties(ignoreUnknown = true)
data class CassavaPricePriceResponse(
    @JsonProperty("data") val data: List<CassavaPrice>
)

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity(tableName = "cassava_prices")
data class CassavaPrice(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id")
    @JsonProperty("id") val id: Int,

    @ColumnInfo(name = "price_index")
    @JsonProperty("price_index")
    val priceIndex: Long? = null,

    
    @ColumnInfo(name = "price_id")
    @JsonProperty("price_id")
    val priceId: Long = 0,

    @ColumnInfo(name = "country_code")
    @JsonProperty("country_code")
    val countryCode: String? = null,

    @ColumnInfo(name = "min_local_price")
    @JsonProperty("min_local_price")
    val minLocalPrice: Double = 0.0,

    @ColumnInfo(name = "max_local_price")
    @JsonProperty("max_local_price")
    val maxLocalPrice: Double = 0.0,

    @ColumnInfo(name = "min_usd")
    @JsonProperty("min_usd")
    val minUsd: Double = 0.0,

    @ColumnInfo(name = "max_usd")
    @JsonProperty("max_usd")
    val maxUsd: Double = 0.0,

    @ColumnInfo(name = "min_allowed_price")
    @JsonProperty("min_allowed_price")
    val minAllowedPrice: Double = 0.0,

    @ColumnInfo(name = "max_allowed_price")
    @JsonProperty("max_allowed_price")
    val maxAllowedPrice: Double = 0.0,

    @ColumnInfo(name = "active")
    @JsonProperty("active")
    val active: Boolean = false,

    @ColumnInfo(name = "average_price")
    @JsonProperty("average_price")
    val averagePrice: Double = 0.0
)