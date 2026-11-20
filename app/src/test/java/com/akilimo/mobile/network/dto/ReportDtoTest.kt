package com.akilimo.mobile.network.dto

import com.squareup.moshi.Moshi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReportDtoTest {

    private val base = "https://api.akilimo.org/"

    private fun report(vararg files: Pair<String, String>) = ReportDto(
        id = "r1",
        files = files.map { ReportFileDto(it.first, it.second) },
    )

    @Test
    fun `relative signed links get the base url in front`() {
        val links = report("fertilizer_advice.pdf" to "/v1/recommendations/reports/r1/fertilizer_advice.pdf?expires=1&signature=abc")
            .downloadLinks(base)

        assertEquals(
            "https://api.akilimo.org/v1/recommendations/reports/r1/fertilizer_advice.pdf?expires=1&signature=abc",
            links[ReportKind.FERTILIZER],
        )
    }

    @Test
    fun `absolute links are kept and unknown files ignored`() {
        val links = report(
            "SP_advice.pdf" to "https://cdn.example/x.pdf",
            "other.pdf" to "/v1/x",
        ).downloadLinks(base)

        assertEquals(mapOf(ReportKind.SCHEDULED to "https://cdn.example/x.pdf"), links)
    }

    @Test
    fun `all four kinds map by file name and a missing report gives none`() {
        val links = report(
            "fertilizer_advice.pdf" to "a",
            "intercrop_advice.pdf" to "b",
            "PP_advice.pdf" to "c",
            "SP_advice.pdf" to "d",
        ).downloadLinks(base)

        assertEquals(ReportKind.entries.toSet(), links.keys)
        assertTrue((null as ReportDto?).downloadLinks(base).isEmpty())
    }

    @Test
    fun `compute answer parses the report and tolerates it being absent`() {
        val moshi = Moshi.Builder().build()
        val adapter = moshi.adapter(RecommendationResponse::class.java)

        val withReport = adapter.fromJson(
            """{"request_id":"r1","status":"success","version":"1","data":{"rec_type":"FR","recommendation":"x"},
               "report":{"id":"r1","expires_at":"2026-11-27T00:00:00+00:00","url":"/u",
               "files":[{"name":"fertilizer_advice.pdf","url":"/u"}]}}"""
        )
        assertNotNull(withReport?.report)
        assertEquals(1, withReport?.report?.files?.size)

        val without = adapter.fromJson(
            """{"request_id":"r1","status":"success","version":"1","data":{"rec_type":"FR","recommendation":"x"},"report":null}"""
        )
        assertNull(without?.report)
    }
}
