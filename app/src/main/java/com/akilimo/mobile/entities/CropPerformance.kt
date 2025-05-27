package com.akilimo.mobile.entities

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "crop_performance")
data class CropPerformance(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Int? = null,

    @ColumnInfo(name = "performance_radio_index")
    var performanceRadioIdx: Int = 0,

    @ColumnInfo(name = "crop_type")
    var cropType: String = "maize",

    @ColumnInfo(name = "maize_performance")
    var maizePerformance: String = "",

    @ColumnInfo(name = "performance_score")
    var performanceScore: Int = 0,

    @ColumnInfo(name = "maize_performance_desc")
    var maizePerformanceDesc: String? = null,

    @ColumnInfo(name = "maize_performance_label")
    var maizePerformanceLabel: String? = null,

    @ColumnInfo(name = "image_id")
    var imageId: Int = 0

) : Parcelable
