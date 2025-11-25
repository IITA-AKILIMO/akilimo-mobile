package com.akilimo.mobile.repos

import com.akilimo.mobile.dao.MaizeMarketDao
import com.akilimo.mobile.entities.MaizeMarket
import com.akilimo.mobile.enums.EnumMaizeProduceType

class MaizeMarketRepo(private val dao: MaizeMarketDao) {

    suspend fun saveMarketEntry(entry: MaizeMarket) {
        dao.upsert(entry)
    }

    suspend fun getLastEntryForUser(userId: Int): MaizeMarket? {
        return dao.getLastEntryForUser(userId)
    }

    suspend fun getUserMarket(userId: Int, produceType: EnumMaizeProduceType): MaizeMarket? {
        return dao.getUserMarket(userId, produceType)
    }
}
