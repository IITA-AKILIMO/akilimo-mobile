package com.akilimo.mobile.repos

import com.akilimo.mobile.network.AppVersionApi
import com.akilimo.mobile.network.dto.AppVersionResponse
import com.akilimo.mobile.network.dto.PlatformVersionDto
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import java.io.IOException

class AppVersionRepoTest {

    private val api: AppVersionApi = mockk()
    private val factory: AppVersionApiFactory = mockk()

    @Before
    fun setUp() {
        clearAllMocks()
        every { factory.create() } returns api
    }

    private fun buildRepo() = AppVersionRepo(factory)

    @Test
    fun `the android entry becomes the policy and latest is never below the minimum`() = runTest {
        val body = AppVersionResponse(mapOf("android" to PlatformVersionDto(minSupported = 30, latest = 10, message = " ")))
        coEvery { api.appVersion(any()) } returns Response.success(body)

        val policy = buildRepo().fetchPolicy()

        assertEquals(30, policy?.minSupported)
        assertEquals(30, policy?.latest)
        assertNull(policy?.message)
    }

    @Test
    fun `a server error gives no policy`() = runTest {
        coEvery { api.appVersion(any()) } returns Response.error(500, "".toResponseBody())

        assertNull(buildRepo().fetchPolicy())
    }

    @Test
    fun `an answer without android gives no policy`() = runTest {
        coEvery { api.appVersion(any()) } returns Response.success(AppVersionResponse(emptyMap()))

        assertNull(buildRepo().fetchPolicy())
    }

    @Test
    fun `being offline gives no policy and does not throw`() = runTest {
        coEvery { api.appVersion(any()) } throws java.net.UnknownHostException("offline")

        assertNull(buildRepo().fetchPolicy())
    }

    @Test
    fun `any other failure gives no policy and does not throw`() = runTest {
        coEvery { api.appVersion(any()) } throws IOException("broken")

        assertNull(buildRepo().fetchPolicy())
    }
}
