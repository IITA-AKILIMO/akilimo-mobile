package com.iita.akilimo.entities

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class MandatoryInfo : RealmObject() {

    @PrimaryKey
    var id: String? = null
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