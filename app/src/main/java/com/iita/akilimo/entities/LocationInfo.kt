package com.iita.akilimo.entities

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey


open class LocationInfo : RealmObject() {

    @PrimaryKey
    var id: String? = null
    var latitude: Double = 0.0
    var longitude: Double = 0.0
    var altitude: Double = 0.0

    var placeName: String? = null
    var address: String? = null
}