package com.iita.akilimo.entities

import com.iita.akilimo.utils.enums.EnumPotatoProduceType
import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

@DatabaseTable
open class PotatoMarketOutlet {

    @DatabaseField(columnName = "id", generatedId = true)
    var id: Int? = null
    var produceTypeRadioIndex = 0
    var potatoUnitOfSaleRadioIndex = 0
    var potatoUnitPriceRadioIndex = 0

    var unitPrice = 0.0
    var unitWeight: Int = 0

    var produceType: String = EnumPotatoProduceType.TUBERS.produce()
    var unitOfSale: String? = null
}