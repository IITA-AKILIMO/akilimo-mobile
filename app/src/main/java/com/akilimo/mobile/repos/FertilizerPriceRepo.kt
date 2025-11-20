package com.akilimo.mobile.repos

import com.akilimo.mobile.dao.FertilizerPriceDao
import com.akilimo.mobile.entities.FertilizerPrice
import kotlinx.coroutines.flow.Flow

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

    suspend fun updatePrice(price: FertilizerPrice) {
        dao.update(price)
    }

    suspend fun getPrice(key: String, country: String): FertilizerPrice? {
        return dao.getByKeyAndCountry(key, country)
    }

    suspend fun getByFertilizerKey(key: String): List<FertilizerPrice> {
        return dao.getByKey(key)
    }

    fun observePrices(): Flow<List<FertilizerPrice>> {
        return dao.observeAll()
    }
}
