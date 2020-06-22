package com.iita.akilimo.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "use_cases")
open class UseCases {
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null
    var FR = false
    var CIM = false
    var CIS = false
    var BPP = false
    var SPH = false
    var SPP = false
    var useCase: String? = null
}