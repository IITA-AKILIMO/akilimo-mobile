package com.akilimo.mobile.rest.request

import com.fasterxml.jackson.annotation.JsonProperty

//@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
class FertilizerRequest {

    @JsonProperty("name")
    var name: String = "NA"

    @JsonProperty("key")
    var key: String = "NA"

    @JsonProperty("fertilizer_type")
    var fertilizerType: String = "NA"

    @JsonProperty("weight")
    var weight: Double = 0.0

    @JsonProperty("price")
    var price: Double = 0.0

    @JsonProperty("selected")
    var selected: Boolean = false
}
