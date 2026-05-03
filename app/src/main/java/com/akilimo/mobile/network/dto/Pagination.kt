package com.akilimo.mobile.network.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class PagedResponse<T>(
    @param:Json(name = "data") val data: List<T>,
    @param:Json(name = "links") val links: PaginationLinks?,
    @param:Json(name = "meta") val meta: PaginationMeta?
)

@JsonClass(generateAdapter = true)
data class PaginationLinks(
    @param:Json(name = "first") val first: String?,
    @param:Json(name = "last") val last: String?,
    @param:Json(name = "prev") val prev: String?,
    @param:Json(name = "next") val next: String?
)

@JsonClass(generateAdapter = true)
data class PaginationMeta(
    @param:Json(name = "current_page") val currentPage: Int?,
    @param:Json(name = "last_page") val lastPage: Int?,
    @param:Json(name = "per_page") val perPage: Int?,
    @param:Json(name = "total") val total: Int?
)
