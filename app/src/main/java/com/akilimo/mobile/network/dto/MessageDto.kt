package com.akilimo.mobile.network.dto

import com.akilimo.mobile.entities.InboxMessage
import com.akilimo.mobile.notifications.NotificationTarget
import com.akilimo.mobile.notifications.NotificationType
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.time.OffsetDateTime

/** `GET v1/messages`: a plain list, no paging envelope. */
@JsonClass(generateAdapter = true)
data class MessagesResponse(
    @param:Json(name = "data") val data: List<MessageDto>,
)

/** A message as the API sends it (see the API's docs/MESSAGES_API.md). */
@JsonClass(generateAdapter = true)
data class MessageDto(
    @param:Json(name = "id") val id: String,
    @param:Json(name = "type") val type: String? = null,
    @param:Json(name = "title") val title: String? = null,
    @param:Json(name = "body") val body: String? = null,
    @param:Json(name = "target") val target: String? = null,
    /** ISO 8601 server time; also the cursor for the next sync. */
    @param:Json(name = "created_at") val createdAt: String? = null,
) {
    /** Epoch millis of [createdAt], or null when it is missing or not a valid time. */
    val createdAtMillis: Long?
        get() = createdAt?.let { runCatching { OffsetDateTime.parse(it).toInstant().toEpochMilli() }.getOrNull() }

    /**
     * The inbox entry, or null when the message has no title or body. Ordered by the server's time so history reads the
     * same however each message arrived; [fallbackTime] is used if the server sent none. [read] is true for old messages
     * a fresh install fetches, so they do not all show as new.
     */
    fun toEntity(fallbackTime: Long, read: Boolean): InboxMessage? {
        val title = title?.takeIf { it.isNotBlank() } ?: return null
        val body = body?.takeIf { it.isNotBlank() } ?: return null
        return InboxMessage(
            id = id,
            type = NotificationType.fromWireName(type).wireName,
            title = title,
            body = body,
            target = NotificationTarget.fromWireName(target)?.wireName,
            receivedAt = createdAtMillis ?: fallbackTime,
            isRead = read,
        )
    }
}
