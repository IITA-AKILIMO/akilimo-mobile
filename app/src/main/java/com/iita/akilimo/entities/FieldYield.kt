package com.iita.akilimo.entities

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "field_yield")
open class FieldYield : Parcelable {

    @PrimaryKey(autoGenerate = true)
    var id: Int? = null
    var yieldAmount = 0.0
    var fieldYieldLabel: String? = null

    @Transient
    var fieldYieldDesc: String? = null

    @Transient
    var fieldYieldAmountLabel: String? = null

    @Transient
    var imageId = 0
}
