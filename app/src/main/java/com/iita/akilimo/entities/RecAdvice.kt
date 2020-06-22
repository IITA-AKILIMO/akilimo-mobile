package com.iita.akilimo.entities

import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable

@DatabaseTable
open class RecAdvice {
    @DatabaseField(columnName = "id", generatedId = true)
    var id: Int? = null
    var FR = false
    var CIM = false
    var CIS = false
    var BPP = false
    var SPH = false
    var SPP = false
    var useCase: String? = null
}