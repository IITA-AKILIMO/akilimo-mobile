package com.akilimo.mobile.data

import com.akilimo.mobile.dao.AppDatabase
import com.akilimo.mobile.utils.SessionManager

class UserDataCleaner(
    private val database: AppDatabase,
    private val sessionManager: SessionManager
) {

    fun clearUserRelatedData() {
        with(database) {
            if (!sessionManager.getRememberUserInfo()) {
                profileInfoDao().deleteAll()
            }
            if (!sessionManager.getRememberAreaUnit()) {
                mandatoryInfoDao().deleteAll()
            }

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