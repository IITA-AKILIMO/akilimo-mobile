package com.akilimo.mobile.dao

import androidx.room.Dao
import androidx.room.Query
import com.akilimo.mobile.entities.CassavaMarket

@Dao
interface CassavaMarketDao: BaseDao<CassavaMarket> {

    @Query("SELECT * FROM cassava_markets")
    fun listAll(): List<CassavaMarket>

    @Query("SELECT * FROM cassava_markets LIMIT 1")
    fun findOne(): CassavaMarket?
}
