package com.akilimo.mobile.dao

import androidx.room.*
import com.akilimo.mobile.entities.MaizePrice

@Dao
interface MaizePriceDao {

    @Query("SELECT * FROM maize_prices")
    fun findAll(): List<MaizePrice>

    @Query("SELECT * FROM maize_prices LIMIT 1")
    fun findOne(): MaizePrice?

    @Query("SELECT * FROM maize_prices where id=:id")
    fun findById(id: Int): MaizePrice

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(location: MaizePrice)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(maizePriceList: List<MaizePrice>)

    @Update
    fun update(maizePrice: MaizePrice)

    @Delete
    fun delete(maizePrice: MaizePrice?)

    @Query("SELECT * FROM maize_prices where country_code=:countryCode")
    fun findAllByCountry(countryCode: String): MutableList<MaizePrice>

    @Query("SELECT * FROM maize_prices where country_code=:countryCode and produce_type=:produceType")
    fun findAllByCountryAndProduceType(
        countryCode: String,
        produceType: String
    ): MutableList<MaizePrice>



    @Query("DELETE FROM maize_prices")
    fun deleteAll()
}
