package com.akilimo.mobile.repos

import com.akilimo.mobile.dao.CassavaMarketPriceDao
import com.akilimo.mobile.entities.CassavaMarketPrice
import com.akilimo.mobile.enums.EnumCountry
import kotlinx.coroutines.flow.Flow

class CassavaMarketPriceRepo(
    private val dao: CassavaMarketPriceDao,
) {

    suspend fun saveAll(marketPrices: List<CassavaMarketPrice>) {
        marketPrices.forEach { f ->
            val rowId = dao.insert(f)
            if (rowId == -1L) {
                dao.update(f) // record exists, update in place
            }
        }
    }

    fun getPricesByCountry(countryCode: EnumCountry): List<CassavaMarketPrice> {
        return dao.getPricesByCountry(countryCode)
    }


    fun observeAll(): Flow<List<CassavaMarketPrice>> = dao.observeAll()
    fun observeByCountry(countryCode: EnumCountry): Flow<List<CassavaMarketPrice>> =
        dao.observeByCountry(countryCode)
}
