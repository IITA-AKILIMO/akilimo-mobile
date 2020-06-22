package com.iita.akilimo.entities

import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable

@DatabaseTable
open class PlantingHarvestDates() {
    @DatabaseField(columnName = "id", generatedId = true)
    var id: Int? = null
    var deviceId: String? = null
    var plantingDate: String? = null
    var plantingWindow = 0
    var harvestDate: String? = null
    var harvestWindow = 0
    var alternativeDate = false
}