package com.iita.akilimo.entities

import com.orm.SugarRecord
import io.realm.RealmObject

open class PlantingHarvestDates : RealmObject() {
    var id: Long = 0
    var deviceId: String? = null
    var plantingDate: String? = null
    var plantingWindow = 0
    var harvestDate: String? = null
    var harvestWindow = 0
    var alternativeDate = false
}