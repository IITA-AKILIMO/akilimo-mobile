package com.akilimo.mobile.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.akilimo.mobile.entities.CassavaMarket

@Dao
interface CassavaMarketDao {

    @Query("SELECT * FROM cassava_markets")
    fun listAll(): List<CassavaMarket>

    @Query("SELECT * FROM cassava_markets LIMIT 1")
    fun findOne(): CassavaMarket?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(markets: CassavaMarket)

    @Update
    fun update(markets: CassavaMarket)

    @Delete
    fun delete(market: CassavaMarket?)

    @Query("DELETE FROM cassava_markets")
    fun deleteAll()
}
