package com.akilimo.mobile.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.akilimo.mobile.entities.PotatoPrice

@Dao
interface PotatoPriceDao {

    @Query("SELECT * FROM potato_prices")
    fun findAll(): List<PotatoPrice>

    @Query("SELECT * FROM potato_prices LIMIT 1")
    fun findOne(): PotatoPrice?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(location: PotatoPrice)

    @Update
    fun update(location: PotatoPrice)

    @Delete
    fun delete(location: PotatoPrice?)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(priceList: List<PotatoPrice>)

    @Query("SELECT * FROM potato_prices where country_code=:countryCode")
    fun findAllByCountry(countryCode: String): MutableList<PotatoPrice>
}
