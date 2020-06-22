package com.iita.akilimo.entities

import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable

@DatabaseTable
open class MaizePerformance {

    @DatabaseField(columnName = "id", generatedId = true)
    var id: Int? = null
    var performanceRadioIndex = 0
    var maizePerformance: String? = null
    var performanceValue: String? = null
}