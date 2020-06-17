package com.iita.akilimo.entities

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey


open class CurrentFieldYield : RealmObject() {

    var id: Long = 0
    var yieldAmount = 0.0
    var fieldYieldLabel: String? = null

    @Transient
    var imageId = 0


}