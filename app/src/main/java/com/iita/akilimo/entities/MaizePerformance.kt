package com.iita.akilimo.entities

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey


open class MaizePerformance : RealmObject() {
    @PrimaryKey
    var id: Long = 0
    var performanceRadioIndex = 0
    var maizePerformance: String? = null
    var performanceValue: String? = null
}