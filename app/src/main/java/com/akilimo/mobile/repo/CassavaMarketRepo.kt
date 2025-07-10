package com.akilimo.mobile.repo

import com.akilimo.mobile.dao.AppDatabase
import com.akilimo.mobile.entities.CassavaMarket
import com.akilimo.mobile.entities.CassavaPrice
import com.akilimo.mobile.entities.StarchFactory
import com.akilimo.mobile.interfaces.AkilimoApi
import com.akilimo.mobile.interfaces.AkilimoService

class CassavaMarketRepo(
    private val db: AppDatabase,
    private val api: AkilimoService = AkilimoApi.apiService,
) {
    fun getSavedCassavaMarket() = db.cassavaMarketDao().findOne()
    fun getProfileInfo() = db.profileInfoDao().findOne()
    fun getScheduledDate() = db.scheduleDateDao().findOne()

    suspend fun fetchStarchFactories(countryCode: String) =
        api.getStarchFactories(countryCode).execute().body()?.data ?: emptyList()

    suspend fun fetchCassavaPrices(countryCode: String) =
        api.getCassavaPrices(countryCode).execute().body()?.data ?: emptyList()

    suspend fun saveStarchFactories(factories: List<StarchFactory>) =
        db.starchFactoryDao().insertAll(factories)

    suspend fun saveCassavaPrices(prices: List<CassavaPrice>) =
        db.cassavaPriceDao().insertAll(prices)

    suspend fun saveCassavaMarket(market: CassavaMarket) =
        db.cassavaMarketDao().insert(market)
}