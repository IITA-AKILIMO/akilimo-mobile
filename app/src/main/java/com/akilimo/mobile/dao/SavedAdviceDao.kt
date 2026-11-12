package com.akilimo.mobile.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.akilimo.mobile.entities.SavedAdvice
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedAdviceDao {

    @Query("SELECT * FROM saved_advice WHERE account_id = :accountId ORDER BY created_at DESC")
    fun observe(accountId: Long): Flow<List<SavedAdvice>>

    @Query("SELECT * FROM saved_advice WHERE request_id = :requestId AND account_id = :accountId")
    suspend fun find(accountId: Long, requestId: String): SavedAdvice?

    @Query("SELECT * FROM saved_advice WHERE request_id = :requestId AND account_id = :accountId")
    fun observeOne(accountId: Long, requestId: String): Flow<SavedAdvice?>

    @Upsert
    suspend fun upsert(advice: SavedAdvice)

    @Upsert
    suspend fun upsertAll(advice: List<SavedAdvice>)

    @Query("DELETE FROM saved_advice WHERE account_id = :accountId AND request_id = :requestId")
    suspend fun delete(accountId: Long, requestId: String)

    @Query("DELETE FROM saved_advice WHERE account_id = :accountId")
    suspend fun deleteForAccount(accountId: Long)

    @Query("DELETE FROM saved_advice")
    suspend fun deleteAll()
}
