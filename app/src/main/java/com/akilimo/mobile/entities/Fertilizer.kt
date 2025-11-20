package com.akilimo.mobile.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import com.akilimo.mobile.base.BaseEntity

@Entity(
    tableName = "fertilizers",
    indices = [Index(value = ["key", "country_code"], unique = true)]
)
class Fertilizer : BaseEntity() {

    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id")
    var id: Int? = null

    @ColumnInfo(name = "key")
    var key: String? = null


    @ColumnInfo(name = "name")
    var name: String? = null

    @ColumnInfo(name = "type")
    var type: String? = null

    @ColumnInfo(name = "weight")
    var weight: Double = 0.0

    @ColumnInfo(name = "sort_order")
    var sortOrder: Int = 0

    @ColumnInfo(name = "price")
    var price: Double = 0.0

    @ColumnInfo(name = "use_case")
    var useCase: String? = null

    @ColumnInfo(name = "country_code")
    var countryCode: String? = null

    @ColumnInfo(name = "k_content")
    var kContent: Int = 0

    @ColumnInfo(name = "n_content")
    var nContent:Int = 0

    @ColumnInfo(name = "p_content")
    var pContent:Int = 0

    @ColumnInfo(name = "available")
    var available: Boolean = false

    @ColumnInfo(name = "cim_available")
    var cimAvailable: Boolean = false

    @ColumnInfo(name = "cis_available")
    var cisAvailable: Boolean = false

    @Ignore
    var selectedPrice: Double = 0.0

    @Ignore
    var displayPrice: String? = null
}