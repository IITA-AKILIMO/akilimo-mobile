package com.iita.akilimo.entities

import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

@DatabaseTable
open class LocationInfo {

    @DatabaseField(columnName = "id", generatedId = true)
    var id: Int? = null
    var latitude: Double = 0.0
    var longitude: Double = 0.0
    var altitude: Double = 0.0

    var placeName: String? = null
    var address: String? = null
}