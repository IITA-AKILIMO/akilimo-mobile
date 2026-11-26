package com.akilimo.mobile.notifications

import com.akilimo.mobile.data.AppSettingsDataStore
import com.akilimo.mobile.entities.AkilimoUser
import com.akilimo.mobile.entities.InboxMessage
import com.akilimo.mobile.enums.EnumCountry
import com.akilimo.mobile.network.dto.MessageDto
import com.akilimo.mobile.repos.AkilimoUserRepo
import com.akilimo.mobile.repos.InboxRepo
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MessageSyncTest {

    private val appSettings: AppSettingsDataStore = mockk(relaxed = true)
    private val userRepo: AkilimoUserRepo = mockk(relaxed = true)
    private val inbox: InboxRepo = mockk(relaxed = true)
    private val saved = mutableListOf<InboxMessage>()
    private var audience: String? = null
    private var cursor: String? = null

    private class FakeFetcher : MessageFetcher {
        /** Everything the "server" holds, oldest first. */
        var all = listOf<MessageDto>()
        val calls = mutableListOf<Triple<String?, String?, String?>>()
        val deviceTokens = mutableListOf<String?>()
        var failing = false

        override suspend fun fetch(
            country: String?,
            language: String?,
            since: String?,
            limit: Int,
            deviceToken: String?,
        ): List<MessageDto> {
            check(!failing) { "offline" }
            calls += Triple(country, language, since)
            deviceTokens += deviceToken
            return all.filter { since == null || it.createdAt!! >= since }.take(limit)
        }
    }

    private val fetcher = FakeFetcher()

    private fun dto(id: String, at: String) = MessageDto(id = id, type = "advice", title = "T$id", body = "B$id", createdAt = at)

    @Before
    fun setUp() {
        every { appSettings.akilimoUser } returns "user1"
        every { appSettings.languageTag } returns "sw-TZ"
        every { appSettings.deviceToken } returns "install-1"
        coEvery { userRepo.getUser("user1") } returns AkilimoUser(userName = "user1", enumCountry = EnumCountry.TZ)
        coEvery { appSettings.messageSyncAudience() } answers { audience }
        coEvery { appSettings.messageSyncCursor() } answers { cursor }
        coEvery { appSettings.setMessageSync(any(), any()) } answers {
            audience = firstArg()
            cursor = secondArg()
        }
        coEvery { inbox.save(any()) } answers { saved += firstArg<InboxMessage>() }
    }

    private fun sync() = MessageSync(appSettings, userRepo, inbox, fetcher)

    @Test
    fun `first fetch asks for the farmers country and language and stores old messages as read`() = runTest {
        fetcher.all = listOf(dto("a", "2026-11-01T10:00:00+00:00"), dto("b", "2026-11-02T10:00:00+00:00"))

        assertTrue(sync().sync())

        assertEquals(Triple("TZ", "sw", null), fetcher.calls.first())
        assertEquals(listOf("a", "b"), saved.map { it.id })
        assertTrue(saved.all { it.isRead })
        assertEquals("TZ|sw", audience)
        assertEquals("2026-11-02T10:00:00+00:00", cursor)
    }

    @Test
    fun `every fetch carries the install id so private messages arrive`() = runTest {
        fetcher.all = listOf(dto("a", "2026-11-01T10:00:00+00:00"))

        sync().sync()
        sync().sync()

        assertTrue(fetcher.deviceTokens.isNotEmpty())
        assertTrue(fetcher.deviceTokens.all { it == "install-1" })
    }

    @Test
    fun `later fetches continue after the cursor and keep new messages unread`() = runTest {
        fetcher.all = listOf(dto("a", "2026-11-01T10:00:00+00:00"))
        sync().sync()
        saved.clear()

        fetcher.all += dto("b", "2026-11-03T10:00:00+00:00")
        assertTrue(sync().sync())

        assertEquals("2026-11-01T10:00:01Z", fetcher.calls.last().third)
        assertEquals(listOf("b"), saved.map { it.id })
        assertFalse(saved.single().isRead)
    }

    @Test
    fun `a message that arrives after an empty first fetch is new`() = runTest {
        sync().sync()
        assertNull(cursor)

        fetcher.all = listOf(dto("a", "2026-11-01T10:00:00+00:00"))
        sync().sync()

        assertFalse(saved.single().isRead)
    }

    @Test
    fun `a new country or language starts over for that audience`() = runTest {
        fetcher.all = listOf(dto("a", "2026-11-01T10:00:00+00:00"))
        sync().sync()
        saved.clear()

        every { appSettings.languageTag } returns "rw"
        fetcher.all += dto("b", "2026-11-03T10:00:00+00:00")
        sync().sync()

        assertNull(fetcher.calls.last().third)
        assertEquals("TZ|rw", audience)
        assertTrue(saved.all { it.isRead })
    }

    @Test
    fun `resetAndSync empties the inbox and fetches again from scratch as read`() = runTest {
        coEvery { inbox.deleteAll() } answers { saved.clear() }
        fetcher.all = listOf(dto("a", "2026-11-01T10:00:00+00:00"), dto("b", "2026-11-02T10:00:00+00:00"))
        sync().sync()
        saved.clear()
        fetcher.all += dto("c", "2026-11-03T10:00:00+00:00")

        assertTrue(sync().resetAndSync())

        coVerify { inbox.deleteAll() }
        assertNull(fetcher.calls.last().third) // from the start, not after the old cursor
        assertEquals(listOf("a", "b", "c"), saved.map { it.id })
        assertTrue(saved.all { it.isRead })
    }

    @Test
    fun `a failing fetch reports false and keeps the cursor`() = runTest {
        fetcher.all = listOf(dto("a", "2026-11-01T10:00:00+00:00"))
        sync().sync()

        fetcher.failing = true

        assertFalse(sync().sync())
        assertEquals("2026-11-01T10:00:00+00:00", cursor)
    }

    @Test
    fun `messages without a title or body are not stored`() = runTest {
        fetcher.all = listOf(
            MessageDto(id = "x", type = "advice", title = null, body = "B", createdAt = "2026-11-01T10:00:00+00:00"),
            dto("ok", "2026-11-01T11:00:00+00:00"),
        )

        sync().sync()

        assertEquals(listOf("ok"), saved.map { it.id })
    }

    @Test
    fun `unknown type becomes an announcement and the server time orders the message`() = runTest {
        fetcher.all = listOf(MessageDto(id = "a", type = "weird", title = "T", body = "B", createdAt = "2026-11-01T10:00:00+00:00"))

        sync().sync()

        assertEquals("announcement", saved.single().type)
        assertEquals(1_793_527_200_000L, saved.single().receivedAt)
    }
}
