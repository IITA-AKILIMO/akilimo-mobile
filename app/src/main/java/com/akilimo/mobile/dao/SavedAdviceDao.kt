package com.akilimo.mobile.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.akilimo.mobile.entities.SavedAdvice
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedAdviceDao {

    /** One screenful of the list, newest first, optionally narrowed to a use case and a country. Grows with [limit]. */
    @Query(
        "SELECT * FROM saved_advice WHERE account_id = :accountId " +
            "AND (:useCase IS NULL OR use_case = :useCase) AND (:country IS NULL OR country_code = :country) " +
            "ORDER BY created_at DESC, request_id DESC LIMIT :limit"
    )
    fun observePage(accountId: Long, useCase: String?, country: String?, limit: Int): Flow<List<SavedAdvice>>

    /** How many items match the same filter (the list itself only holds the first [observePage] rows). */
    @Query(
        "SELECT COUNT(*) FROM saved_advice WHERE account_id = :accountId " +
            "AND (:useCase IS NULL OR use_case = :useCase) AND (:country IS NULL OR country_code = :country)"
    )
    fun observeCount(accountId: Long, useCase: String?, country: String?): Flow<Int>

    @Query("SELECT DISTINCT use_case FROM saved_advice WHERE account_id = :accountId AND use_case IS NOT NULL ORDER BY use_case")
    fun observeUseCases(accountId: Long): Flow<List<String>>

    @Query("SELECT DISTINCT country_code FROM saved_advice WHERE account_id = :accountId AND country_code IS NOT NULL ORDER BY country_code")
    fun observeCountries(accountId: Long): Flow<List<String>>

    @Query("SELECT * FROM saved_advice WHERE request_id = :requestId AND account_id = :accountId")
    suspend fun find(accountId: Long, requestId: String): SavedAdvice?

    @Query("SELECT * FROM saved_advice WHERE request_id = :requestId AND account_id = :accountId")
    fun observeOne(accountId: Long, requestId: String): Flow<SavedAdvice?>

    /** The items among [requestIds] that are already on the phone (a page of ids at a time, well under SQLite's variable limit). */
    @Query("SELECT * FROM saved_advice WHERE account_id = :accountId AND request_id IN (:requestIds)")
    suspend fun findMany(accountId: Long, requestIds: List<String>): List<SavedAdvice>

    @Query("SELECT request_id FROM saved_advice WHERE account_id = :accountId")
    suspend fun allIds(accountId: Long): List<String>

    @Upsert
    suspend fun upsert(advice: SavedAdvice)

    @Upsert
    suspend fun upsertAll(advice: List<SavedAdvice>)

    @Query("DELETE FROM saved_advice WHERE account_id = :accountId AND request_id = :requestId")
    suspend fun delete(accountId: Long, requestId: String)

    /** Removes several items; keep [requestIds] to a few hundred at a time (SQLite allows 999 variables). */
    @Query("DELETE FROM saved_advice WHERE account_id = :accountId AND request_id IN (:requestIds)")
    suspend fun deleteMany(accountId: Long, requestIds: List<String>)

    @Query("DELETE FROM saved_advice WHERE account_id = :accountId")
    suspend fun deleteForAccount(accountId: Long)

    @Query("DELETE FROM saved_advice")
    suspend fun deleteAll()
}
