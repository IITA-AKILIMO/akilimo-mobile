package com.iita.akilimo.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "maize_performance")
open class MaizePerformance {

    @PrimaryKey(autoGenerate = true)
    var id: Int? = null
    var performanceRadioIndex = 0
    var maizePerformance: String? = null
    var performanceValue: String? = null
}