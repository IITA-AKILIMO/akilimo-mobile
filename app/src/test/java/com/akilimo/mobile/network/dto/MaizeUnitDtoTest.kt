package com.akilimo.mobile.network.dto

import com.akilimo.mobile.enums.EnumUnitOfSale
import com.squareup.moshi.Moshi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MaizeUnitDtoTest {

    private val moshi = Moshi.Builder().build()

    @Test
    fun `maize unit parses and maps to the unit of sale by label`() {
        val json = """{"id":3,"label":"HUNDRED_KG","unit_weight":100,"sort_order":3,
            "description":"Standard bag","default":true,"active":true}"""
        val dto = moshi.adapter(MaizeUnitDto::class.java).fromJson(json)
        assertNotNull(dto)
        val unit = dto!!.toEntity()
        assertTrue(unit.isDefault)
        assertEquals(100.0, unit.unitWeight, 0.0)
        assertEquals(EnumUnitOfSale.HUNDRED_KG, unit.toUnitOfSale())
    }

    @Test
    fun `an unknown label maps to no unit of sale`() {
        val unit = MaizeUnitDto(id = 9, label = "MUDU", unitWeight = 1.0).toEntity()
        assertNull(unit.toUnitOfSale())
    }
}
