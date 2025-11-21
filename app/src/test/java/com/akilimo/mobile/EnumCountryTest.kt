package com.akilimo.mobile

import com.akilimo.mobile.enums.EnumCountry
import org.junit.Assert.assertEquals
import org.junit.Test

class EnumCountryTest {

    @Test
    fun `NG maps to NIGERIA`() {
        assertEquals(EnumCountry.NG, EnumCountry.fromCode("NG"))
    }

    @Test
    fun `TZ maps to TANZANIA`() {
        assertEquals(EnumCountry.NG, EnumCountry.fromCode("TZ"))
    }


    @Test
    fun `unknown code maps to Unsupported`() {
        assertEquals(EnumCountry.Unsupported, EnumCountry.fromCode("XX"))
    }

    @Test
    fun `null or blank maps to UNKNOWN`() {
        assertEquals(EnumCountry.Unsupported, EnumCountry.fromCode(null))
        assertEquals(EnumCountry.Unsupported, EnumCountry.fromCode(""))
        assertEquals(EnumCountry.Unsupported, EnumCountry.fromCode("   "))
    }
}
