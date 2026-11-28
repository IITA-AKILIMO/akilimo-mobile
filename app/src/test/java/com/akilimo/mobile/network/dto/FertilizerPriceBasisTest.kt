package com.akilimo.mobile.network.dto

import com.squareup.moshi.Moshi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/** The price sheet says whether the prices are subsidised, from the API's `price_basis`. */
class FertilizerPriceBasisTest {

    private val moshi = Moshi.Builder().build()

    private fun json(basis: String?) = """{"id":1,"price_id":1,"fertilizer_key":"UREA_RW","fertilizer_country":"RW1",
        "country_code":"RW","sort_order":1,"price_per_bag":50000.0,"price_range":"50,000 RWF","active":true,
        "description":null,"created_at":null,"updated_at":null${basis?.let { ""","price_basis":"$it"""" } ?: ""}}"""

    @Test
    fun `the price basis is kept on the entity`() {
        val dto = moshi.adapter(FertilizerPriceDto::class.java).fromJson(json("subsidised"))
        assertNotNull(dto)
        assertEquals("subsidised", dto!!.toEntity().priceBasis)
    }

    @Test
    fun `an older server that sends no price basis leaves it empty`() {
        val dto = moshi.adapter(FertilizerPriceDto::class.java).fromJson(json(null))
        assertNotNull(dto)
        assertNull(dto!!.toEntity().priceBasis)
    }
}
