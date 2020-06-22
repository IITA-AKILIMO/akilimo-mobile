package com.iita.akilimo.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.google.android.gms.common.util.Strings

@JsonIgnoreProperties(ignoreUnknown = true)
@Entity(tableName = "starch_factory")
open class StarchFactory {
    @JsonProperty("id")
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

    @JsonProperty("factoryName")
    var factoryName: String? = null


    @JsonProperty("factoryLabel")
    var factoryLabel: String? = null

    @JsonProperty("factoryNameCountry")
    var factoryNameCountry: String? = null

    @JsonProperty("countryCode")
    var countryCode: String? = null

    @JsonProperty("factoryActive")
    var factoryActive = false
    var factorySelected = false

    fun sellToStarchFactory(): Boolean {
        return !Strings.isEmptyOrWhitespace(factoryName) && !factoryName.equals(
            "NA",
            ignoreCase = true
        )
    }
}