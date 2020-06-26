package com.iita.akilimo.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mandatory_info")
open class MandatoryInfo {

    @PrimaryKey(autoGenerate = true)
    var id: Int? = null
    var placeName: String? = null
    var address: String? = null

    var areaUnitRadioIndex = 0
    var fieldSizeRadioIndex = 0

    // var areaUnitsEnum: EnumAreaUnits = EnumAreaUnits.ACRE
    // var fieldAreaEnum: EnumFieldArea = EnumFieldArea.ONE_ACRE

    var areaUnit: String? = null
    var areaSize = 0.0
    var exactArea: Boolean = false
}