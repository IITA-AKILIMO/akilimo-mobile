package com.akilimo.mobile.network.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/** One PDF of a recommendation report: its file name and a signed, expiring (relative) download link. */
@JsonClass(generateAdapter = true)
data class ReportFileDto(
    @param:Json(name = "name") val name: String,
    @param:Json(name = "url") val url: String,
)

/** The PDF report of a recommendation: signed download links that expire at [expiresAt]. */
@JsonClass(generateAdapter = true)
data class ReportDto(
    @param:Json(name = "id") val id: String? = null,
    @param:Json(name = "expires_at") val expiresAt: String? = null,
    @param:Json(name = "url") val url: String? = null,
    @param:Json(name = "files") val files: List<ReportFileDto> = emptyList(),
)

/** The answer of the saved-history report endpoint: fresh links for one saved advice. */
@JsonClass(generateAdapter = true)
data class ReportLinksResponse(@param:Json(name = "data") val data: ReportDto)

/** The four kinds of PDF the API can make, by the file name it gives them. */
enum class ReportKind(val fileName: String) {
    FERTILIZER("fertilizer_advice.pdf"),
    INTERCROP("intercrop_advice.pdf"),
    PLANTING("PP_advice.pdf"),
    SCHEDULED("SP_advice.pdf"),
}

/**
 * The downloadable PDFs of this report, by kind, with absolute URLs. The API sends links relative to its own
 * address (they carry the signature in the query), so [baseUrl] is put in front. Unknown file names are ignored.
 */
fun ReportDto?.downloadLinks(baseUrl: String): Map<ReportKind, String> {
    if (this == null) return emptyMap()
    val root = baseUrl.trimEnd('/')
    return files.mapNotNull { file ->
        val kind = ReportKind.entries.firstOrNull { it.fileName.equals(file.name, ignoreCase = true) }
            ?: return@mapNotNull null
        val absolute = if (file.url.startsWith("http", ignoreCase = true)) file.url else root + "/" + file.url.trimStart('/')
        kind to absolute
    }.toMap()
}
