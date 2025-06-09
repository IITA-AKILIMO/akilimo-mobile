package com.akilimo.mobile.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
@Entity(
    tableName = "fertilizer_prices",
    indices = [Index(value = ["fertilizer_country"], unique = true)]
)
data class FertilizerPrice(

    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id")
    @Json(name = "id") val id: Int,

    @ColumnInfo(name = "record_id")
    @Json(name = "record_id")
    @Deprecated("Remove")
    var recordId: Int = 0,

    @ColumnInfo(name = "price_id")
    @Json(name = "price_id")
    @Deprecated("Remove")
    var priceId: Int = 0,

    @ColumnInfo(name = "sort_order")
    @Json(name = "sort_order")
    var sortOrder: Int = 0,

    @ColumnInfo(name = "min_local_price")
    @Json(name = "min_local_price")
    var minLocalPrice: Double = 0.0,


    @ColumnInfo(name = "max_local_price")
    @Json(name = "max_local_price")
    var maxLocalPrice: Double = 0.0,

    @ColumnInfo(name = "min_allowed_price")
    @Json(name = "min_allowed_price")
    var minAllowedPrice: Double = 0.0,

    @ColumnInfo(name = "max_allowed_price")
    @Json(name = "max_allowed_price")
    var maxAllowedPrice: Double = 0.0,

    @ColumnInfo(name = "price_per_bag")
    @Json(name = "price_per_bag")
    var pricePerBag: Double = 0.0,

    @ColumnInfo(name = "active")
    @Json(name = "active")
    var active: Boolean = false,

    @ColumnInfo(name = "price_range")
    @Json(name = "price_range")
    var priceRange: String? = null,

    @ColumnInfo(name = "country_code")
    @Json(name = "country_code")
    var countryCode: String? = null,

    @ColumnInfo(name = "fertilizer_country")
    @Json(name = "fertilizer_country")
    var fertilizerCountry: String? = null,

    @ColumnInfo(name = "fertilizer_key")
    @Json(name = "fertilizer_key")
    var fertilizerKey: String? = null,

    @ColumnInfo(name = "description")
    @Json(name = "description")
    var description: String? = null
)
