package com.akilimo.mobile.repos

import com.akilimo.mobile.dao.InvestmentAmountDao
import com.akilimo.mobile.entities.InvestmentAmount
import com.akilimo.mobile.enums.EnumCountry
import kotlinx.coroutines.flow.Flow

class InvestmentRepo(private val dao: InvestmentAmountDao) {
    fun observeAll(): Flow<List<InvestmentAmount>> {
        return dao.observeAll()
    }

    fun observeAllByCountry(countryCode: EnumCountry): Flow<List<InvestmentAmount>> {
        return dao.observeAllByCountry(countryCode)
    }


    /** Drops cached amounts missing from [current], the full list just fetched. */
    suspend fun pruneStale(current: List<InvestmentAmount>) {
        if (current.isNotEmpty()) dao.deleteStale(current.map { it.id })
    }

    suspend fun saveAll(investmentAmounts: List<InvestmentAmount>) {
        investmentAmounts.forEach { f ->
            val rowId = dao.insert(f)
            if (rowId == -1L) {
                dao.update(f)
            }
        }
    }
}