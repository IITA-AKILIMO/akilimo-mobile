package com.iita.akilimo.dao

import androidx.room.*
import com.iita.akilimo.entities.CassavaMarket

@Dao
interface CassavaMarketDao {

    @Query("SELECT * FROM cassava_market")
    fun listAll(): List<CassavaMarket>

    @Query("SELECT * FROM cassava_market LIMIT 1")
    fun findOne(): CassavaMarket?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(markets: CassavaMarket)

    @Update
    fun update(markets: CassavaMarket)

    @Delete
    fun delete(market: CassavaMarket?)
}
