package com.akilimo.mobile.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AmountInputTest {

    private fun assertParses(expected: Double, raw: String) =
        assertEquals("parse(\"$raw\")", expected, AmountInput.parse(raw)!!, 0.0001)

    @Test
    fun `plain numbers`() {
        assertParses(1500.0, "1500")
        assertParses(1500.5, "1500.5")
        assertParses(0.5, "0.5")
        assertParses(0.5, ".5")
        assertParses(1500.0, " 1500 ")
    }

    @Test
    fun `comma as decimal separator`() {
        assertParses(1.5, "1,5")
        assertParses(12.5, "12,50")
        assertParses(0.25, "0,25")
    }

    @Test
    fun `thousands separators`() {
        assertParses(12000.0, "12,000")
        assertParses(1500.0, "1,500")
        assertParses(1234567.0, "1,234,567")
        assertParses(1234567.0, "1.234.567")
        assertParses(1500.0, "1 500")
        assertParses(1500.0, "1 500")
    }

    @Test
    fun `both separators - last one is the decimal point`() {
        assertParses(1234.5, "1,234.5")
        assertParses(1234.5, "1.234,5")
        assertParses(1234567.89, "1,234,567.89")
    }

    @Test
    fun `rejects invalid input`() {
        assertNull(AmountInput.parse(""))
        assertNull(AmountInput.parse("   "))
        assertNull(AmountInput.parse("abc"))
        assertNull(AmountInput.parse("12a"))
        assertNull(AmountInput.parse("-5"))
        assertNull(AmountInput.parse(","))
        assertNull(AmountInput.parse("1,2,3"))
        assertNull(AmountInput.parse("1.2.3"))
        assertNull(AmountInput.parse("12,34,567"))
        assertNull(AmountInput.parse("1,234.5.6"))
    }

    @Test
    fun `isInvalid treats blank as not invalid and checks positivity when required`() {
        assertFalse(AmountInput.isInvalid("", requirePositive = true))
        assertTrue(AmountInput.isInvalid("abc", requirePositive = false))
        assertTrue(AmountInput.isInvalid("0", requirePositive = true))
        assertFalse(AmountInput.isInvalid("0", requirePositive = false))
        assertFalse(AmountInput.isInvalid("1,5", requirePositive = true))
    }

    @Test
    fun `format round-trips through parse without scientific notation`() {
        assertEquals("1500", AmountInput.format(1500.0))
        assertEquals("1500.5", AmountInput.format(1500.5))
        assertEquals("10000000", AmountInput.format(1.0E7))
        assertEquals("0", AmountInput.format(0.0))
        listOf(1500.0, 1500.5, 1.0E7, 0.25).forEach {
            assertEquals(it, AmountInput.parse(AmountInput.format(it))!!, 0.0)
        }
    }
}
