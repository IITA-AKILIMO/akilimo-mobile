package com.iita.akilimo.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "advice_status", indices = [Index(value = ["advice_name"], unique = true)])
class AdviceStatus(
    @ColumnInfo(name = "advice_name")
    val adviceName: String,
    val completed: Boolean,
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null
}
