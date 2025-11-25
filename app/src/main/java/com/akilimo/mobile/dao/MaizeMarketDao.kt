package com.akilimo.mobile.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.akilimo.mobile.entities.MaizeMarket
import com.akilimo.mobile.enums.EnumMaizeProduceType
import com.akilimo.mobile.enums.EnumMarketType

@Dao
interface MaizeMarketDao {

    @Query("SELECT * FROM maize_markets WHERE user_id=:userId  and market_type=:marketType LIMIT 1")
    fun findOne(userId: Int, marketType: EnumMarketType): MaizeMarket?

    @Insert
    fun insert(maizeMarket: MaizeMarket): Long

    @Update
    fun update(maizeMarket: MaizeMarket)

    @Query("SELECT * FROM maize_markets WHERE user_id = :userId AND produce_type = :produceType LIMIT 1")
    fun getUserMarket(userId: Int, produceType: EnumMaizeProduceType): MaizeMarket?

    @Query("SELECT * FROM maize_markets WHERE user_id = :userId ORDER BY id DESC LIMIT 1")
    fun getLastEntryForUser(userId: Int): MaizeMarket?
}
