package com.akilimo.mobile.entities

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@JsonClass(generateAdapter = true)
data class OperationCostResponse(
    @Json(name = "data")
    val data: List<OperationCost>
)

@Parcelize
@Entity(tableName = "operation_costs")
@JsonClass(generateAdapter = true)
data class OperationCost(

    @PrimaryKey(autoGenerate = false)
    @Json(name = "id")
    @ColumnInfo(name = "id")
    val id: Long,

    @Json(name = "item_tag")
    @ColumnInfo(name = "item_tag")
    val itemTag: String? = null,

    @Json(name = "operation_name")
    @ColumnInfo(name = "operation_name")
    val operationName: String? = null,

    @Json(name = "operation_type")
    @ColumnInfo(name = "operation_type")
    val operationType: String? = null,

    @Json(name = "country_code")
    @ColumnInfo(name = "country_code")
    val countryCode: String? = null,

    @Json(name = "min_cost")
    @ColumnInfo(name = "min_cost")
    val minCost: Double = 0.0,

    @Json(name = "max_cost")
    @ColumnInfo(name = "max_cost")
    val maxCost: Double = 0.0,

    @Json(name = "average_cost")
    @ColumnInfo(name = "average_cost")
    val averageCost: Double = 0.0,

    @Json(name = "is_active")
    @ColumnInfo(name = "is_active")
    val active: Boolean = false,
) : Parcelable