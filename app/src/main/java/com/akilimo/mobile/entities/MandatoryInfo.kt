package com.akilimo.mobile.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mandatory_info")
open class MandatoryInfo {

    @PrimaryKey(autoGenerate = true)
    var id: Int? = null
    var placeName: String? = null
    var address: String? = null

    var areaUnitRadioIndex: Int = 0
    var fieldSizeRadioIndex: Int = 0

    var areaUnit: String = "acre"
    var oldAreaUnit: String? = null
    var displayAreaUnit: String = ""
    var areaSize: Double = 0.0
    var exactArea: Boolean = false
}
