package com.akilimo.mobile.utils

import org.junit.Assert
import org.junit.Test

internal class ValidationHelperTest {
    private val validation = ValidationHelper()

    @Test
    fun tested_email_should_be_valid() {
        val isValid = validation.isValidEmail("sammy@tsobu.co.ke")
        Assert.assertTrue("This email is valid", isValid)
    }

    @Test
    fun tested_email_should_be_invalid() {
        val isValid = validation.isValidEmail("sammy")
        Assert.assertFalse("This email is invalid", isValid)
    }
}
