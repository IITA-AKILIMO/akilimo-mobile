package com.akilimo.mobile.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.akilimo.mobile.base.BaseEntity

@Entity(
    tableName = "field_operation_costs",
    foreignKeys = [
        ForeignKey(
            entity = AkilimoUser::class,              // parent entity
            parentColumns = ["id"],                   // PK in parent
            childColumns = ["user_id"],               // FK in this entity
            onDelete = ForeignKey.CASCADE             // delete costs when user is deleted
        )
    ],
    indices = [
        Index(value = ["user_id"])     // enforce uniqueness on user_id
    ]
)
data class FieldOperationCost(
//    @PrimaryKey(autoGenerate = true)
//    @ColumnInfo(name = "id")
//    val id: Int? = null,

    // Foreign key reference to user
    @ColumnInfo(name = "user_id")
    @PrimaryKey(autoGenerate = false)
    val userId: Int,

    @ColumnInfo(name = "cost_lmo_area_basis")
    val costLmoAreaBasis: String = "areaUnit",

    // --- Manual operations ---
    @ColumnInfo(name = "manual_plough_cost")
    val manualPloughCost: Double = 0.0,

    @ColumnInfo(name = "manual_ridge_cost")
    val manualRidgeCost: Double = 0.0,

    @ColumnInfo(name = "manual_harrow_cost")
    val manualHarrowCost: Double = 0.0,

    @ColumnInfo(name = "exact_manual_plough_price")
    val exactManualPloughPrice: Boolean = false,

    @ColumnInfo(name = "exact_manual_ridge_price")
    val exactManualRidgePrice: Boolean = false,

    @ColumnInfo(name = "exact_manual_harrow_price")
    val exactManualHarrowPrice: Boolean = false,

    // --- Tractor operations ---
    @ColumnInfo(name = "tractor_available")
    val tractorAvailable: Boolean = false,

    @ColumnInfo(name = "tractor_plough_cost")
    val tractorPloughCost: Double = 0.0,

    @ColumnInfo(name = "tractor_ridge_cost")
    val tractorRidgeCost: Double = 0.0,

    @ColumnInfo(name = "tractor_harrow_cost")
    val tractorHarrowCost: Double = 0.0,

    @ColumnInfo(name = "exact_tractor_plough_price")
    val exactTractorPloughPrice: Boolean = false,

    @ColumnInfo(name = "exact_tractor_ridge_price")
    val exactTractorRidgePrice: Boolean = false,

    @ColumnInfo(name = "exact_tractor_harrow_price")
    val exactTractorHarrowPrice: Boolean = false,

    // --- Weeding operations ---
    @ColumnInfo(name = "first_weeding_operation_cost")
    val firstWeedingOperationCost: Double = 0.0,

    @ColumnInfo(name = "second_weeding_operation_cost")
    val secondWeedingOperationCost: Double = 0.0,

    @ColumnInfo(name = "exact_first_weeding_price")
    val exactFirstWeedingPrice: Boolean = false,

    @ColumnInfo(name = "exact_second_weeding_price")
    val exactSecondWeedingPrice: Boolean = false
) : BaseEntity()
