package com.akilimo.mobile.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class StarchFactoryResponse(
    @Json(name = "data")
    var data: List<StarchFactory>
)

@JsonClass(generateAdapter = true)
@Entity(tableName = "starch_factories")
data class StarchFactory(
    @Json(name = "id")
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id")
    var id: Int,

    @Json(name = "factory_name")
    @ColumnInfo(name = "factory_name")
    var factoryName: String? = null,

    @Json(name = "factory_label")
    @ColumnInfo(name = "factory_label")
    var factoryLabel: String? = null,

    @Json(name = "factory_name_country")
    @ColumnInfo(name = "factory_name_country")
    var factoryNameCountry: String? = null,

    @Json(name = "country_code")
    @ColumnInfo(name = "country_code")
    var countryCode: String? = null,

    @Json(name = "factory_active")
    @ColumnInfo(name = "factory_active")
    var factoryActive: Boolean = false,

    @Json(name = "factory_selected")
    @ColumnInfo(name = "factory_selected")
    var factorySelected: Boolean = false
)
