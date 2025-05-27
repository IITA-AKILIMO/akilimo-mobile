package com.akilimo.mobile.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "use_cases")
open class UseCase {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id", index = true)
    var id: Int? = null

    @ColumnInfo(name = "use_case_name", index = true)
    var useCaseName: String = ""

    @ColumnInfo(name = "fr")
    var fertilizerRecommendation = false

    @ColumnInfo(name = "cim")
    var maizeInterCropping = false

    @ColumnInfo(name = "cis")
    var sweetPotatoInterCropping = false

    @ColumnInfo(name = "bpp")
    var bestPlantingPractices = false

    @ColumnInfo(name = "sph")
    var scheduledPlantingHighStarch = false

    @ColumnInfo(name = "spp")
    var scheduledPlanting = false
}
