package com.akilimo.mobile.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.akilimo.mobile.entities.FertilizerPrice
import kotlinx.coroutines.flow.Flow

@Dao
interface FertilizerPriceDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(price: FertilizerPrice): Long

    @Update
    suspend fun update(price: FertilizerPrice)

    @Query("SELECT * FROM fertilizer_prices WHERE fertilizer_key = :key AND country_code = :country ORDER BY sort_order ASC")
    suspend fun getByKeyAndCountry(key: String, country: String): FertilizerPrice?

    @Query("SELECT * FROM fertilizer_prices WHERE fertilizer_key = :key ORDER BY sort_order ASC")
    suspend fun getByKey(key: String): List<FertilizerPrice>

    @Query("UPDATE selected_fertilizers SET fertilizer_price_id = NULL WHERE fertilizer_price_id NOT IN (:keepIds)")
    suspend fun detachStale(keepIds: List<Int>)

    @Query("DELETE FROM fertilizer_prices WHERE id NOT IN (:keepIds)")
    suspend fun deleteStale(keepIds: List<Int>)

    /** Removes options the API no longer serves; a saved pick keeps its stored price and just loses the link. */
    @Transaction
    suspend fun pruneStale(keepIds: List<Int>) {
        detachStale(keepIds)
        deleteStale(keepIds)
    }

    @Query("SELECT * FROM fertilizer_prices ORDER BY sort_order ASC")
    fun observeAll(): Flow<List<FertilizerPrice>>
}
