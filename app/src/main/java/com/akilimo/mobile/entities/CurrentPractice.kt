package com.akilimo.mobile.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.akilimo.mobile.enums.EnumWeedControlMethod

@Entity(
    tableName = "current_practices",
    foreignKeys = [
        ForeignKey(
            entity = AkilimoUser::class,              // assuming you have an AkilimoUser entity
            parentColumns = ["id"],                   // parent PK column
            childColumns = ["user_id"],               // FK column in this table
            onDelete = ForeignKey.CASCADE             // delete practices if user is deleted
        )
    ]
)
data class CurrentPractice(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int? = null,

    @ColumnInfo(name = "user_id", index = true)
    val userId: Int,

    @ColumnInfo(name = "weed_radio_index")
    val weedRadioIndex: Int = 0,

    @ColumnInfo(name = "weed_control_method")
    val weedControlMethod: EnumWeedControlMethod? = null,

    @ColumnInfo(name = "plough_operations")
    val ploughOperations: String? = null,

    @ColumnInfo(name = "ridge_operations")
    val ridgeOperations: String? = null,

    @ColumnInfo(name = "harrow_operations")
    val harrowOperations: String? = null,

    @ColumnInfo(name = "weed_control_operations")
    val weedControlOperations: String? = null,

    @ColumnInfo(name = "ploughing_method")
    val ploughingMethod: String? = null,

    @ColumnInfo(name = "ridging_method")
    val ridgingMethod: String? = null,

    @ColumnInfo(name = "harrowing_method")
    val harrowingMethod: String? = null,

    @ColumnInfo(name = "tractor_available")
    val tractorAvailable: Boolean = false,

    @ColumnInfo(name = "tractor_plough")
    val tractorPlough: Boolean = false,

    @ColumnInfo(name = "tractor_harrow")
    val tractorHarrow: Boolean = false,

    @ColumnInfo(name = "tractor_ridger")
    val tractorRidger: Boolean = false,

    @ColumnInfo(name = "uses_herbicide")
    val usesHerbicide: Boolean = false,

    @ColumnInfo(name = "perform_ploughing")
    val performPloughing: Boolean = false,

    @ColumnInfo(name = "perform_harrowing")
    val performHarrowing: Boolean = false,

    @ColumnInfo(name = "perform_ridging")
    val performRidging: Boolean = false
)
