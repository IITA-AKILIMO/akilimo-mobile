package com.akilimo.mobile.dto

import com.akilimo.mobile.entities.Fertilizer
import com.akilimo.mobile.enums.EnumUseCase
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FertilizerResponse(
    @param:Json(name = "data") val data: List<FertilizerDto>,
    @param:Json(name = "links") val links: PaginationLinks?,
    @param:Json(name = "meta") val meta: PaginationMeta?
)

@JsonClass(generateAdapter = true)
data class FertilizerDto(
    @param:Json(name = "id") val id: Int?,
    @param:Json(name = "name") val name: String?,
    @param:Json(name = "type") val fertilizerType: String?,
    @param:Json(name = "fertilizer_key") val fertilizerKey: String?,
    @param:Json(name = "weight") val weight: Double?,
    @param:Json(name = "country_code") val countryCode: String?,
    @param:Json(name = "currency_code") val currencyCode: String?,
    @param:Json(name = "sort_order") val sortOrder: Int?,
    @param:Json(name = "use_case") val useCase: String?,
    @param:Json(name = "available") val available: Boolean?,
    @param:Json(name = "created_at") val createdAt: String?,
    @param:Json(name = "updated_at") val updatedAt: String?
) {
    fun toEntity(): Fertilizer {
        return Fertilizer(
            id = id,
            key = fertilizerKey,
            name = name,
            type = fertilizerType,
            weight = weight ?: 0.0,
            sortOrder = sortOrder ?: 0,
            countryCode = countryCode,
            useCase = EnumUseCase.fromCode(useCase),
            available = available ?: false
        )
    }
}