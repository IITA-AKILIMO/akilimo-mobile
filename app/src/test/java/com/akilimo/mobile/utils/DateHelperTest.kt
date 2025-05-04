package com.akilimo.mobile.utils

import android.content.Context
import com.jakewharton.threetenabp.AndroidThreeTen
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

internal class DateHelperTest {
    private lateinit var mockContext: Context

    @Before
    fun setUp() {
        mockContext = Mockito.mock(Context::class.java)
        AndroidThreeTen.init(mockContext) // `mockContext` can be a mock context for unit tests
    }

    @Test
    fun formatLongToDateString_validTimestamp() {
        val timestamp = 1609459200000L // Represents 1st January 2021
        val formattedDate = DateHelper.formatLongToDateString(timestamp)
        Assert.assertEquals("January 01, 2021", formattedDate)
    }

    @Test
    fun formatLongToDateString_timestampZero() {
        val timestamp = 0L // Represents epoch time
        val formattedDate = DateHelper.formatLongToDateString(timestamp)
        Assert.assertEquals("January 01, 1970", formattedDate)
    }

    @Test
    fun formatLongToDateString_largeTimestamp() {
        val timestamp = 4070908800000L // Represents 1st January 2100
        val formattedDate = DateHelper.formatLongToDateString(timestamp)
        Assert.assertEquals("January 01, 2099", formattedDate)
    }
}
