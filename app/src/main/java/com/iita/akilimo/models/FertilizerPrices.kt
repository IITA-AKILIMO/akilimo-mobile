package com.iita.akilimo.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.orm.SugarRecord
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

@JsonIgnoreProperties(ignoreUnknown = true)
open class FertilizerPrices : RealmObject() {
    @JsonProperty("id")
    var id: Long = 0

    @JsonProperty("recordId")
    var recordId = 0

    @JsonProperty("priceId")
    var priceId = 0

    @JsonProperty("minUsd")
    var minUsd = 0.0

    @JsonProperty("maxUsd")
    var maxUsd = 0.0

    @JsonProperty("pricePerBag")
    var pricePerBag = 0.0

    @JsonProperty("active")
    var active = false

    @JsonProperty("priceRange")
    var priceRange: String? = null

    @JsonProperty("country")
    var country: String? = null

    @PrimaryKey
    @JsonProperty("fertilizerCountry")
    var fertilizerCountry: String? = null

    @JsonProperty("description")
    var description: String? = null
}