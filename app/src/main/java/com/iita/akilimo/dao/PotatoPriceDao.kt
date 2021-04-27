package com.iita.akilimo.dao

import androidx.room.*
import com.iita.akilimo.entities.PotatoPrice

@Dao
interface PotatoPriceDao {

    @Query("SELECT * FROM potato_price")
    fun listAll(): List<PotatoPrice>

    @Query("SELECT * FROM potato_price LIMIT 1")
    fun findOne(): PotatoPrice?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(location: PotatoPrice)

    @Update
    fun update(location: PotatoPrice)

    @Delete
    fun delete(location: PotatoPrice?)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(priceList: List<PotatoPrice>)

    @Query("SELECT * FROM potato_price where country=:countryCode")
    fun findAllByCountry(countryCode: String): MutableList<PotatoPrice>
}
