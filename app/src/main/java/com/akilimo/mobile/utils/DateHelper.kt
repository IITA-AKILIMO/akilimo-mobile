package com.akilimo.mobile.utils

import io.sentry.Sentry
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Deprecated("Find better ways to  implement the functions listed")
object DateHelper {
    private val LOG_TAG: String = DateHelper::class.java.simpleName
    var format: String = "dd/MM/yyyy"

    @JvmField
    var dateTimeFormat: String = "yyyy-MM-dd"
    private var simpleDateFormat: SimpleDateFormat? = null

    @JvmStatic
    val simpleDateFormatter: SimpleDateFormat
        get() = SimpleDateFormat(format, Locale.UK)

    val dateTimeFormatter: DateTimeFormatter
        get() = DateTimeFormat.forPattern(format)

    @Deprecated("To be removed ASAP")
    fun getWeekNumber(dateString: String): Int {
        var weekNumber = 0
        try {
            val date = simpleDateFormatter.parse(dateString)
            val cal = Calendar.getInstance()
            cal.time = date
            weekNumber = cal[Calendar.WEEK_OF_YEAR]
        } catch (ex: ParseException) {
            Sentry.captureException(ex)
        }
        return weekNumber
    }

    fun getDayNumber(dateString: String): Int {
        var weekNumber = 0
        try {
            val date = simpleDateFormatter.parse(dateString)
            val cal = Calendar.getInstance()
            cal.time = date
            weekNumber = cal[Calendar.DAY_OF_YEAR]
        } catch (ex: ParseException) {
            Sentry.captureException(ex)
        }
        return weekNumber
    }

    @Deprecated("To be removed ASAP")
    fun getDateFromWeeks(weekNumber: Int, fromDate: String): String {
        var refDate: Date? = null
        try {
            refDate = simpleDateFormatter.parse(fromDate)
        } catch (ex: ParseException) {
            Sentry.captureException(ex)
        }

        val dt = DateTime(refDate)
        val computedDate = dt.plusWeeks(weekNumber) //dt.minusWeeks(weekNumber);

        return dateTimeFormatter.print(computedDate) //.format(computedDate);
    }

    fun getDateFromDays(dayNumber: Int, fromDate: String): String {
        var refDate: Date? = null
        try {
            refDate = simpleDateFormatter.parse(fromDate)
        } catch (ex: ParseException) {
            Sentry.captureException(ex)
        }

        val dt = DateTime(refDate)
        val computedDate = dt.plusDays(dayNumber)

        return dateTimeFormatter.print(computedDate)
    }

    @Deprecated("")
    fun getDateFromWeekOfYear(weekNumber: Int): String {
        simpleDateFormat = simpleDateFormatter
        val cal = Calendar.getInstance()
        cal[Calendar.WEEK_OF_YEAR] = weekNumber
        cal[Calendar.DAY_OF_WEEK] = Calendar.SUNDAY

        return simpleDateFormat!!.format(cal.time)
    }

    fun getDateFromDayOfYear(dayOfYear: Int): String {
        simpleDateFormat = simpleDateFormatter
        val cal = Calendar.getInstance()
        cal[Calendar.DAY_OF_YEAR] = dayOfYear
        return simpleDateFormat!!.format(cal.time)
    }


    fun currentDayNumber(): Int {
        val today = Date()
        simpleDateFormat = simpleDateFormatter
        val currDate = simpleDateFormat!!.format(today.time)

        return getDayNumber(currDate)
    }

    val currentYear: Int
        get() = getCalendarNumber(Calendar.YEAR)

    val currentMonth: Int
        get() = getCalendarNumber(Calendar.MONTH)

    val currentDay: Int
        get() = getCalendarNumber(Calendar.DAY_OF_MONTH)

    private fun getCalendarNumber(calendarValueType: Int): Int {
        val c = Calendar.getInstance()
        return c[calendarValueType]
    }

    fun getMaxDate(maxMonths: Int): Calendar {
        return getFutureOrPastMonth(maxMonths)
    }

    fun getMinDate(minMonths: Int): Calendar {
        return getFutureOrPastMonth(minMonths)
    }

    private fun getFutureOrPastMonth(maxMonth: Int): Calendar {
        simpleDateFormat = simpleDateFormatter
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, maxMonth)
        return cal
    }

    fun getFutureOrPastMonth(referenceDate: String?, maxMonth: Int): Calendar {
        val cal = Calendar.getInstance()
        try {
            if (referenceDate != null) {
                val refDate = simpleDateFormatter.parse(referenceDate)
                if (refDate != null) {
                    cal.time = refDate // all done
                }
                cal.add(Calendar.MONTH, maxMonth)
            }
        } catch (ex: ParseException) {
            Sentry.captureException(ex)
        }
        return cal
    }

    fun formatToLocalDateTime(dateString: String?): LocalDateTime {
        return LocalDateTime.parse(
            dateString, DateTimeFormat.forPattern(
                dateTimeFormat
            )
        )
    }

    @JvmStatic
    fun formatToLocalDate(dateString: String?): LocalDate {
        return LocalDate.parse(
            dateString, DateTimeFormat.forPattern(
                dateTimeFormat
            )
        )
    }

    fun unixTimeStampToDate(unixTimestamp: Long, timeZone: DateTimeZone?): DateTime {
        var parse = DateTime()
        try {
            val dateTime = DateTime(unixTimestamp)
            val zoneTime = dateTime.withZone(timeZone)
            val dateString = zoneTime.toString()
            parse = DateTime.parse(dateString)
        } catch (ex: Exception) {
            Sentry.captureException(ex)
        }
        return parse
    }

    @JvmStatic
    fun olderThanCurrent(dateOne: String?): Boolean {
        val d1 = formatToLocalDate(dateOne)
        val currentDate = LocalDate.now()

        var dateOlderThanCurrent = false
        if (d1.compareTo(currentDate) > 0) {
            dateOlderThanCurrent = false
        } else if (d1.compareTo(currentDate) < 0) {
            dateOlderThanCurrent = true
        } else if (d1.compareTo(currentDate) == 0) {
            dateOlderThanCurrent = false
        }
        return dateOlderThanCurrent
    }
}
