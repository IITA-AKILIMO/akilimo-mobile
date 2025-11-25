package com.akilimo.mobile.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.akilimo.mobile.entities.MaizeMarket
import com.akilimo.mobile.enums.EnumMaizeProduceType

@Dao
interface MaizeMarketDao {
    @Upsert
    fun upsert(maizeMarket: MaizeMarket)

    @Query("SELECT * FROM maize_markets WHERE user_id = :userId AND produce_type = :produceType LIMIT 1")
    fun getUserMarket(userId: Int, produceType: EnumMaizeProduceType): MaizeMarket?

    @Query("SELECT * FROM maize_markets WHERE user_id = :userId ORDER BY id DESC LIMIT 1")
    fun getLastEntryForUser(userId: Int): MaizeMarket?
}
