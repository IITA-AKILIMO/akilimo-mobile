package com.akilimo.mobile.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "planting_scheduler")
open class PlantingInfo() {
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null
    var deviceId: String? = null
    var plantingDate: String? = null
    var plantingWindow: Int = 0
    var harvestDate: String? = null
    var harvestWindow: Int = 0
    var alternativeDate: Boolean = false
    var alreadyPlanted: Boolean = false
}
