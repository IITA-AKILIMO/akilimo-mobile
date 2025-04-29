package com.akilimo.mobile.entities

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.parcelize.Parcelize

@JsonIgnoreProperties(ignoreUnknown = true)
data class OperationCostResponse(
    @JsonProperty("data")
    val data: List<OperationCost>
)

@Parcelize
@Entity(tableName = "operation_costs")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class OperationCost(

    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id")
    val id: Int,

    @JsonProperty("item_tag")
    @ColumnInfo(name = "item_tag")
    val itemTag: String? = null,

    @JsonProperty("operation_name")
    @ColumnInfo(name = "operation_name")
    val operationName: String? = null,

    @JsonProperty("operation_type")
    @ColumnInfo(name = "operation_type")
    val operationType: String? = null,


    @JsonProperty("currency_code")
    @ColumnInfo(name = "currency_code")
    val currencyCode: String? = null,

    @JsonProperty("min_cost")
    @ColumnInfo(name = "min_cost")
    val minCost: Double = 0.0,

    @JsonProperty("max_cost")
    @ColumnInfo(name = "max_cost")
    val maxCost: Double = 0.0,

    @JsonProperty("average_cost")
    @ColumnInfo(name = "average_cost")
    val averageCost: Double = 0.0
) : Parcelable