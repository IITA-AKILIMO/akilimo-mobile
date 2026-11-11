package com.akilimo.mobile.network.dto

import com.squareup.moshi.Moshi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

/** The marker rows ("I don't know", "Exact price") must parse into entities. */
class PriceDtoParsingTest {

    private val moshi = Moshi.Builder().build()

    @Test
    fun `fertilizer marker row parses`() {
        val json = """{"id":1000010,"price_id":1000010,"fertilizer_key":"UREA_NG","fertilizer_country":"NG",
            "country_code":"NG","sort_order":0,"price_per_bag":0,
            "price_range":"I don't know","active":true,"description":null}"""
        val dto = moshi.adapter(FertilizerPriceDto::class.java).fromJson(json)
        assertNotNull(dto)
        assertEquals("UREA_NG", dto!!.toEntity().fertilizerKey)
    }

    @Test
    fun `cassava exact price row parses`() {
        val json = """{"id":11,"country_code":"NG","average_price":-1,
            "exact_price":true,"item_tag":"cassUP","active":true,"sort_order":0}"""
        val dto = moshi.adapter(CassavaPriceDto::class.java).fromJson(json)
        assertNotNull(dto)
        assertEquals(-1.0, dto!!.toEntity().averagePrice, 0.0)
    }
}
