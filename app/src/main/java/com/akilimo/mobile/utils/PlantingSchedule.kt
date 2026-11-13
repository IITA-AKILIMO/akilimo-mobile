package com.akilimo.mobile.utils

import java.time.LocalDate

/**
 * How far apart planting and harvest may be. The recommendation engine refuses a schedule outside
 * [MIN_DAYS]..[MAX_DAYS] ("the time from planting to harvest must be between 235 and 452 days"),
 * so the date pickers offer only harvest dates inside it. The flexibility windows do not widen it.
 */
object PlantingSchedule {
    const val MIN_DAYS = 235L
    const val MAX_DAYS = 452L

    fun earliestHarvest(planting: LocalDate): LocalDate = planting.plusDays(MIN_DAYS)

    fun latestHarvest(planting: LocalDate): LocalDate = planting.plusDays(MAX_DAYS)

    fun isValid(planting: LocalDate, harvest: LocalDate): Boolean =
        !harvest.isBefore(earliestHarvest(planting)) && !harvest.isAfter(latestHarvest(planting))
}