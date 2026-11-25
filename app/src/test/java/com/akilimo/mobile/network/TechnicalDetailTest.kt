package com.akilimo.mobile.network

import com.akilimo.mobile.network.dto.ApiErrorResponse
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response
import java.net.UnknownHostException

class TechnicalDetailTest {

    // A reserved .test host: no real API address in test code
    private val testBase = "https://api.example.test"

    private fun failed(code: Int, url: String = "$testBase/v1/recommendations/compute?token=secret"): Response<Any> {
        val request = Request.Builder().url(url).post("{}".toRequestBody("application/json".toMediaType())).build()
        val raw = okhttp3.Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(code)
            .message("error")
            .build()
        return Response.error("{}".toResponseBody("application/json".toMediaType()), raw)
    }

    @Test
    fun `exception detail names the type and message`() {
        val detail = UnknownHostException("Unable to resolve host \"api.example.test\"").technicalDetail()
        assertEquals("UnknownHostException: Unable to resolve host \"api.example.test\"", detail)
    }

    @Test
    fun `exception detail adds a different cause and survives a missing message`() {
        val detail = RuntimeException(null, IllegalStateException("boom")).technicalDetail()
        assertEquals("RuntimeException: no message (cause: IllegalStateException: boom)", detail)
    }

    @Test
    fun `exception detail is capped to one short line`() {
        val detail = IllegalStateException("x\n".repeat(500)).technicalDetail()
        assertTrue(detail.length <= 240)
        assertFalse(detail.contains('\n'))
    }

    @Test
    fun `http detail has status method and path but never the query string`() {
        val detail = failed(503).technicalDetail()
        assertEquals("HTTP 503 POST /v1/recommendations/compute", detail)
        assertFalse(detail.contains("secret"))
    }

    @Test
    fun `http detail adds the api code message and invalid field names only`() {
        val error = ApiErrorResponse(
            message = "The given data was invalid.",
            errors = mapOf("computeRequest.farmInformation.map_lat" to listOf("The value 99 is outside")),
            code = "validation_failed",
        )
        val detail = failed(422).technicalDetail(error)
        assertTrue(detail.startsWith("HTTP 422 POST /v1/recommendations/compute"))
        assertTrue(detail.contains("code=validation_failed"))
        assertTrue(detail.contains("fields=computeRequest.farmInformation.map_lat"))
        assertFalse(detail.contains("outside"))
    }

    @Test
    fun `a machine code replaces the free text so coordinates are never shown`() {
        val error = ApiErrorResponse(
            error = "Bad Request",
            message = "No recommendation data available for lat=-9.287, lon=39.107 in TZ",
            status = 400,
            code = "outside_coverage",
        )
        val detail = failed(400).technicalDetail(error)
        assertEquals("HTTP 400 POST /v1/recommendations/compute · code=outside_coverage", detail)
    }

    @Test
    fun `free text without a code has its decimal numbers hidden`() {
        val error = ApiErrorResponse(message = "No data for lat=-9.287, lon=39.107")
        val detail = failed(400).technicalDetail(error)
        assertTrue(detail.contains("message=No data for lat=…, lon=…"))
        assertFalse(detail.contains("9.287"))
    }

    @Test
    fun `the api outside coverage answer is recognised from its code`() {
        val json = """{"error":"Bad Request","message":"No recommendation data available","status":400,"code":"outside_coverage"}"""
        val parsed = com.squareup.moshi.Moshi.Builder().build().adapter(ApiErrorResponse::class.java).fromJson(json)
        assertTrue(parsed?.isOutsideCoverage == true)
        assertFalse(ApiErrorResponse(code = "validation_failed").isOutsideCoverage)
    }
}
