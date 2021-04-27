package com.iita.akilimo.dao

import androidx.room.*
import com.iita.akilimo.entities.CassavaPrice

@Dao
interface CassavaPriceDao {

    @Query("SELECT * FROM cassava_price")
    fun listAll(): List<CassavaPrice>

    @Query("SELECT * FROM cassava_price LIMIT 1")
    fun findOne(): CassavaPrice?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg prices: CassavaPrice)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(cassavaPriceList: List<CassavaPrice>)

    @Update
    fun update(vararg price: CassavaPrice)

    @Delete
    fun delete(market: CassavaPrice?)

    @Query("SELECT * FROM cassava_price where country=:countryCode")
    fun findAllByCountry(countryCode: String): List<CassavaPrice>
}
