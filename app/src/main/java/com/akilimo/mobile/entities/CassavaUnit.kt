package com.akilimo.mobile.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.akilimo.mobile.base.BaseEntity

@Entity(tableName = "cassava_units")
data class CassavaUnit(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id")
    var id: Int,
    @ColumnInfo(name = "unit_weight")
    var unitWeight: Double = 0.0,
    @ColumnInfo(name = "sort_order")
    var sortOrder: Int = 0,
    @ColumnInfo(name = "label")
    var label: String,
    @ColumnInfo(name = "description")
    var description: String?,
    @ColumnInfo(name = "is_active")
    var isActive: Boolean = true,
) : BaseEntity()
