package com.akilimo.mobile.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.akilimo.mobile.entities.ProduceMarket
import com.akilimo.mobile.enums.EnumMarketType
import com.akilimo.mobile.enums.EnumProduceType

@Dao
interface ProduceMarketDao {

    @Query("SELECT * FROM produce_markets WHERE user_id=:userId  and market_type=:marketType LIMIT 1")
    suspend fun findOne(userId: Int, marketType: EnumMarketType): ProduceMarket?

    @Insert
    suspend fun insert(produceMarket: ProduceMarket): Long

    @Update
    suspend fun update(produceMarket: ProduceMarket)

    @Query("SELECT * FROM produce_markets WHERE user_id = :userId AND produce_type = :produceType LIMIT 1")
    suspend fun getUserMarket(userId: Int, produceType: EnumProduceType): ProduceMarket?

    @Query("SELECT * FROM produce_markets WHERE user_id = :userId and market_type =:marketType ORDER BY id DESC LIMIT 1")
    suspend fun getLastEntryForUser(userId: Int,marketType: EnumMarketType): ProduceMarket?
}

class ProduceMarketRepo(private val dao: ProduceMarketDao) {

    suspend fun saveMarketEntry(entry: ProduceMarket) {
        val existing = dao.findOne(entry.userId, entry.marketType)
        if (existing != null) {
            val updated = existing.copy(
                marketType = entry.marketType,
                produceType = entry.produceType,
                unitPrice = entry.unitPrice,
                unitOfSale = entry.unitOfSale,
            ).apply {
                createdAt = existing.createdAt
                updatedAt = System.currentTimeMillis()
            }
            dao.update(updated)
        } else {
            dao.insert(entry)
        }
    }


    suspend fun getLastEntryForUser(userId: Int,marketType: EnumMarketType): ProduceMarket? {
        return dao.getLastEntryForUser(userId,marketType)
    }

    suspend fun getUserMarket(userId: Int, produceType: EnumProduceType): ProduceMarket? {
        return dao.getUserMarket(userId, produceType)
    }
}