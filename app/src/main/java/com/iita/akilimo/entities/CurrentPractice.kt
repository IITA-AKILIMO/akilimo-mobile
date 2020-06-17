package com.iita.akilimo.entities

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class CurrentPractice : RealmObject() {

    @PrimaryKey
    var id: String? = null
    var weedRadioIndex = 0
    var weedControlTechnique: String? = null
    var ploughOperations: String? = null
    var ridgeOperations: String? = null
    var harrowOperations: String? = null
    var weedControlOperations: String? = null
    var ploughingMethod: String? = null
    var ridgingMethod: String? = null
    var harrowingMethod: String? = null
    var tractorAvailable = false
    var tractorPlough = false
    var tractorHarrow = false
    var tractorRidger = false
    var usesHerbicide = false
    var performPloughing = false
    var performHarrowing = false
    var performRidging = false
}