package com.akilimo.mobile.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.akilimo.mobile.entities.FertilizerPrice

@Dao
interface FertilizerPriceDao {

    @Query("SELECT * FROM fertilizer_prices")
    fun listAll(): List<FertilizerPrice>

    @Query("SELECT * FROM fertilizer_prices LIMIT 1")
    fun findOne(): FertilizerPrice?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg fieldYield: FertilizerPrice)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(fertilizerPricesList: List<FertilizerPrice>): LongArray?

    @Update
    fun update(vararg fieldYield: FertilizerPrice?)

    @Delete
    fun delete(fieldYield: FertilizerPrice?)

    @Query("SELECT * FROM fertilizer_prices where country_code=:countryCode")
    fun findAllByCountry(countryCode: String): List<FertilizerPrice>

    @Query("SELECT * FROM fertilizer_prices where fertilizer_key=:fertilizerKey ORDER BY sort_order ASC")
    fun findAllByFertilizerKey(fertilizerKey: String): List<FertilizerPrice>

    @Query("SELECT * FROM fertilizer_prices where price_id=:itemTagIndex LIMIT 1")
    fun findOneByPriceId(itemTagIndex: Long): FertilizerPrice

    @Query("DELETE FROM fertilizer_prices")
    fun deleteAll()
}
