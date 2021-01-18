package com.iita.akilimo.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "use_case")
open class UseCases {
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null
    var name: String = ""
    var FR = false
    var CIM = false
    var CIS = false
    var BPP = false
    var SPH = false
    var SPP = false
}