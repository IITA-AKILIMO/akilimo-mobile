package com.akilimo.mobile.notifications

import com.akilimo.mobile.enums.EnumCountry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NotificationModelTest {

    @Test
    fun `type parses wire names case-insensitively and falls back to announcement`() {
        assertEquals(NotificationType.ADVICE, NotificationType.fromWireName("advice"))
        assertEquals(NotificationType.REMINDER, NotificationType.fromWireName(" Reminder "))
        assertEquals(NotificationType.ANNOUNCEMENT, NotificationType.fromWireName("nonsense"))
        assertEquals(NotificationType.ANNOUNCEMENT, NotificationType.fromWireName(null))
    }

    @Test
    fun `each type has its own channel`() {
        val ids = NotificationType.entries.map { it.channelId }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun `explicit target overrides the type default`() {
        assertEquals(
            NotificationTarget.SETTINGS,
            NotificationRouting.resolveTarget(NotificationType.ADVICE, "settings"),
        )
    }

    @Test
    fun `missing or unknown target uses the type default`() {
        assertEquals(
            NotificationTarget.INBOX,
            NotificationRouting.resolveTarget(NotificationType.ADVICE, null),
        )
        assertEquals(
            NotificationTarget.INBOX,
            NotificationRouting.resolveTarget(NotificationType.REMINDER, "unknown-screen"),
        )
    }

    @Test
    fun `intent without notification extras routes nowhere`() {
        assertNull(NotificationRouting.fromExtras(null, null))
    }

    @Test
    fun `fcm launch extras route by type when no target is given`() {
        assertEquals(NotificationTarget.INBOX, NotificationRouting.fromExtras("advice", null))
        assertEquals(NotificationTarget.HUB, NotificationRouting.fromExtras(null, "hub"))
    }

    @Test
    fun `topics combine country and language`() {
        assertEquals(setOf("country_NG", "lang_sw"), NotificationTopics.forUser(EnumCountry.NG, "sw-TZ"))
    }

    @Test
    fun `unsupported country and blank language give no topics`() {
        assertEquals(emptySet<String>(), NotificationTopics.forUser(EnumCountry.Unsupported, ""))
        assertEquals(emptySet<String>(), NotificationTopics.forUser(null, null))
    }

    @Test
    fun `language with odd characters is skipped so topic names stay valid`() {
        assertEquals(setOf("country_RW"), NotificationTopics.forUser(EnumCountry.RW, "r/w"))
        assertEquals(setOf("lang_rw"), NotificationTopics.forUser(null, "RW_rw"))
    }

    @Test
    fun `inbox is a valid explicit target`() {
        assertEquals(NotificationTarget.INBOX, NotificationRouting.resolveTarget(NotificationType.ADVICE, "inbox"))
    }

    private fun entry(data: Map<String, String>, messageId: String? = null, title: String? = null, body: String? = null) =
        InboxMessages.fromPayload(data, title, body, messageId, generatedId = "generated", receivedAt = 1_000L)

    @Test
    fun `payload becomes an unread inbox entry`() {
        val message = entry(
            mapOf("id" to "m1", "type" to "advice", "title" to "T", "body" to "B", "target" to "recommendations"),
        )!!
        assertEquals("m1", message.id)
        assertEquals("advice", message.type)
        assertEquals("T", message.title)
        assertEquals("B", message.body)
        assertEquals("recommendations", message.target)
        assertEquals(1_000L, message.receivedAt)
        assertEquals(false, message.isRead)
    }

    @Test
    fun `id falls back to the fcm message id then a generated one`() {
        val data = mapOf("title" to "T", "body" to "B")
        assertEquals("fcm-1", entry(data, messageId = "fcm-1")!!.id)
        assertEquals("generated", entry(data)!!.id)
        assertEquals("generated", entry(data + ("id" to " "), messageId = " ")!!.id)
    }

    @Test
    fun `unknown type is stored as announcement and unknown target as none`() {
        val message = entry(mapOf("type" to "weird", "target" to "nowhere", "title" to "T", "body" to "B"))!!
        assertEquals("announcement", message.type)
        assertNull(message.target)
    }

    @Test
    fun `notification block fills in a missing title and body`() {
        val message = entry(emptyMap(), title = "NT", body = "NB")!!
        assertEquals("NT", message.title)
        assertEquals("NB", message.body)
    }

    @Test
    fun `payload without title or body is not stored`() {
        assertNull(entry(mapOf("body" to "B")))
        assertNull(entry(mapOf("title" to "T")))
    }
}
