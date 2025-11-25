package com.akilimo.mobile.repos

import com.akilimo.mobile.dao.MaizeMarketDao
import com.akilimo.mobile.entities.MaizeMarket
import com.akilimo.mobile.enums.EnumMaizeProduceType

class MaizeMarketRepo(private val dao: MaizeMarketDao) {

    suspend fun saveMarketEntry(entry: MaizeMarket) {
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


    suspend fun getLastEntryForUser(userId: Int): MaizeMarket? {
        return dao.getLastEntryForUser(userId)
    }

    suspend fun getUserMarket(userId: Int, produceType: EnumMaizeProduceType): MaizeMarket? {
        return dao.getUserMarket(userId, produceType)
    }
}
