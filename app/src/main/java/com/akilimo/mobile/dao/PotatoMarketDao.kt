package com.akilimo.mobile.dao

import androidx.room.*
import com.akilimo.mobile.entities.PotatoMarket

@Dao
interface PotatoMarketDao {

    @Query("SELECT * FROM potato_markets")
    fun listAll(): List<PotatoMarket>

    @Query("SELECT * FROM potato_markets LIMIT 1")
    fun findOne(): PotatoMarket?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(location: PotatoMarket)

    @Update
    fun update(location: PotatoMarket)

    @Delete
    fun delete(location: PotatoMarket?)

    @Query("DELETE FROM potato_markets")
    fun deleteAll()
}
