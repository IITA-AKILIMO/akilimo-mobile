package com.akilimo.mobile.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_location")
data class UserLocation(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Int? = null,

    @ColumnInfo(name = "latitude")
    var latitude: Double = 0.0,

    @ColumnInfo(name = "longitude")
    var longitude: Double = 0.0,

    @ColumnInfo(name = "altitude")
    var altitude: Double = 0.0,

    @ColumnInfo(name = "location_country_code")
    var locationCountryCode: String? = null,

    @ColumnInfo(name = "location_country_name")
    var locationCountryName: String? = null,

    @ColumnInfo(name = "address")
    var address: String? = null
)
