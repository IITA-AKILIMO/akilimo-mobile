package com.akilimo.mobile.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.akilimo.mobile.base.BaseEntity

@Entity(
    tableName = "starch_factories",
    indices = [
        Index(value = ["factory_name", "country_code"], unique = true)
    ]
)
data class StarchFactory(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id")
    var id: Int? = null,

    @ColumnInfo(name = "factory_name")
    var name: String? = null,

    @ColumnInfo(name = "factory_label")
    var label: String? = null,

    @ColumnInfo(name = "country_code")
    var countryCode: String? = null,

    @ColumnInfo(name = "is_active")
    var isActive: Boolean = false,

    @ColumnInfo(name = "sort_order")
    var sortOrder: Int = 0
) : BaseEntity()
