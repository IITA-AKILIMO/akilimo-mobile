package com.akilimo.mobile.entities

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "maize_performance")
open class MaizePerformance : Parcelable {

    @PrimaryKey(autoGenerate = true)
    var id: Int? = null
    var performanceRadioIndex = 0
    var maizePerformance: String? = null
    var performanceValue: String? = null

    @Transient
    var maizePerformanceDesc: String? = null

    @Transient
    var maizePerformanceLabel: String? = null

    @Transient
    var imageId = 0
}
