package com.akilimo.mobile.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A message the farmer received from AKILIMO (push or, later, the API), kept on the phone as a history.
 * [id] is the sender's message id, so the same message arriving twice is stored once.
 */
@Entity(tableName = "inbox_messages", indices = [Index("received_at")])
data class InboxMessage(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: String,
    /** advice, reminder or announcement (see NotificationType.wireName). */
    @ColumnInfo(name = "type") val type: String,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "body") val body: String,
    /** hub, recommendations, settings or inbox; null means the type's default screen. */
    @ColumnInfo(name = "target") val target: String?,
    /** Epoch millis the message reached the phone. */
    @ColumnInfo(name = "received_at") val receivedAt: Long,
    @ColumnInfo(name = "is_read") val isRead: Boolean = false,
)
