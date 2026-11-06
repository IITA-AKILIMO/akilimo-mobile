package com.akilimo.mobile.base.workers

import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class WorkerRetryPolicyTest {

    private fun http(code: Int) = HttpException(Response.error<Any>(code, "".toResponseBody()))

    @Test
    fun `network and timeout errors are transient`() {
        assertTrue(WorkerRetryPolicy.isTransient(IOException("reset")))
        assertTrue(WorkerRetryPolicy.isTransient(SocketTimeoutException()))
        assertTrue(WorkerRetryPolicy.isTransient(UnknownHostException()))
    }

    @Test
    fun `server errors, 408 and 429 are transient`() {
        assertTrue(WorkerRetryPolicy.isTransient(http(500)))
        assertTrue(WorkerRetryPolicy.isTransient(http(503)))
        assertTrue(WorkerRetryPolicy.isTransient(http(408)))
        assertTrue(WorkerRetryPolicy.isTransient(http(429)))
    }

    @Test
    fun `client errors and programming errors are permanent`() {
        assertFalse(WorkerRetryPolicy.isTransient(http(400)))
        assertFalse(WorkerRetryPolicy.isTransient(http(401)))
        assertFalse(WorkerRetryPolicy.isTransient(http(404)))
        assertFalse(WorkerRetryPolicy.isTransient(IllegalStateException("bad json")))
    }

    @Test
    fun `shouldRetry stops at MAX_ATTEMPTS`() {
        val error = http(503)
        assertTrue(WorkerRetryPolicy.shouldRetry(error, runAttemptCount = 0))
        assertTrue(WorkerRetryPolicy.shouldRetry(error, runAttemptCount = WorkerRetryPolicy.MAX_ATTEMPTS - 2))
        assertFalse(WorkerRetryPolicy.shouldRetry(error, runAttemptCount = WorkerRetryPolicy.MAX_ATTEMPTS - 1))
    }

    @Test
    fun `shouldRetry never retries permanent errors`() {
        assertFalse(WorkerRetryPolicy.shouldRetry(http(404), runAttemptCount = 0))
    }
}
