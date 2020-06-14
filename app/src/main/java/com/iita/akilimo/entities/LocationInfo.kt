package com.iita.akilimo.entities

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Entity
class LocationInfo {
    @Id
    var id: Long = 0
    var latitude: Double = 0.0
    var longitude: Double = 0.0
    var altitude: Double = 0.0

    var placeName: String? = null
    var address: String? = null
}