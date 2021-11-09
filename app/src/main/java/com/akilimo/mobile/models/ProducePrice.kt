package com.akilimo.mobile.models


import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
open class ProducePrice {
    @JsonProperty("priceIndex")
    var priceIndex: Long = 0

    @JsonProperty("priceId")
    var priceId: Long = 0

    @JsonProperty("country")
    var country: String? = null


    @JsonProperty("countryPrice")
    var countryPrice: String? = null

    @JsonProperty("minLocalPrice")
    var minLocalPrice = 0.0

    @JsonProperty("maxLocalPrice")
    var maxLocalPrice = 0.0

    @JsonProperty("minUsd")
    var minUsd = 0.0

    @JsonProperty("maxUsd")
    var maxUsd = 0.0

    @JsonProperty("active")
    var active = false

    @JsonProperty("averagePrice")
    var averagePrice = 0.0

    @JsonProperty("createdAt")
    var createdAt: Date? = null

    @JsonProperty("updatedAt")
    var updatedAt: Date? = null
}
