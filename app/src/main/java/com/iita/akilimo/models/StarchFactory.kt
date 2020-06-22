package com.iita.akilimo.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.google.android.gms.common.util.Strings
import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable

@JsonIgnoreProperties(ignoreUnknown = true)
@DatabaseTable
open class StarchFactory {
    @JsonProperty("id")
    @DatabaseField(columnName = "id", generatedId = true)
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