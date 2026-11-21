package com.akilimo.mobile.network

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import java.net.SocketTimeoutException
import kotlin.coroutines.cancellation.CancellationException

class CancellationExtTest {

    @Test
    fun `okhttp canceled IOException is a cancellation`() {
        assertTrue(IOException("Canceled").isCancellation())
    }

    @Test
    fun `cancellation wrapped as a cause is detected`() {
        assertTrue(IOException("Network error", IOException("Canceled")).isCancellation())
        assertTrue(IllegalStateException("x", CancellationException()).isCancellation())
    }

    @Test
    fun `other failures are not cancellations`() {
        assertFalse(SocketTimeoutException("timeout").isCancellation())
        assertFalse(IOException("Network error").isCancellation())
    }
}

class ExpectedNetworkFailureTest {

    private fun http(code: Int) = retrofit2.HttpException(
        retrofit2.Response.error<Any>(
            code,
            okhttp3.ResponseBody.create(null, "")
        )
    )

    @Test
    fun `connectivity failures are expected`() {
        assertTrue(java.net.UnknownHostException("host").isExpectedNetworkFailure())
        assertTrue(SocketTimeoutException("timeout").isExpectedNetworkFailure())
        assertTrue(java.net.ConnectException("refused").isExpectedNetworkFailure())
        assertTrue(IOException("Network error", java.net.SocketException("reset")).isExpectedNetworkFailure())
    }

    @Test
    fun `client errors are expected but server errors are not`() {
        assertTrue(http(404).isExpectedNetworkFailure())
        assertTrue(http(429).isExpectedNetworkFailure())
        assertFalse(http(500).isExpectedNetworkFailure())
    }

    @Test
    fun `tls certificate and parsing problems are not expected`() {
        assertFalse(javax.net.ssl.SSLPeerUnverifiedException("cert").isExpectedNetworkFailure())
        assertFalse(IllegalStateException("bug").isExpectedNetworkFailure())
    }
}
