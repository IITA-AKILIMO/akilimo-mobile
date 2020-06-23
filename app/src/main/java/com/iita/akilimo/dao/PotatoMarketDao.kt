package com.iita.akilimo.dao

import androidx.room.*
import com.iita.akilimo.entities.MaizeMarket
import com.iita.akilimo.entities.PotatoMarket

@Dao
interface PotatoMarketDao {

    @Query("SELECT * FROM potato_market")
    fun listAll(): List<PotatoMarket>

    @Query("SELECT * FROM potato_market LIMIT 1")
    fun findOne(): PotatoMarket?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(location: PotatoMarket)

    @Update
    fun update(location: PotatoMarket)

    @Delete
    fun delete(location: PotatoMarket?)
}