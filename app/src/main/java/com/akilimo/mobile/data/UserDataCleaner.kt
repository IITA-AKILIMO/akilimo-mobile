package com.akilimo.mobile.data

import com.akilimo.mobile.dao.AppDatabase

class UserDataCleaner(
    private val database: AppDatabase,
) {

    fun clearUserRelatedData() {
        with(database) {

            profileInfoDao().deleteAll()
            mandatoryInfoDao().deleteAll()
            adviceStatusDao().deleteAll()
            cassavaMarketDao().deleteAll()
            cassavaPriceDao().deleteAll()
            currencyDao().deleteAll()
            currentPracticeDao().deleteAll()
            fertilizerDao().deleteAll()
            fertilizerPriceDao().deleteAll()
            fieldOperationCostDao().deleteAll()
            fieldYieldDao().deleteAll()
            investmentAmountDao().deleteAll()
            locationInfoDao().deleteAll()
            maizeMarketDao().deleteAll()
            maizePerformanceDao().deleteAll()
            maizePriceDao().deleteAll()
            potatoMarketDao().deleteAll()
            scheduleDateDao().deleteAll()
            starchFactoryDao().deleteAll()
            operationCostDao().deleteAll()
        }
    }
}