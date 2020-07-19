package com.iita.akilimo.utils

import org.junit.Assert
import org.junit.Test

internal class DateHelperTest {
    @Test
    fun dateOlder() {
        val date = "2020-07-16"
        val isOlder = DateHelper.olderThanCurrent(date)
        Assert.assertTrue("Date is in the past", isOlder)
    }

    @Test
    fun dateNewer() {
        val date = "2029-04-15"
        val isNewer = DateHelper.olderThanCurrent(date)
        Assert.assertFalse("Date is in the future", isNewer)
    }
}