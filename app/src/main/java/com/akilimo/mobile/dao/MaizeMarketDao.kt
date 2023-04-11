package com.akilimo.mobile.dao

import androidx.room.*
import com.akilimo.mobile.entities.MaizeMarket

@Dao
interface MaizeMarketDao {

    @Query("SELECT * FROM maize_market")
    fun listAll(): List<MaizeMarket>

    @Query("SELECT * FROM maize_market LIMIT 1")
    fun findOne(): MaizeMarket?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(maizeMarket: MaizeMarket)

    @Update
    fun update(maizeMarket: MaizeMarket)

    @Delete
    fun delete(maizeMarket: MaizeMarket?)
    @Query("DELETE FROM maize_market")
    fun deleteAll()
}
