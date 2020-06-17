package com.iita.akilimo.entities

import com.iita.akilimo.utils.enums.EnumPotatoProduceType
import com.iita.akilimo.utils.enums.EnumUnitOfSale
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class PotatoMarketOutlet : RealmObject() {
    @PrimaryKey
    var id: Long = 0
    var produceTypeRadioIndex = 0
    var potatoUnitOfSaleRadioIndex = 0
    var potatoUnitPriceRadioIndex = 0
    var unitPrice = 0.0
    var unitWeight: Int = 0

    var produceType: String = EnumPotatoProduceType.TUBERS.produce()
    var unitOfSale: String = EnumUnitOfSale.ONE_KG.unitOfSale()
}