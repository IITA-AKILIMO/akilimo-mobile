package com.iita.akilimo.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scheduled_date")
open class ScheduledDate() {
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null
    var deviceId: String? = null
    var plantingDate: String? = null
    var plantingWindow = 0
    var harvestDate: String? = null
    var harvestWindow = 0
    var alternativeDate = false
}