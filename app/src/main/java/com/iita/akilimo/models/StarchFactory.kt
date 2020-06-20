package com.iita.akilimo.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.google.android.gms.common.util.Strings
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

@JsonIgnoreProperties(ignoreUnknown = true)
open class StarchFactory : RealmObject() {
    var id: Long = 0

    @JsonProperty("factoryName")
    var factoryName: String? = null

    @PrimaryKey
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