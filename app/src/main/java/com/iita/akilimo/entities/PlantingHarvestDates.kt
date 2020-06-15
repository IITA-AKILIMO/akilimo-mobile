package com.iita.akilimo.entities

import com.orm.SugarRecord

class PlantingHarvestDates : SugarRecord<PlantingHarvestDates?>() {
    var id: Long = 0
    var deviceId: String? = null
    var plantingDate: String? = null
    var plantingWindow = 0
    var harvestDate: String? = null
    var harvestWindow = 0
    var alternativeDate = false
}