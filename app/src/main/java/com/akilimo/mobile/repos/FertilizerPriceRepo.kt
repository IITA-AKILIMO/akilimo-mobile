package com.akilimo.mobile.repos

import com.akilimo.mobile.dao.FertilizerPriceDao
import com.akilimo.mobile.entities.FertilizerPrice

class FertilizerPriceRepo(private val dao: FertilizerPriceDao) {

    suspend fun save(price: FertilizerPrice) {
        dao.insert(price)
    }

    suspend fun saveAll(prices: List<FertilizerPrice>) {
        prices.forEach { f ->
            val rowId = dao.insert(f)
            if (rowId == -1L) {
                dao.update(f) // record exists, update in place
            }
        }
    }

    /** Drops cached options missing from [current], the full list just fetched. */
    suspend fun pruneStale(current: List<FertilizerPrice>) {
        if (current.isNotEmpty()) dao.pruneStale(current.map { it.id })
    }

    suspend fun getByFertilizerKey(key: String): List<FertilizerPrice> {
        return dao.getByKey(key)
    }

}
