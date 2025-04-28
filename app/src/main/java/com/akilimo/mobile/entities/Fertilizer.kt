package com.akilimo.mobile.entities

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.parcelize.Parcelize


@JsonIgnoreProperties(ignoreUnknown = true)
data class FertilizerResponse(
    @JsonProperty("data") val data: List<Fertilizer>
)

@Parcelize
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity(
    tableName = "fertilizers",
    indices = [Index(value = ["fertilizer_key", "country_code"], unique = true)]
)
data class Fertilizer(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id")
    @JsonProperty("id") val id: Int,

    @ColumnInfo(name = "image_id")
    @JsonProperty("imageId") val imageId: Int = 0,

    @ColumnInfo(name = "country_code")
    @JsonProperty("country_code") var countryCode: String? = null,

    @ColumnInfo(name = "fertilizer_key")
    @JsonProperty("fertilizer_key") val fertilizerKey: String? = null,

    @ColumnInfo(name = "name")
    @JsonProperty("name") val name: String? = null,

    @ColumnInfo(name = "type")
    @JsonProperty("type") val fertilizerType: String? = null,

    @ColumnInfo(name = "weight")
    @JsonProperty("weight") val weight: Int = 0,

    @ColumnInfo(name = "use_case")
    @JsonProperty("use_case") val useCase: String? = null,

    @ColumnInfo(name = "available")
    @JsonProperty("available") var available: Boolean = false,

    @ColumnInfo(name = "sort_order")
    @JsonProperty("sort_order") val sortOrder: Int = 0,

    @ColumnInfo(name = "created_at")
    @JsonProperty("created_at") val createdAt: String? = null,

    @ColumnInfo(name = "updated_at")
    @JsonProperty("updated_at") val updatedAt: String? = null,

    @ColumnInfo(name = "price")
    @JsonProperty("price") var price: Double = 0.0,

    @ColumnInfo(name = "currency_code")
    @JsonProperty("currency_code") var currencyCode: String? = null,

    @ColumnInfo(name = "price_range")
    @JsonProperty("price_range") var priceRange: String? = null,

    @ColumnInfo(name = "price_per_bag")
    @JsonProperty("price_per_bag") var pricePerBag: Double = 0.0,

    @ColumnInfo(name = "k_content")
    @JsonProperty("k_content") var kContent: Int = 0,

    @ColumnInfo(name = "n_content")
    @JsonProperty("n_content") var nContent: Int = 0,

    @ColumnInfo(name = "p_content")
    @JsonProperty("p_content") var pContent: Int = 0,

    @ColumnInfo(name = "cim_available")
    @JsonProperty("cim_available") var cimAvailable: Boolean = false,

    @ColumnInfo(name = "cis_available")
    @JsonProperty("cis_available") var cisAvailable: Boolean = false,

    @ColumnInfo(name = "selected")
    @JsonProperty("selected") var selected: Boolean = false,

    @ColumnInfo(name = "exact_price")
    @JsonProperty("exact_price") var exactPrice: Boolean = false,

    @ColumnInfo(name = "custom")
    @JsonProperty("custom") var custom: Boolean = false
) : Parcelable
