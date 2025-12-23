package com.akilimo.mobile.dto

import com.akilimo.mobile.entities.StarchFactory
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class StarchFactoryResponse(
    @param:Json(name = "data") val data: List<StarchFactoryDto>,
    @param:Json(name = "links") val links: PaginationLinks?,
    @param:Json(name = "meta") val meta: PaginationMeta?
)

@JsonClass(generateAdapter = true)
data class StarchFactoryDto(
    @param:Json(name = "id")
    val id: Int,
    @param:Json(name = "factory_name") val factoryName: String,
    @param:Json(name = "factory_label") val factoryLabel: String,
    @param:Json(name = "country_code") val countryCode: String,
    @param:Json(name = "sort_order") val sortOrder: Int?,
    @param:Json(name = "factory_active") val isActive: Boolean,
    @param:Json(name = "created_at") val createdAt: String?,
    @param:Json(name = "updated_at") val updatedAt: String?
) {
    fun toEntity() = StarchFactory(
        id = id,
        name = factoryName,
        label = factoryLabel,
        countryCode = countryCode,
        sortOrder = sortOrder ?: 0,
        isActive = isActive
    )
}