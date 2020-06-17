package com.iita.akilimo.entities

import com.iita.akilimo.utils.enums.EnumMaizeProduceType
import com.iita.akilimo.utils.enums.EnumUnitOfSale
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class MaizeMarketOutlet : RealmObject() {

    @PrimaryKey
    var id: String? = null
    var produceRadioIndex = 0
    var grainUnitRadioIndex = 0
    var grainUnitPriceRadioIndex = 0
    var exactPrice = 0.0
    var averagePrice = 0.0
    var unitPrice: Double = 0.0
    var unitWeight: Int = 0


    var produceType: String = EnumMaizeProduceType.GRAIN.name
    var unitOfSale: String = EnumUnitOfSale.ONE_KG.unitOfSale()
}