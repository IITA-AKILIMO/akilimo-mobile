package com.akilimo.mobile.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "location_info")
open class LocationInfo {

    @PrimaryKey(autoGenerate = true)
    var id: Int? = null
    var latitude: Double = 0.0
    var longitude: Double = 0.0
    var altitude: Double = 0.0

    var placeName: String? = null
    var locationCountry: String? = null
    var address: String? = null
}
