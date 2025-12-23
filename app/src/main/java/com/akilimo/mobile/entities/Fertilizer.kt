package com.akilimo.mobile.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import com.akilimo.mobile.base.BaseEntity
import com.akilimo.mobile.enums.EnumUseCase

@Entity(
    tableName = "fertilizers",
    indices = [Index(value = ["key", "country_code"], unique = true)]
)
data class Fertilizer(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id")
    val id: Int? = null,

    @ColumnInfo(name = "key")
    val key: String? = null,

    @ColumnInfo(name = "name")
    val name: String? = null,

    @ColumnInfo(name = "type")
    val type: String? = null,

    @ColumnInfo(name = "weight")
    val weight: Double = 0.0,

    @ColumnInfo(name = "sort_order")
    val sortOrder: Int = 0,

    @ColumnInfo(name = "price")
    val price: Double = 0.0,

    @ColumnInfo(name = "use_case")
    val useCase: EnumUseCase? = null,

    @ColumnInfo(name = "country_code")
    val countryCode: String? = null,

    @ColumnInfo(name = "k_content")
    val kContent: Int = 0,

    @ColumnInfo(name = "n_content")
    val nContent: Int = 0,

    @ColumnInfo(name = "p_content")
    val pContent: Int = 0,

    @ColumnInfo(name = "available")
    val available: Boolean = false,

    @ColumnInfo(name = "cim_available")
    val cimAvailable: Boolean = false,

    @ColumnInfo(name = "cis_available")
    val cisAvailable: Boolean = false
) : BaseEntity() {

    @Ignore
    var selectedPrice: Double = 0.0

    @Ignore
    var displayPrice: String? = null
}