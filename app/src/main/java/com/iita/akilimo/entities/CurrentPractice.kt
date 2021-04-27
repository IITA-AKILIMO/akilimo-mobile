package com.iita.akilimo.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "current_practice")
open class CurrentPractice {

    @PrimaryKey(autoGenerate = true)
    var id: Long? = null
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
