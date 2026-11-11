package com.akilimo.mobile.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
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

    /** Removes bands the API no longer serves, keeping any a farmer's saved answer still points at. */
    @Query(
        "DELETE FROM cassava_market_prices WHERE id NOT IN (:keepIds) AND id NOT IN " +
            "(SELECT market_price_id FROM selected_cassava_markets WHERE market_price_id IS NOT NULL)"
    )
    suspend fun deleteStale(keepIds: List<Int>): Int

    @Update
    suspend fun update(price: CassavaMarketPrice)
}