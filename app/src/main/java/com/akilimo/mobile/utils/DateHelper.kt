package com.akilimo.mobile.utils

import io.sentry.Sentry
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateHelper {
    private val dateFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault())

    fun formatToString(date: LocalDate?): String =
        date?.format(dateFormatter) ?: ""

    fun unixTimeStampToDate(unixTimestamp: Long, timeZone: ZoneId = ZoneId.of("UTC")): LocalDate? =
        runCatching {
            Instant.ofEpochMilli(unixTimestamp).atZone(timeZone).toLocalDate()
        }.onFailure { Sentry.captureException(it) }
            .getOrNull()

    fun unixTimeStampToDateTime(unixTimestamp: Long, timeZone: ZoneId = ZoneId.of("UTC")): LocalDateTime? =
        runCatching {
            Instant.ofEpochMilli(unixTimestamp).atZone(timeZone).toLocalDateTime()
        }.onFailure { Sentry.captureException(it) }
            .getOrNull()

    fun olderThanCurrent(date: LocalDate?): Boolean =
        date?.isBefore(LocalDate.now()) ?: false
}
