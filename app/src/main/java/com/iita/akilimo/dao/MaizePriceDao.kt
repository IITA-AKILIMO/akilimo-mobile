package com.iita.akilimo.dao

import androidx.room.*
import com.iita.akilimo.entities.MaizePrice

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
    fun update(location: MaizePrice)

    @Delete
    fun delete(location: MaizePrice?)

    @Query("SELECT * FROM maize_price where country=:countryCode")
    fun findAllByCountry(countryCode: String): MutableList<MaizePrice>
}