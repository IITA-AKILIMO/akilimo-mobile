package com.akilimo.mobile.models

import android.os.Parcelable
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.android.parcel.Parcelize

@Parcelize
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
open class OperationCost : Parcelable {

    var id: Int = 0

    @JsonProperty("listIndex")
    var listIndex: Long = 0

    @JsonProperty("operationName")
    var operationName: String? = null

    @JsonProperty("operationType")
    var operationType: String? = null

    @JsonProperty("minUsd")
    var minUsd = 0.0

    @JsonProperty("maxUsd")
    var maxUsd = 0.0

    @JsonProperty("minTzs")
    var minTzs = 0.0

    @JsonProperty("maxTzs")
    var maxTzs = 0.0

    @JsonProperty("minNgn")
    var minNgn = 0.0

    @JsonProperty("maxNgn")
    var maxNgn = 0.0

    @JsonProperty("averageNgnPrice")
    var averageNgnPrice = 0.0

    @JsonProperty("averageTzsPrice")
    var averageTzsPrice = 0.0

    @JsonProperty("averageUsdPrice")
    var averageUsdPrice = 0.0
}
