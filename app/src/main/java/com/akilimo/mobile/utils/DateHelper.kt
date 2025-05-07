package com.akilimo.mobile.utils

import io.sentry.Sentry
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateHelper {

    // Make format immutable to avoid accidental changes
    private const val DEFAULT_FORMAT = "dd/MM/yyyy"

    val simpleDateFormatter: SimpleDateFormat = SimpleDateFormat(DEFAULT_FORMAT, Locale.ENGLISH)

    fun getMaxDate(maxMonths: Int): Calendar = getAdjustedCalendar(maxMonths)

    fun getMinDate(minMonths: Int): Calendar = getAdjustedCalendar(minMonths)

    private fun getAdjustedCalendar(months: Int): Calendar =
        Calendar.getInstance().apply { add(Calendar.MONTH, months) }

    fun getFutureOrPastMonth(referenceDate: String?, months: Int): Calendar {
        val calendar = Calendar.getInstance()
        referenceDate?.let { dateStr ->
            parseDate(dateStr)?.let { calendar.time = it }
        }
        calendar.add(Calendar.MONTH, months)
        return calendar
    }

    fun formatToLocalDate(dateString: String?, dateFormat: String = DEFAULT_FORMAT): LocalDate? {
        if (dateString.isNullOrEmpty()) return null
        return try {
            LocalDate.parse(dateString, DateTimeFormatter.ofPattern(dateFormat))
        } catch (e: Exception) {
            Sentry.captureException(e)
            null
        }
    }

    fun unixTimeStampToDate(unixTimestamp: Long, timeZone: ZoneId? = null): LocalDateTime {
        return try {
            Instant.ofEpochMilli(unixTimestamp).atZone(timeZone ?: ZoneId.systemDefault())
                .toLocalDateTime()
        } catch (ex: Exception) {
            Sentry.captureException(ex)
            LocalDateTime.now()
        }
    }

    fun olderThanCurrent(dateOne: String?, dateFormat: String = DEFAULT_FORMAT): Boolean {
        val currentDate = LocalDate.now()
        val d1 = formatToLocalDate(dateOne, dateFormat)
        return d1?.isBefore(currentDate) == true
    }

    // This function parses a date string using the default format or a custom format and returns a Date object.
    private fun parseDate(dateString: String?): Date? {
        return try {
            dateString?.let { simpleDateFormatter.parse(it) }
        } catch (ex: ParseException) {
            Sentry.captureException(ex)
            null
        }
    }

    // This method formats the given Date object into a human-readable string.
    private fun formatCalendarToDateString(dateTime: Date): String {
        val newFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.ENGLISH)
        return newFormat.format(dateTime)
    }

    // This method formats the given long timestamp into a human-readable date string.
    fun formatLongToDateString(dateTime: Long): String {
        return formatCalendarToDateString(Date(dateTime))
    }
}