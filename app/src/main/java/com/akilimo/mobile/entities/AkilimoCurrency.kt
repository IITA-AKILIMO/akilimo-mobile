package com.akilimo.mobile.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.google.gson.annotations.SerializedName


@JsonIgnoreProperties(ignoreUnknown = true)
data class AkilimoCurrencyResponse(
    @JsonProperty("data")
    val data: List<AkilimoCurrency>
)

@Entity(
    tableName = "currencies",
    indices = [Index(value = ["currency_code"], unique = true)]
)
@JsonIgnoreProperties(ignoreUnknown = true)
data class AkilimoCurrency(
    @PrimaryKey(autoGenerate = false)
    @JsonProperty("id")
    val id: Int,

    @ColumnInfo(name = "country")
    @JsonProperty("country")
    val country: String? = null,

    @ColumnInfo(name = "country_code")
    @JsonProperty("country_code")
    val countryCode: String? = null,

    @ColumnInfo(name = "name")
    @JsonProperty("name")
    val currencyName: String? = null,

    @ColumnInfo(name = "currency_code")
    @JsonProperty("currency_code")
    val currencyCode: String? = null,

    @ColumnInfo(name = "currency_symbol")
    @JsonProperty("currency_symbol")
    val currencySymbol: String? = null,

    @ColumnInfo(name = "currency_native_symbol")
    @JsonProperty("currency_native_symbol")
    val currencyNativeSymbol: String? = null,

    @ColumnInfo(name = "currency_name_plural")
    @JsonProperty("currency_name_plural")
    val namePlural: String? = null,

    @ColumnInfo(name = "created_at")
    @JsonProperty("created_at")
    val createdAt: String? = null,

    @ColumnInfo(name = "updated_at")
    @JsonProperty("updated_at")
    val updatedAt: String? = null
)