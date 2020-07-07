package com.iita.akilimo.dao

import androidx.room.*
import com.iita.akilimo.entities.MaizeMarket

@Dao
interface MaizeMarketDao {

    @Query("SELECT * FROM maize_market")
    fun listAll(): List<MaizeMarket>

    @Query("SELECT * FROM maize_market LIMIT 1")
    fun findOne(): MaizeMarket?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(location: MaizeMarket)

    @Update
    fun update(location: MaizeMarket)

    @Delete
    fun delete(location: MaizeMarket?)
}