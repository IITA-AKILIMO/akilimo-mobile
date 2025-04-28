package com.akilimo.mobile.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty


@JsonIgnoreProperties(ignoreUnknown = true)
data class FertilizerPriceResponse(
    @JsonProperty("data") val data: List<FertilizerPrice>
)

@JsonIgnoreProperties(ignoreUnknown = true)
@Entity(
    tableName = "fertilizer_prices",
    indices = [Index(value = ["fertilizer_country"], unique = true)]
)
data class FertilizerPrice(

    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id")
    @JsonProperty("id") val id: Int,

    @ColumnInfo(name = "record_id")
    @JsonProperty("record_id")
    @Deprecated("Remove")
    var recordId: Int = 0,

    @ColumnInfo(name = "price_id")
    @JsonProperty("price_id")
    @Deprecated("Remove")
    var priceId: Int = 0,

    @ColumnInfo(name = "sort_order")
    @JsonProperty("sort_order")
    var sortOrder: Int = 0,

    @ColumnInfo(name = "min_local_price")
    @JsonProperty("min_local_price")
    var minLocalPrice: Double = 0.0,


    @ColumnInfo(name = "max_local_price")
    @JsonProperty("max_local_price")
    var maxLocalPrice: Double = 0.0,

    @ColumnInfo(name = "min_allowed_price")
    @JsonProperty("min_allowed_price")
    var minAllowedPrice: Double = 0.0,

    @ColumnInfo(name = "max_allowed_price")
    @JsonProperty("max_allowed_price")
    var maxAllowedPrice: Double = 0.0,

    @ColumnInfo(name = "price_per_bag")
    @JsonProperty("price_per_bag")
    var pricePerBag: Double = 0.0,

    @ColumnInfo(name = "active")
    @JsonProperty("active")
    var active: Boolean = false,

    @ColumnInfo(name = "price_range")
    @JsonProperty("price_range")
    var priceRange: String? = null,

    @ColumnInfo(name = "country_code")
    @JsonProperty("country_code")
    var countryCode: String? = null,

    @ColumnInfo(name = "fertilizer_country")
    @JsonProperty("fertilizer_country")
    var fertilizerCountry: String? = null,

    @ColumnInfo(name = "fertilizer_key")
    @JsonProperty("fertilizer_key")
    var fertilizerKey: String? = null,

    @ColumnInfo(name = "description")
    @JsonProperty("description")
    var description: String? = null
)