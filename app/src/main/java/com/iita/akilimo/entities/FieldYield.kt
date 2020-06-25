package com.iita.akilimo.entities

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "field_yield")
open class FieldYield {

    @PrimaryKey(autoGenerate = true)
    var id: Int? = null
    var yieldAmount = 0.0

    var fieldYieldLabel: String? = null

    @Transient
    var imageId = 0


}