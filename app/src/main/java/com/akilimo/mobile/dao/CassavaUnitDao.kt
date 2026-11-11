package com.akilimo.mobile.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.akilimo.mobile.entities.CassavaUnit
import kotlinx.coroutines.flow.Flow

@Dao
interface CassavaUnitDao {

    @Query("SELECT * FROM cassava_units ORDER BY sort_order ASC")
    fun observeAll(): Flow<List<CassavaUnit>>

    @Query("SELECT * FROM cassava_units WHERE id = :unitId LIMIT 1")
    suspend fun getById(unitId: Int): CassavaUnit?

    @Query("SELECT * FROM cassava_units ORDER BY sort_order ASC")
    suspend fun getAll(): List<CassavaUnit>
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(unit: CassavaUnit): Long

    @Update
    suspend fun update(unit: CassavaUnit)

    @Query("DELETE FROM cassava_units")
    suspend fun clear()

    @Query("UPDATE selected_cassava_markets SET cassava_unit_id = NULL WHERE cassava_unit_id NOT IN (:keepIds)")
    suspend fun detachStale(keepIds: List<Int>)

    @Query("DELETE FROM cassava_units WHERE id NOT IN (:keepIds)")
    suspend fun deleteStale(keepIds: List<Int>)

    /** Removes units the API no longer serves; a saved pick keeps its stored price and just loses the link. */
    @Transaction
    suspend fun pruneStale(keepIds: List<Int>) {
        detachStale(keepIds)
        deleteStale(keepIds)
    }
}
