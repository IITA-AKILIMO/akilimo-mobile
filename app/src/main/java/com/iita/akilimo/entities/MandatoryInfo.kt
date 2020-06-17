package com.iita.akilimo.entities

import com.iita.akilimo.utils.enums.EnumAreaUnits
import com.iita.akilimo.utils.enums.EnumCountry
import com.iita.akilimo.utils.enums.EnumFieldArea
import com.orm.SugarRecord
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

class MandatoryInfo : RealmObject() {
    @PrimaryKey
    var id: Long = 0
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
    var exactArea:Boolean = false
}