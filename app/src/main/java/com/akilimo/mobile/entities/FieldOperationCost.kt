package com.akilimo.mobile.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "field_operation_costs")
data class FieldOperationCost(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Int? = null,

    @ColumnInfo(name = "cost_lmo_area_basis")
    var costLmoAreaBasis: String? = null,

    @ColumnInfo(name = "manual_plough_cost")
    var manualPloughCost: Double = 0.0,

    @ColumnInfo(name = "manual_ridge_cost")
    var manualRidgeCost: Double = 0.0,

    @ColumnInfo(name = "manual_harrow_cost")
    var manualHarrowCost: Double = 0.0,

    @ColumnInfo(name = "tractor_plough_cost")
    var tractorPloughCost: Double = 0.0,

    @ColumnInfo(name = "tractor_ridge_cost")
    var tractorRidgeCost: Double = 0.0,

    @ColumnInfo(name = "tractor_harrow_cost")
    var tractorHarrowCost: Double = 0.0,

    @ColumnInfo(name = "first_weeding_operation_cost")
    var firstWeedingOperationCost: Double = 0.0,

    @ColumnInfo(name = "second_weeding_operation_cost")
    var secondWeedingOperationCost: Double = 0.0,

    @ColumnInfo(name = "exact_manual_plough_price")
    var exactManualPloughPrice: Boolean = false,

    @ColumnInfo(name = "exact_manual_ridge_price")
    var exactManualRidgePrice: Boolean = false,

    @ColumnInfo(name = "exact_manual_harrow_price")
    var exactManualHarrowPrice: Boolean = false,

    @ColumnInfo(name = "exact_tractor_plough_price")
    var exactTractorPloughPrice: Boolean = false,

    @ColumnInfo(name = "exact_tractor_ridge_price")
    var exactTractorRidgePrice: Boolean = false,

    @ColumnInfo(name = "exact_first_weeding_price")
    var exactFirstWeedingPrice: Boolean = false,

    @ColumnInfo(name = "exact_second_weeding_price")
    var exactSecondWeedingPrice: Boolean = false
)

