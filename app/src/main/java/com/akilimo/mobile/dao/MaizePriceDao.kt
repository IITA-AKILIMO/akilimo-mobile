package com.akilimo.mobile.dao

import androidx.room.*
import com.akilimo.mobile.entities.MaizePrice

@Dao
interface MaizePriceDao {

    @Query("SELECT * FROM maize_price")
    fun listAll(): List<MaizePrice>

    @Query("SELECT * FROM maize_price LIMIT 1")
    fun findOne(): MaizePrice?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(location: MaizePrice)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(maizePriceList: List<MaizePrice>)

    @Update
    fun update(maizePrice: MaizePrice)

    @Delete
    fun delete(maizePrice: MaizePrice?)

    @Query("SELECT * FROM maize_price where country=:countryCode")
    fun findAllByCountry(countryCode: String): MutableList<MaizePrice>

    @Query("SELECT * FROM maize_price where country=:countryCode and produceType=:produceType")
    fun findAllByCountryAndProduceType(
        countryCode: String,
        produceType: String
    ): MutableList<MaizePrice>

    @Query("SELECT * FROM maize_price where priceIndex=:itemTagIndex")
    fun findPriceByPriceIndex(itemTagIndex: Int): MaizePrice

    @Query("DELETE FROM maize_price")
    fun deleteAll()
}
