package com.akilimo.mobile.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mandatory_info")
open class MandatoryInfo {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Int? = null

    @ColumnInfo(name = "place_name")
    var placeName: String? = null

    @ColumnInfo(name = "address")
    var address: String? = null

    @ColumnInfo(name = "area_unit")
    var areaUnit: String = "acre"

    @ColumnInfo(name = "old_area_unit")
    var oldAreaUnit: String? = null

    @ColumnInfo(name = "display_area_unit")
    var displayAreaUnit: String = ""

    @ColumnInfo(name = "area_size")
    var areaSize: Double = 0.0

    @ColumnInfo(name = "exact_area")
    var exactArea: Boolean = false
}