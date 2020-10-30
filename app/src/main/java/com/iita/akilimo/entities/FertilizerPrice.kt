package com.iita.akilimo.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty


@JsonIgnoreProperties(ignoreUnknown = true)
@Entity(
    tableName = "fertilizer_price",
    indices = [Index(value = ["fertilizerCountry"], unique = true)]
)
open class FertilizerPrice {

    @PrimaryKey(autoGenerate = true)
    var id: Int? = null

    @JsonProperty("recordId")
    var recordId = 0

    @JsonProperty("priceId")
    var priceId = 0

    @JsonProperty("minLocalPrice")
    var minLocalPrice = 0.0

    @JsonProperty("minUsd")
    var minUsd = 0.0

    @JsonProperty("maxLocalPrice")
    var maxLocalPrice = 0.0

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

    @JsonProperty("fertilizerCountry")
    var fertilizerCountry: String? = null

    @JsonProperty("description")
    var description: String? = null
}