package com.akilimo.mobile.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class CassavaPricePriceResponse(
    @Json(name = "data") val data: List<CassavaPrice>
)

@JsonClass(generateAdapter = true)
@Entity(tableName = "cassava_prices")
data class CassavaPrice(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id")
    @Json(name = "id")
    val id: Int,

    @ColumnInfo(name = "item_tag")
    @Json(name = "item_tag")
    val itemTag: String = "",


    @ColumnInfo(name = "country_code")
    @Json(name = "country_code")
    val countryCode: String? = null,

    @ColumnInfo(name = "min_local_price")
    @Json(name = "min_local_price")
    val minLocalPrice: Double = 0.0,

    @ColumnInfo(name = "max_local_price")
    @Json(name = "max_local_price")
    val maxLocalPrice: Double = 0.0,

    @ColumnInfo(name = "average_price")
    @Json(name = "average_price")
    val averagePrice: Double = 0.0,

    @ColumnInfo(name = "min_allowed_price")
    @Json(name = "min_allowed_price")
    val minAllowedPrice: Double = 0.0,

    @ColumnInfo(name = "max_allowed_price")
    @Json(name = "max_allowed_price")
    val maxAllowedPrice: Double = 0.0,

    @ColumnInfo(name = "active")
    @Json(name = "active")
    val active: Boolean = false,

    @ColumnInfo(name = "exact_price")
    @Json(name = "exact_price")
    val exactPrice: Boolean = false

)
