package com.akilimo.mobile.network

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import okhttp3.Call
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.io.IOException

class RetryInterceptorTest {

    private val interceptor = RetryInterceptor(maxAttempts = 3, backoffMillis = 0)

    private val getRequest = Request.Builder().url("https://api.example.org/items").get().build()
    private val postRequest = Request.Builder()
        .url("https://api.example.org/feedback")
        .post("{}".toRequestBody("application/json".toMediaType()))
        .build()

    private fun chainFor(request: Request, canceled: Boolean = false): Interceptor.Chain {
        val call: Call = mockk { every { isCanceled() } returns canceled }
        return mockk {
            every { request() } returns request
            every { call() } returns call
        }
    }

    private fun ok(request: Request) = Response.Builder()
        .request(request).protocol(Protocol.HTTP_1_1).code(200).message("OK").build()

    @Test
    fun `GET is retried until it succeeds`() {
        val chain = chainFor(getRequest)
        var calls = 0
        every { chain.proceed(getRequest) } answers {
            calls++
            if (calls < 3) throw IOException("timeout") else ok(getRequest)
        }

        val response = interceptor.intercept(chain)

        assertEquals(200, response.code)
        assertEquals(3, calls)
    }

    @Test
    fun `GET gives up after maxAttempts`() {
        val chain = chainFor(getRequest)
        every { chain.proceed(getRequest) } throws IOException("timeout")

        assertThrows(IOException::class.java) { interceptor.intercept(chain) }
        verify(exactly = 3) { chain.proceed(getRequest) }
    }

    @Test
    fun `POST is not retried so the server never receives it twice`() {
        val chain = chainFor(postRequest)
        every { chain.proceed(postRequest) } throws IOException("read timeout")

        assertThrows(IOException::class.java) { interceptor.intercept(chain) }
        verify(exactly = 1) { chain.proceed(postRequest) }
    }

    @Test
    fun `canceled call is not retried`() {
        val chain = chainFor(getRequest, canceled = true)

        assertThrows(IOException::class.java) { interceptor.intercept(chain) }
        verify(exactly = 0) { chain.proceed(any()) }
    }
}
