package com.akilimo.mobile.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.akilimo.mobile.enums.EnumUnitOfSale

/** A unit maize grain is sold in, as served by the price service (`v1/maize-units`). */
@Entity(tableName = "maize_units")
data class MaizeUnit(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id") val id: Int,
    /** The unit's name: ONE_KG, FIFTY_KG, HUNDRED_KG or THOUSAND_KG, the same as [EnumUnitOfSale]. */
    @ColumnInfo(name = "label") val label: String,
    @ColumnInfo(name = "unit_weight") val unitWeight: Double,
    @ColumnInfo(name = "sort_order") val sortOrder: Int,
    @ColumnInfo(name = "is_default") val isDefault: Boolean,
    @ColumnInfo(name = "is_active") val isActive: Boolean,
) : BaseEntity() {

    /** The app's unit of sale for this label, or null when the app does not know it. */
    fun toUnitOfSale(): EnumUnitOfSale? =
        EnumUnitOfSale.entries.firstOrNull { it.isUniversal && it.name.equals(label, ignoreCase = true) }
}