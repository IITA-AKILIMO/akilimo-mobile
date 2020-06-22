package com.iita.akilimo.entities

import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable

@DatabaseTable
open class MandatoryInfo {

    @DatabaseField(columnName = "id", generatedId = true)
    var id: Int? = null
    var placeName: String? = null
    var address: String? = null

    var countryCode: String? = null
    var countryName: String? = null
    var currency: String? = null

    var areaUnitRadioIndex = 0
    var fieldSizeRadioIndex = 0
    var selectedCountryIndex = 0

    // var areaUnitsEnum: EnumAreaUnits = EnumAreaUnits.ACRE
    // var fieldAreaEnum: EnumFieldArea = EnumFieldArea.ONE_ACRE

    var areaUnit: String? = null
    var areaSize = 0.0
    var exactArea: Boolean = false
}