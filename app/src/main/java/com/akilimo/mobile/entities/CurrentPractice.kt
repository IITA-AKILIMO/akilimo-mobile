package com.akilimo.mobile.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.akilimo.mobile.utils.enums.EnumOperationMethod
import com.akilimo.mobile.utils.enums.EnumWeedControlMethod

@Entity(tableName = "current_practices")
data class CurrentPractice(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Long? = null,

    @ColumnInfo(name = "weed_radio_index")
    var weedRadioIndex: Int = -1,

    @ColumnInfo(name = "weed_control_method")
    var weedControlMethod: EnumWeedControlMethod = EnumWeedControlMethod.NONE,

    @ColumnInfo(name = "plough_operations")
    var ploughOperations: String? = null,

    @ColumnInfo(name = "ridge_operations")
    var ridgeOperations: String? = null,

    @ColumnInfo(name = "harrow_operations")
    var harrowOperations: String? = null,

    @ColumnInfo(name = "weed_control_operations")
    var weedControlOperations: String? = null,

    @ColumnInfo(name = "ploughing_method")
    var ploughingMethod: EnumOperationMethod = EnumOperationMethod.NONE,

    @ColumnInfo(name = "ridging_method")
    var ridgingMethod: EnumOperationMethod = EnumOperationMethod.NONE,

    @ColumnInfo(name = "harrowing_method")
    var harrowingMethod: EnumOperationMethod = EnumOperationMethod.NONE,

    @ColumnInfo(name = "tractor_available")
    var tractorAvailable: Boolean = false,

    @ColumnInfo(name = "tractor_plough")
    var tractorPlough: Boolean = false,

    @ColumnInfo(name = "tractor_harrow")
    var tractorHarrow: Boolean = false,

    @ColumnInfo(name = "tractor_ridger")
    var tractorRidger: Boolean = false,

    @ColumnInfo(name = "uses_herbicide")
    var usesHerbicide: Boolean = false,

    @ColumnInfo(name = "perform_ploughing")
    var performPloughing: Boolean = false,

    @ColumnInfo(name = "perform_harrowing")
    var performHarrowing: Boolean = false,

    @ColumnInfo(name = "perform_ridging")
    var performRidging: Boolean = false
)