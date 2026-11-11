package com.akilimo.mobile.network.dto

import com.akilimo.mobile.entities.MaizeUnit
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MaizeUnitDto(
    @param:Json(name = "id") val id: Int,
    @param:Json(name = "label") val label: String,
    @param:Json(name = "unit_weight") val unitWeight: Double,
    @param:Json(name = "sort_order") val sortOrder: Int = 0,
    @param:Json(name = "default") val isDefault: Boolean = false,
    @param:Json(name = "active") val isActive: Boolean = true,
) {
    fun toEntity() = MaizeUnit(
        id = id,
        label = label,
        unitWeight = unitWeight,
        sortOrder = sortOrder,
        isDefault = isDefault,
        isActive = isActive,
    )
}