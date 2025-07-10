package com.akilimo.mobile.dao

import androidx.room.Dao
import androidx.room.Query
import com.akilimo.mobile.entities.MaizePrice

@Dao
interface MaizePriceDao: BaseDao<MaizePrice> {

    @Query("SELECT * FROM maize_prices")
    fun findAll(): List<MaizePrice>

    @Query("SELECT * FROM maize_prices LIMIT 1")
    fun findOne(): MaizePrice?

    @Query("SELECT * FROM maize_prices where id=:id")
    fun findById(id: Int): MaizePrice


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
