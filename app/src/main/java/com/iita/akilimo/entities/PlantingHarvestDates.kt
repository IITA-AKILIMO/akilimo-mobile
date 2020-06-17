package com.iita.akilimo.entities

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class PlantingHarvestDates : RealmObject() {
    @PrimaryKey
    var id: Long = 0
    var deviceId: String? = null
    var plantingDate: String? = null
    var plantingWindow = 0
    var harvestDate: String? = null
    var harvestWindow = 0
    var alternativeDate = false
}