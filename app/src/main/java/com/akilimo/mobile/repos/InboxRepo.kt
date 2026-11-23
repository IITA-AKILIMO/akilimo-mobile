package com.akilimo.mobile.repos

import com.akilimo.mobile.dao.InboxMessageDao
import com.akilimo.mobile.entities.InboxMessage
import kotlinx.coroutines.flow.Flow

/** The farmer's message history. The only layer view models and the messaging service call. */
class InboxRepo(private val dao: InboxMessageDao) {

    fun messages(): Flow<List<InboxMessage>> = dao.observeLatest(MAX_MESSAGES)

    fun unreadCount(): Flow<Int> = dao.observeUnreadCount()

    /** Stores a received message once; a repeat of the same id changes nothing. */
    suspend fun save(message: InboxMessage) {
        if (dao.insert(message) != -1L) dao.prune(MAX_MESSAGES)
    }

    suspend fun markRead(id: String) = dao.markRead(id)

    suspend fun markAllRead() = dao.markAllRead()

    suspend fun delete(id: String) = dao.delete(id)

    suspend fun deleteAll() = dao.deleteAll()

    companion object {
        /** History kept on the phone. */
        const val MAX_MESSAGES = 500
    }
}
