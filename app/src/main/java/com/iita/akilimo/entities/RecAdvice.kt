package com.iita.akilimo.entities

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class RecAdvice : RealmObject() {

    @PrimaryKey
    var id: String? = null
    var FR = false
    var CIM = false
    var CIS = false
    var BPP = false
    var SPH = false
    var SPP = false
    var useCase: String? = null
}