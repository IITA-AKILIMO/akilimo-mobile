package com.iita.akilimo.entities

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class OperationCosts constructor() : RealmObject() {

    var id: Long = 0
    var costLmoAreaBasis: String? = null
    var manualPloughCost = 0.0
    var manualRidgeCost = 0.0
    var manualHarrowCost = 0.0
    var tractorPloughCost = 0.0
    var tractorRidgeCost = 0.0
    var tractorHarrowCost = 0.0
    var firstWeedingOperationCost = 0.0
    var secondWeedingOperationCost = 0.0
    var exactManualPloughPrice = false
    var exactManualRidgePrice = false
    var exactManualHarrowPrice = false
    var exactTractorPloughPrice = false
    var exactTractorRidgePrice = false
    var exactFirstWeedingPrice = false
    var exactSecondWeedingPrice = false
}