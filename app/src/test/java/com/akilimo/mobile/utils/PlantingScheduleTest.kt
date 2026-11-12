package com.akilimo.mobile.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class PlantingScheduleTest {

    private val planting = LocalDate.of(2026, 11, 28)

    @Test
    fun `harvest window is 235 to 452 days after planting`() {
        assertEquals(LocalDate.of(2027, 7, 21), PlantingSchedule.earliestHarvest(planting))
        assertEquals(LocalDate.of(2028, 2, 23), PlantingSchedule.latestHarvest(planting))
    }

    @Test
    fun `the schedule the engine refused is not valid`() {
        // 212 days: GlitchTip / engine "must be between 235 and 452 days"
        assertFalse(PlantingSchedule.isValid(planting, LocalDate.of(2027, 6, 28)))
    }

    @Test
    fun `both ends of the window are valid and one day outside is not`() {
        assertTrue(PlantingSchedule.isValid(planting, LocalDate.of(2027, 7, 21)))
        assertTrue(PlantingSchedule.isValid(planting, LocalDate.of(2028, 2, 23)))
        assertFalse(PlantingSchedule.isValid(planting, LocalDate.of(2027, 7, 20)))
        assertFalse(PlantingSchedule.isValid(planting, LocalDate.of(2028, 2, 24)))
    }
}