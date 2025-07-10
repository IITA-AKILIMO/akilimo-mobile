package com.akilimo.mobile.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.akilimo.mobile.entities.MaizeMarket

@Dao
interface MaizeMarketDao: BaseDao<MaizeMarket> {

    @Query("SELECT * FROM maize_market")
    fun listAll(): List<MaizeMarket>

    @Query("SELECT * FROM maize_market LIMIT 1")
    fun findOne(): MaizeMarket?
}
