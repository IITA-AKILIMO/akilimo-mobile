package com.akilimo.mobile.utils

import com.akilimo.mobile.enums.EnumAreaUnit
import org.junit.Assert.assertEquals
import org.junit.Test

class MathHelperTest {

    // ---- convertFromAcres ----

    @Test
    fun `convertFromAcres ACRE returns value unchanged`() {
        assertEquals(2.5, MathHelper.convertFromAcres(2.5, EnumAreaUnit.ACRE), 0.0001)
    }

    @Test
    fun `convertFromAcres HA applies correct factor`() {
        // 1 acre = 0.404686 ha
        assertEquals(0.404686, MathHelper.convertFromAcres(1.0, EnumAreaUnit.HA), 0.0001)
    }

    @Test
    fun `convertFromAcres M2 applies correct factor`() {
        // 1 acre = 4046.86 m²
        assertEquals(4046.86, MathHelper.convertFromAcres(1.0, EnumAreaUnit.M2), 0.01)
    }

    @Test
    fun `convertFromAcres ARE applies correct factor`() {
        // 1 acre = 40.4686 ares
        assertEquals(40.4686, MathHelper.convertFromAcres(1.0, EnumAreaUnit.ARE), 0.0001)
    }

    @Test
    fun `convertFromAcres scales proportionally`() {
        val single = MathHelper.convertFromAcres(1.0, EnumAreaUnit.HA)
        val double = MathHelper.convertFromAcres(2.0, EnumAreaUnit.HA)
        assertEquals(single * 2, double, 0.0001)
    }

    @Test
    fun `convertFromAcres zero returns zero`() {
        assertEquals(0.0, MathHelper.convertFromAcres(0.0, EnumAreaUnit.HA), 0.0)
    }

    // ---- format ----

    @Test
    fun `format whole number omits decimal places`() {
        assertEquals("1,000", MathHelper.format(1000.0))
    }

    @Test
    fun `format fractional number shows two decimal places`() {
        assertEquals("1,000.50", MathHelper.format(1000.5))
    }

    @Test
    fun `format zero returns zero without decimals`() {
        assertEquals("0", MathHelper.format(0.0))
    }
}
