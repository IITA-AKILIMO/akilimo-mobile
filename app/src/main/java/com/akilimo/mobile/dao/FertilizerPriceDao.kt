package com.akilimo.mobile.dao

import androidx.room.*
import com.akilimo.mobile.entities.FertilizerPrice

@Dao
interface FertilizerPriceDao {

    @Query("SELECT * FROM fertilizer_price")
    fun listAll(): List<FertilizerPrice>

    @Query("SELECT * FROM fertilizer_price LIMIT 1")
    fun findOne(): FertilizerPrice?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg fieldYield: FertilizerPrice)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(fertilizerPricesList: List<FertilizerPrice>): LongArray?

    @Update
    fun update(vararg fieldYield: FertilizerPrice?)

    @Delete
    fun delete(fieldYield: FertilizerPrice?)

    @Query("SELECT * FROM fertilizer_price where country=:countryCode")
    fun findAllByCountry(countryCode: String): List<FertilizerPrice>

    @Query("SELECT * FROM fertilizer_price where fertilizerKey=:fertKey ORDER BY sortOrder ASC")
    fun findAllByFertilizerKey(fertKey: String): List<FertilizerPrice>

    @Query("SELECT * FROM fertilizer_price where priceId=:itemTagIndex LIMIT 1")
    fun findOneByPriceId(itemTagIndex: Long): FertilizerPrice

    @Query("DELETE FROM fertilizer_price")
    fun deleteAll()
}
