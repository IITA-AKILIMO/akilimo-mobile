package com.akilimo.mobile.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.akilimo.mobile.entities.InboxMessage
import kotlinx.coroutines.flow.Flow

@Dao
interface InboxMessageDao {

    @Query("SELECT * FROM inbox_messages ORDER BY received_at DESC, id DESC LIMIT :limit")
    fun observeLatest(limit: Int): Flow<List<InboxMessage>>

    @Query("SELECT COUNT(*) FROM inbox_messages WHERE is_read = 0")
    fun observeUnreadCount(): Flow<Int>

    /** Keeps the stored copy (and its read state) when the same id arrives again. Returns -1 for a duplicate. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(message: InboxMessage): Long

    @Query("UPDATE inbox_messages SET is_read = 1 WHERE id = :id")
    suspend fun markRead(id: String)

    @Query("UPDATE inbox_messages SET is_read = 1 WHERE is_read = 0")
    suspend fun markAllRead()

    @Query("DELETE FROM inbox_messages WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM inbox_messages")
    suspend fun deleteAll()

    /** Drops everything but the newest [keep] messages so the history cannot grow without bound. */
    @Query(
        "DELETE FROM inbox_messages WHERE id NOT IN " +
            "(SELECT id FROM inbox_messages ORDER BY received_at DESC, id DESC LIMIT :keep)"
    )
    suspend fun prune(keep: Int)
}
