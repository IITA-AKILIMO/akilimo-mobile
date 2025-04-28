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

@Deprecated("Find better ways to implement the functions listed")
object DateHelper {

    var format: String = "dd/MM/yyyy"
    var dateTimeFormat: String = "dd-MM-yyyy"

    val simpleDateFormatter: SimpleDateFormat
        get() = SimpleDateFormat(format, Locale.getDefault())

    private val dateTimeFormatter: DateTimeFormatter
        get() = DateTimeFormatter.ofPattern(dateTimeFormat)


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


    fun formatToLocalDate(dateString: String?): LocalDate {
        return LocalDate.parse(dateString, dateTimeFormatter)
    }

    fun unixTimeStampToDate(unixTimestamp: Long, timeZone: ZoneId?): LocalDateTime {
        return try {
            Instant.ofEpochMilli(unixTimestamp).atZone(timeZone ?: ZoneId.systemDefault())
                .toLocalDateTime()
        } catch (ex: Exception) {
            Sentry.captureException(ex)
            LocalDateTime.now()
        }
    }

    fun olderThanCurrent(dateOne: String?): Boolean {
        val currentDate = LocalDate.now()
        val d1 = formatToLocalDate(dateOne)
        return d1.isBefore(currentDate)
    }

    private fun parseDate(dateString: String?): Date? {
        return try {
            dateString?.let { simpleDateFormatter.parse(it) }
        } catch (ex: ParseException) {
            Sentry.captureException(ex)
            null
        }
    }
}
