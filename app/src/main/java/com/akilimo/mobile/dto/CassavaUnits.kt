package com.akilimo.mobile.dto

import com.akilimo.mobile.entities.CassavaUnit
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class CassavaUnitResponse(
    @param:Json(name = "data") val data: List<CassavaUnitDto>,
    @param:Json(name = "links") val links: PaginationLinks?,
    @param:Json(name = "meta") val meta: PaginationMeta?
)

@JsonClass(generateAdapter = true)
data class CassavaUnitDto(
    @param:Json(name = "id")
    val id: Int,

    @param:Json(name = "unit_weight")
    val unitWeight: Double,

    @param:Json(name = "sort_order")
    val sortOrder: Int,

    @param:Json(name = "label")
    val label: String,

    @param:Json(name = "description")
    val description: String? = null,

    @param:Json(name = "is_active")
    val isActive: Boolean = true,

    @param:Json(name = "created_at")
    val createdAt: String? = null,

    @param:Json(name = "updated_at")
    val updatedAt: String? = null
) {
    fun toEntity(): CassavaUnit {
        return CassavaUnit(
            id = id,
            unitWeight = unitWeight,
            sortOrder = sortOrder,
            label = label,
            description = description,
            isActive = isActive,
        ).apply {
            createdAt = this.createdAt
            updatedAt = this.updatedAt
        }
    }
}