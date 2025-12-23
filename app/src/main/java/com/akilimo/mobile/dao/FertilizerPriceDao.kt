package com.akilimo.mobile.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.akilimo.mobile.entities.FertilizerPrice
import kotlinx.coroutines.flow.Flow

@Dao
interface FertilizerPriceDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(price: FertilizerPrice): Long

    @Update
    fun update(price: FertilizerPrice)

    @Query("SELECT * FROM fertilizer_prices WHERE fertilizer_key = :key AND country_code = :country ORDER BY sort_order ASC")
    fun getByKeyAndCountry(key: String, country: String): FertilizerPrice?

    @Query("SELECT * FROM fertilizer_prices WHERE fertilizer_key = :key ORDER BY sort_order ASC")
    fun getByKey(key: String): List<FertilizerPrice>

    @Query("SELECT * FROM fertilizer_prices ORDER BY sort_order ASC")
    fun observeAll(): Flow<List<FertilizerPrice>>
}
