package com.akilimo.mobile.notifications

import com.akilimo.mobile.data.AppSettingsDataStore
import com.akilimo.mobile.entities.AkilimoUser
import com.akilimo.mobile.enums.EnumCountry
import com.akilimo.mobile.repos.AkilimoUserRepo
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PushTopicSyncTest {

    private val appSettings: AppSettingsDataStore = mockk(relaxed = true)
    private val userRepo: AkilimoUserRepo = mockk(relaxed = true)
    private val subscriber = RecordingSubscriber()
    private var stored: Set<String> = emptySet()

    private class RecordingSubscriber : TopicSubscriber {
        val subscribed = mutableListOf<String>()
        val unsubscribed = mutableListOf<String>()
        var failOn: String? = null

        override suspend fun subscribe(topic: String) {
            check(topic != failOn) { "boom" }
            subscribed += topic
        }

        override suspend fun unsubscribe(topic: String) {
            check(topic != failOn) { "boom" }
            unsubscribed += topic
        }
    }

    @Before
    fun setUp() {
        every { appSettings.akilimoUser } returns "user1"
        every { appSettings.languageTag } returns "sw"
        coEvery { appSettings.subscribedPushTopics() } answers { stored }
        coEvery { appSettings.setSubscribedPushTopics(any()) } answers { stored = firstArg() }
        coEvery { userRepo.getUser("user1") } returns AkilimoUser(userName = "user1", enumCountry = EnumCountry.TZ)
    }

    private fun sync() = PushTopicSync(appSettings, userRepo, subscriber)

    @Test
    fun `switch off subscribes to nothing`() = runTest {
        coEvery { appSettings.pushEnabled() } returns false

        assertTrue(sync().sync())

        assertTrue(subscriber.subscribed.isEmpty())
        assertEquals(emptySet<String>(), stored)
    }

    @Test
    fun `switch on subscribes to country and language topics`() = runTest {
        coEvery { appSettings.pushEnabled() } returns true

        assertTrue(sync().sync())

        assertEquals(setOf("country_TZ", "lang_sw"), subscriber.subscribed.toSet())
        assertEquals(setOf("country_TZ", "lang_sw"), stored)
    }

    @Test
    fun `switching off leaves every stored topic`() = runTest {
        coEvery { appSettings.pushEnabled() } returns false
        stored = setOf("country_TZ", "lang_sw")

        sync().sync()

        assertEquals(setOf("country_TZ", "lang_sw"), subscriber.unsubscribed.toSet())
        assertEquals(emptySet<String>(), stored)
    }

    @Test
    fun `language change swaps only the language topic`() = runTest {
        coEvery { appSettings.pushEnabled() } returns true
        stored = setOf("country_TZ", "lang_en")

        sync().sync()

        assertEquals(listOf("lang_en"), subscriber.unsubscribed)
        assertEquals(listOf("lang_sw"), subscriber.subscribed)
        assertEquals(setOf("country_TZ", "lang_sw"), stored)
    }

    @Test
    fun `failed topic is not recorded so the next sync retries it`() = runTest {
        coEvery { appSettings.pushEnabled() } returns true
        subscriber.failOn = "lang_sw"

        assertFalse(sync().sync())

        assertEquals(setOf("country_TZ"), stored)
        coVerify { appSettings.setSubscribedPushTopics(setOf("country_TZ")) }
    }
}
