package com.akilimo.mobile.entities

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
@Entity(
    tableName = "fertilizers",
    indices = [Index(value = ["fertilizer_key", "country_code"], unique = true)]
)
data class Fertilizer(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id")
    @Json(name = "id") val id: Int,

    @ColumnInfo(name = "image_id")
    @Json(name = "imageId") val imageId: Int = 0,

    @ColumnInfo(name = "country_code")
    @Json(name = "country_code") var countryCode: String? = null,

    @ColumnInfo(name = "fertilizer_key")
    @Json(name = "fertilizer_key") val fertilizerKey: String? = null,

    @ColumnInfo(name = "name")
    @Json(name = "name") val name: String? = null,

    @ColumnInfo(name = "type")
    @Json(name = "type") val fertilizerType: String? = null,

    @ColumnInfo(name = "weight")
    @Json(name = "weight") val weight: Int = 0,

    @ColumnInfo(name = "use_case")
    @Json(name = "use_case") val useCase: String? = null,

    @ColumnInfo(name = "available")
    @Json(name = "available") var available: Boolean = false,

    @ColumnInfo(name = "sort_order")
    @Json(name = "sort_order") val sortOrder: Int = 0,

    @ColumnInfo(name = "created_at")
    @Json(name = "created_at") val createdAt: String? = null,

    @ColumnInfo(name = "updated_at")
    @Json(name = "updated_at") val updatedAt: String? = null,

    @ColumnInfo(name = "price")
    @Json(name = "price") var price: Double = 0.0,

    @ColumnInfo(name = "currency_code")
    @Json(name = "currency_code") var currencyCode: String = "",

    @ColumnInfo(name = "price_range")
    @Json(name = "price_range") var priceRange: String? = null,

    @ColumnInfo(name = "price_per_bag")
    @Json(name = "price_per_bag") var pricePerBag: Double = 0.0,

    @ColumnInfo(name = "k_content")
    @Json(name = "k_content") var kContent: Int = 0,

    @ColumnInfo(name = "n_content")
    @Json(name = "n_content") var nContent: Int = 0,

    @ColumnInfo(name = "p_content")
    @Json(name = "p_content") var pContent: Int = 0,

    @ColumnInfo(name = "cim_available")
    @Json(name = "cim_available") var cimAvailable: Boolean = false,

    @ColumnInfo(name = "cis_available")
    @Json(name = "cis_available") var cisAvailable: Boolean = false,

    @ColumnInfo(name = "selected")
    @Json(name = "selected") var selected: Boolean = false,

    @ColumnInfo(name = "exact_price")
    @Json(name = "exact_price") var exactPrice: Boolean = false,

    @ColumnInfo(name = "custom")
    @Json(name = "custom") var custom: Boolean = false
) : Parcelable
