package com.akilimo.mobile.repos

import com.akilimo.mobile.dao.InvestmentAmountDao
import com.akilimo.mobile.entities.InvestmentAmount
import kotlinx.coroutines.flow.Flow

class InvestmentRepo(private val dao: InvestmentAmountDao) {
    fun observeAll(): Flow<List<InvestmentAmount>> {
        return dao.observeAll()
    }

    fun observeAllByCountry(countryCode: String): Flow<List<InvestmentAmount>> {
        return dao.observeAllByCountry(countryCode)
    }


    fun saveAll(investmentAmounts: List<InvestmentAmount>) {
        investmentAmounts.forEach { f ->
            val rowId = dao.insert(f)
            if (rowId == -1L) {
                dao.update(f)
            }
        }
    }
}