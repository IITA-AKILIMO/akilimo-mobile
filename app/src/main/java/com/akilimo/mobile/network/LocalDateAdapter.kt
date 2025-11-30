package com.akilimo.mobile.network

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.time.LocalDate
class LocalDateAdapter {

    @FromJson
    fun fromJson(date: String): LocalDate {
        return LocalDate.parse(date) // expects yyyy-MM-dd
    }

    @ToJson
    fun toJson(localDate: LocalDate): String {
        return localDate.toString() // produces yyyy-MM-dd
    }
}

