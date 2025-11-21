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
    fun getPricesByCountry(countryCode: EnumCountry): List<CassavaMarketPrice>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(price: CassavaMarketPrice): Long

    @Update
    fun update(price: CassavaMarketPrice)
}