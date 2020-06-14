package com.iita.akilimo.entities

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Entity
class MaizePerformance {
    @Id
    var id: Long = 0
    var performanceRadioIndex = 0
    var maizePerformance: String? = null
    var performanceValue: String? = null
}