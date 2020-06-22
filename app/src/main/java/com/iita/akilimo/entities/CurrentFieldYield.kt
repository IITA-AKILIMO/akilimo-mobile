package com.iita.akilimo.entities

import androidx.room.PrimaryKey
import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable


@DatabaseTable(tableName = "current_field_yield")
open class CurrentFieldYield {

    @DatabaseField(columnName = "id", generatedId = true)
    var id: Int? = null
    var yieldAmount = 0.0
    var fieldYieldLabel: String? = null

    @Transient
    var imageId = 0


}