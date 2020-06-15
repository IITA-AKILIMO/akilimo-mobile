package com.iita.akilimo.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.google.android.gms.common.util.Strings
import com.orm.SugarRecord

@JsonIgnoreProperties(ignoreUnknown = true)
class StarchFactory : SugarRecord<StarchFactory?>() {
    var id: Long = 0

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