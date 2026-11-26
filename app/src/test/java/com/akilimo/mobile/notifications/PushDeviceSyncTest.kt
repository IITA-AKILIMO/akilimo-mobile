package com.akilimo.mobile.notifications

import com.akilimo.mobile.BuildConfig
import com.akilimo.mobile.data.AppSettingsDataStore
import com.akilimo.mobile.data.account.AccountSession
import com.akilimo.mobile.data.account.AccountSessionStore
import com.akilimo.mobile.data.account.FakeSessionStorage
import com.akilimo.mobile.data.account.FakeTokenCipher
import com.akilimo.mobile.entities.AkilimoUser
import com.akilimo.mobile.enums.EnumCountry
import com.akilimo.mobile.network.dto.DeviceRegistrationRequest
import com.akilimo.mobile.repos.AkilimoUserRepo
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PushDeviceSyncTest {

    private val appSettings: AppSettingsDataStore = mockk(relaxed = true)
    private val userRepo: AkilimoUserRepo = mockk(relaxed = true)
    private val sessionStore = AccountSessionStore(FakeSessionStorage(), FakeTokenCipher())
    private val tokens = FakeTokens()
    private val registrar = RecordingRegistrar()

    private var storedToken: String? = null
    private var storedDetails: String? = null
    private var pushOn = true

    private class FakeTokens : FcmTokenSource {
        var token: String? = "fcm-1"
        override suspend fun token(): String? = token
    }

    private class RecordingRegistrar : DeviceRegistrar {
        val registered = mutableListOf<DeviceRegistrationRequest>()
        val unregistered = mutableListOf<String>()
        var failing = false

        override suspend fun register(request: DeviceRegistrationRequest) {
            check(!failing) { "offline" }
            registered += request
        }

        override suspend fun unregister(deviceToken: String) {
            check(!failing) { "offline" }
            unregistered += deviceToken
        }
    }

    @Before
    fun setUp() {
        clearAllMocks()
        every { appSettings.akilimoUser } returns "user1"
        every { appSettings.languageTag } returns "sw-TZ"
        every { appSettings.deviceToken } returns "install-1"
        coEvery { appSettings.pushEnabled() } answers { pushOn }
        coEvery { userRepo.getUser("user1") } returns AkilimoUser(userName = "user1", enumCountry = EnumCountry.TZ)
        coEvery { appSettings.registeredPushToken() } answers { storedToken }
        coEvery { appSettings.registeredPushDetails() } answers { storedDetails }
        coEvery { appSettings.setRegisteredPush(any(), any()) } answers {
            storedToken = firstArg()
            storedDetails = secondArg()
        }
    }

    private fun sync() = PushDeviceSync(appSettings, userRepo, sessionStore, tokens, registrar)

    private fun signIn(consentRequired: Boolean = false, accountId: Long = 5) = sessionStore.save(
        AccountSession("tok", Long.MAX_VALUE, accountId, "Amina", "amina@example.com", false, consentRequired)
    )

    @Test
    fun `registers the install id, token, app version, country and language`() = runTest {
        assertTrue(sync().sync())

        val request = registrar.registered.single()
        assertEquals("install-1", request.deviceToken)
        assertEquals("fcm-1", request.fcmToken)
        assertEquals("android", request.platform)
        assertEquals(BuildConfig.VERSION_NAME, request.appVersion)
        assertEquals("TZ", request.country)
        assertEquals("sw", request.language)
        assertEquals("fcm-1", storedToken)
    }

    @Test
    fun `a call that changes nothing sends nothing`() = runTest {
        val sync = sync()
        sync.sync()
        sync.sync()

        assertEquals(1, registrar.registered.size)
    }

    @Test
    fun `a new token from Firebase is registered even if nothing else changed`() = runTest {
        val sync = sync()
        sync.sync()

        assertTrue(sync.sync(knownToken = "fcm-2"))

        assertEquals(listOf("fcm-1", "fcm-2"), registrar.registered.map { it.fcmToken })
        assertEquals("fcm-2", storedToken)
    }

    @Test
    fun `a country or language change registers again`() = runTest {
        val sync = sync()
        sync.sync()

        every { appSettings.languageTag } returns "rw"
        coEvery { userRepo.getUser("user1") } returns AkilimoUser(userName = "user1", enumCountry = EnumCountry.NG)
        sync.sync()

        assertEquals(listOf("sw" to "TZ", "rw" to "NG"), registrar.registered.map { it.language to it.country })
    }

    @Test
    fun `signing in registers again, and a farmer who has not given consent does not count as signed in`() = runTest {
        val sync = sync()
        sync.sync()

        signIn(consentRequired = true)
        sync.sync()
        assertEquals(1, registrar.registered.size)

        signIn(consentRequired = false)
        sync.sync()
        assertEquals(2, registrar.registered.size)
    }

    @Test
    fun `notifications off removes the phone from the API once`() = runTest {
        val sync = sync()
        sync.sync()

        pushOn = false
        assertTrue(sync.sync())
        assertTrue(sync.sync())

        assertEquals(listOf("install-1"), registrar.unregistered)
        assertNull(storedToken)
        assertNull(storedDetails)
    }

    @Test
    fun `notifications off and never registered sends nothing`() = runTest {
        pushOn = false

        assertTrue(sync().sync())

        assertTrue(registrar.registered.isEmpty())
        assertTrue(registrar.unregistered.isEmpty())
    }

    @Test
    fun `unregister removes a registered phone and forgets the token`() = runTest {
        val sync = sync()
        sync.sync()

        assertTrue(sync.unregister())

        assertEquals(listOf("install-1"), registrar.unregistered)
        assertNull(storedToken)
    }

    @Test
    fun `no Firebase token means nothing is registered and false is reported`() = runTest {
        tokens.token = null

        assertFalse(sync().sync())

        assertTrue(registrar.registered.isEmpty())
    }

    @Test
    fun `a failing registration reports false, stores nothing and is retried`() = runTest {
        val sync = sync()
        registrar.failing = true

        assertFalse(sync.sync())
        assertNull(storedToken)

        registrar.failing = false
        assertTrue(sync.sync())
        assertEquals("fcm-1", storedToken)
    }

    @Test
    fun `a failing unregistration keeps the stored token so it is retried`() = runTest {
        val sync = sync()
        sync.sync()
        registrar.failing = true

        assertFalse(sync.unregister())

        assertEquals("fcm-1", storedToken)
    }
}
