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

    @Update
    fun update(vararg price: CassavaPrice)

    @Delete
    fun delete(market: CassavaPrice?)
}