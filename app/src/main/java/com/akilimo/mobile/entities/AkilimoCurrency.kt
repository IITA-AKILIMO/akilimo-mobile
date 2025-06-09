package com.akilimo.mobile.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class AkilimoCurrencyResponse(
    @Json(name = "data")
    val data: List<AkilimoCurrency>
)

@Entity(
    tableName = "currencies",
    indices = [Index(value = ["currency_code"], unique = true)]
)
@JsonClass(generateAdapter = true)
data class AkilimoCurrency(
    @PrimaryKey(autoGenerate = false)
    @Json(name = "id")
    val id: Int,

    @ColumnInfo(name = "country")
    @Json(name = "country")
    val country: String? = null,

    @ColumnInfo(name = "country_code")
    @Json(name = "country_code")
    val countryCode: String? = null,

    @ColumnInfo(name = "name")
    @Json(name = "name")
    val currencyName: String = "",

    @ColumnInfo(name = "currency_code")
    @Json(name = "currency_code")
    val currencyCode: String = "",

    @ColumnInfo(name = "currency_symbol")
    @Json(name = "currency_symbol")
    val currencySymbol: String = "",

    @ColumnInfo(name = "currency_native_symbol")
    @Json(name = "currency_native_symbol")
    val currencyNativeSymbol: String = "",

    @ColumnInfo(name = "currency_name_plural")
    @Json(name = "currency_name_plural")
    val namePlural: String? = null,

    @ColumnInfo(name = "created_at")
    @Json(name = "created_at")
    val createdAt: String? = null,

    @ColumnInfo(name = "updated_at")
    @Json(name = "updated_at")
    val updatedAt: String? = null
)