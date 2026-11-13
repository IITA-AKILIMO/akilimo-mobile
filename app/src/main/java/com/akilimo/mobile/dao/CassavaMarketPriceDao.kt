package com.akilimo.mobile.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.akilimo.mobile.entities.CassavaMarketPrice
import com.akilimo.mobile.enums.EnumCountry
import kotlinx.coroutines.flow.Flow

@Dao
interface CassavaMarketPriceDao {
    @Query("SELECT * FROM cassava_market_prices")
    fun observeAll(): Flow<List<CassavaMarketPrice>>

    @Query("SELECT * FROM cassava_market_prices where country_code = :countryCode")
    fun observeByCountry(countryCode: EnumCountry): Flow<List<CassavaMarketPrice>>

    @Query("SELECT * FROM cassava_market_prices WHERE country_code = :countryCode")
    suspend fun getPricesByCountry(countryCode: EnumCountry): List<CassavaMarketPrice>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(price: CassavaMarketPrice): Long

    @Query("UPDATE selected_cassava_markets SET market_price_id = NULL WHERE market_price_id NOT IN (:keepIds)")
    suspend fun detachStale(keepIds: List<Int>)

    @Query("DELETE FROM cassava_market_prices WHERE id NOT IN (:keepIds)")
    suspend fun deleteStale(keepIds: List<Int>)

    /** Removes bands the API no longer serves; a saved pick keeps its stored price and just loses the link. */
    @Transaction
    suspend fun pruneStale(keepIds: List<Int>) {
        detachStale(keepIds)
        deleteStale(keepIds)
    }

    @Update
    suspend fun update(price: CassavaMarketPrice)
}