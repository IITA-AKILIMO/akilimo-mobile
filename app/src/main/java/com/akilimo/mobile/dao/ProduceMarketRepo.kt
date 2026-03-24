package com.akilimo.mobile.dao

import com.akilimo.mobile.entities.ProduceMarket
import com.akilimo.mobile.enums.EnumMarketType
import com.akilimo.mobile.enums.EnumProduceType

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