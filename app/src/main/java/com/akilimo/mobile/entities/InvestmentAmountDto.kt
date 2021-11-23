package com.akilimo.mobile.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.fasterxml.jackson.annotation.JsonProperty


@Entity(
    tableName = "investment_amount_dto",
    indices = [Index(value = ["investmentId"], unique = true)]
)
open class InvestmentAmountDto {

    @PrimaryKey(autoGenerate = true)
    var id: Int? = null

    @JsonProperty("investmentId")
    var investmentId: Long = 0

    @JsonProperty("country")
    var country: String? = null

    @JsonProperty("investmentAmount")
    var investmentAmount: Double = 0.0

    @JsonProperty("minInvestmentAmount")
    var minInvestmentAmount: Double = 0.0

    @JsonProperty("maxInvestmentAmount")
    var maxInvestmentAmount: Double = 0.0


    @JsonProperty("areaUnit")
    var areaUnit: String? = null

    @JsonProperty("priceActive")
    var priceActive = false

    @JsonProperty("sortOrder")
    var sortOrder: Long = 0

    @JsonProperty("createdAt")
    var createdAt: String? = null

    @JsonProperty("updatedAt")
    var updatedAt: String? = null
}
