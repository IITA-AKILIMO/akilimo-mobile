package com.akilimo.mobile.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.akilimo.mobile.entities.CassavaMarketPrice
import kotlinx.coroutines.flow.Flow

@Dao
interface CassavaPriceDao {

    @Query("SELECT * FROM cassava_prices")
    fun observeAll(): Flow<List<CassavaMarketPrice>>

    @Query("SELECT * FROM cassava_prices where country_code = :countryCode")
    fun observeByCountry(countryCode: String): Flow<List<CassavaMarketPrice>>


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(price: CassavaMarketPrice): Long

    @Update
    fun update(price: CassavaMarketPrice)

}
