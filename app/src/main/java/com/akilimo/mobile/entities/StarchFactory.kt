package com.akilimo.mobile.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class StarchFactoryResponse(
    @JsonProperty("data")
    var data: List<StarchFactory>
)

@JsonIgnoreProperties(ignoreUnknown = true)
@Entity(tableName = "starch_factories")
data class StarchFactory(
    @JsonProperty("id")
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id")
    var id: Int,

    @JsonProperty("factory_name")
    @ColumnInfo(name = "factory_name")
    var factoryName: String? = null,

    @JsonProperty("factory_label")
    @ColumnInfo(name = "factory_label")
    var factoryLabel: String? = null,

    @JsonProperty("factory_name_country")
    @ColumnInfo(name = "factory_name_country")
    var factoryNameCountry: String? = null,

    @JsonProperty("country_code")
    @ColumnInfo(name = "country_code")
    var countryCode: String? = null,

    @JsonProperty("factory_active")
    @ColumnInfo(name = "factory_active")
    var factoryActive: Boolean = false,

    @JsonProperty("factory_selected")
    @ColumnInfo(name = "factory_selected")
    var factorySelected: Boolean = false
)
