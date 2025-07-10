package com.akilimo.mobile.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.akilimo.mobile.entities.PotatoMarket

@Dao
interface PotatoMarketDao : BaseDao<PotatoMarket>{

    @Query("SELECT * FROM potato_markets")
    fun listAll(): List<PotatoMarket>

    @Query("SELECT * FROM potato_markets LIMIT 1")
    fun findOne(): PotatoMarket?
}
