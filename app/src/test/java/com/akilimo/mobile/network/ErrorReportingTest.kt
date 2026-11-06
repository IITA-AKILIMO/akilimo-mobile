package com.akilimo.mobile.network

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class ErrorReportingTest {

    @Test
    fun `offline and timeout errors are expected`() {
        assertTrue(UnknownHostException("api.akilimo.org").isExpectedNetworkError())
        assertTrue(SocketTimeoutException().isExpectedNetworkError())
        assertTrue(IOException("Failed after 3 attempts", SocketTimeoutException()).isExpectedNetworkError())
    }

    @Test
    fun `bugs are reported even when wrapped as IOException`() {
        assertFalse(IllegalStateException("bad state").isExpectedNetworkError())
        // SafeNetworkInterceptor wraps non-IO errors like this
        assertFalse(IOException("Network error", NullPointerException()).isExpectedNetworkError())
    }
}
