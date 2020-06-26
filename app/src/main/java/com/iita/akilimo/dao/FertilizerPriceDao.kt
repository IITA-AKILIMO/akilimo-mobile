package com.iita.akilimo.dao

import androidx.room.*
import com.iita.akilimo.entities.FertilizerPrice

@Dao
interface FertilizerPriceDao {

    @Query("SELECT * FROM fertilizer_price")
    fun listAll(): List<FertilizerPrice>

    @Query("SELECT * FROM fertilizer_price LIMIT 1")
    fun findOne(): FertilizerPrice?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(vararg fieldYield: FertilizerPrice)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(fertilizerPricesList: List<FertilizerPrice>):LongArray?

    @Update
    fun update(vararg fieldYield: FertilizerPrice?)

    @Delete
    fun delete(fieldYield: FertilizerPrice?)

    @Query("SELECT * FROM fertilizer_price where country=:countryCode")
    fun findAllByCountry(countryCode: String): List<FertilizerPrice>
}