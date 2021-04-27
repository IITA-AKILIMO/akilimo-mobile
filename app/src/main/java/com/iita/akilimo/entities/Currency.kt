package com.iita.akilimo.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.fasterxml.jackson.annotation.JsonProperty


@Entity(
    tableName = "currency",
    indices = [Index(value = ["currencyCode"], unique = true)]
)
open class Currency {

    @PrimaryKey(autoGenerate = true)
    var id: Int? = null

    @JsonProperty("country")
    var country: String? = null

    @JsonProperty("currencyName")
    var currencyName: String? = null

    @JsonProperty("currencyCode")
    var currencyCode: String? = null

    @JsonProperty("currencySymbol")
    var currencySymbol: String? = null

    @JsonProperty("currencyNativeSymbol")
    var currencyNativeSymbol: String? = null

    @JsonProperty("namePlural")
    var namePlural: String? = null
}
