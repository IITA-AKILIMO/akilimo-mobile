package com.akilimo.mobile.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class PotatoPriceResponse(
    @field:Json(name = "data") val data: List<PotatoPrice>
)

@JsonClass(generateAdapter = true)
@Entity(tableName = "potato_prices")
data class PotatoPrice(

    @PrimaryKey(autoGenerate = false)
    @field:Json(name = "id")
    @ColumnInfo(name = "id")
    val id: Long,

    @ColumnInfo(name = "sort_order")
    @field:Json(name = "sort_order")
    var sortOrder: Int = 0,

    @field:Json(name = "country_code")
    @ColumnInfo(name = "country_code")
    val countryCode: String? = null,

    @field:Json(name = "country_price")
    @ColumnInfo(name = "country_price")
    val countryPrice: String? = null,

    @field:Json(name = "min_local_price")
    @ColumnInfo(name = "min_local_price")
    val minLocalPrice: Double = 0.0,

    @field:Json(name = "max_local_price")
    @ColumnInfo(name = "max_local_price")
    val maxLocalPrice: Double = 0.0,

    @ColumnInfo(name = "min_allowed_price")
    @field:Json(name = "min_allowed_price")
    var minAllowedPrice: Double = 0.0,

    @ColumnInfo(name = "max_allowed_price")
    @field:Json(name = "max_allowed_price")
    var maxAllowedPrice: Double = 0.0,

    @ColumnInfo(name = "price_range")
    @field:Json(name = "price_range")
    var priceRange: String? = null,

    @field:Json(name = "active")
    @ColumnInfo(name = "active")
    val active: Boolean = false,

    @field:Json(name = "average_price")
    @ColumnInfo(name = "average_price")
    val averagePrice: Double = 0.0,

    @ColumnInfo(name = "description")
    @field:Json(name = "description")
    var description: String? = null
)
