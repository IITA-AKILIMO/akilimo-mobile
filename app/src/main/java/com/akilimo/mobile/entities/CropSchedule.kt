package com.akilimo.mobile.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "crop_schedules")
data class CropSchedule(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Int? = null,

    @ColumnInfo(name = "device_id")
    var deviceId: String? = null,

    @ColumnInfo(name = "planting_date")
    var plantingDate: String = "",

    @ColumnInfo(name = "planting_window")
    var plantingWindow: Int = 0,

    @ColumnInfo(name = "harvest_date")
    var harvestDate: String = "",

    @ColumnInfo(name = "harvest_window")
    var harvestWindow: Int = 0,

    @ColumnInfo(name = "alternative_date")
    var alternativeDate: Boolean = false,

    @ColumnInfo(name = "already_planted")
    var alreadyPlanted: Boolean = false
)
