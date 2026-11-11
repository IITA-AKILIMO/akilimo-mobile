package com.akilimo.mobile.repos

import com.akilimo.mobile.dao.CassavaMarketPriceDao
import com.akilimo.mobile.dao.FertilizerPriceDao
import com.akilimo.mobile.dao.InvestmentAmountDao
import com.akilimo.mobile.entities.CassavaMarketPrice
import com.akilimo.mobile.entities.FertilizerPrice
import com.akilimo.mobile.entities.InvestmentAmount
import com.akilimo.mobile.enums.EnumCountry
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * The price service regenerates its option lists with new ids (catalogue id * 10 + step), so
 * after a full sync the repos drop the cached rows the server no longer serves.
 */
class PriceListPruneTest {

    private fun fertilizerPrice(id: Int) = FertilizerPrice(
        id = id, fertilizerKey = "UREA_NG", fertilizerCountry = "NG", countryCode = "NG",
        currencyCode = "NGN", currencySymbol = "N", minAllowedPrice = 0.0, maxAllowedPrice = 0.0,
        priceRange = "", description = null
    )

    private fun cassavaPrice(id: Int) = CassavaMarketPrice(
        id = id, countryCode = EnumCountry.NG, currencyCode = "NGN", currencySymbol = "N",
        minLocalPrice = 1.0, maxLocalPrice = 2.0, averagePrice = 1.5, exactPrice = false,
        itemTag = "cassUP", minAllowedPrice = 0.0, maxAllowedPrice = 0.0, active = true, sortOrder = 0
    )

    private fun investment(id: Int) = InvestmentAmount(
        id = id, investmentAmount = 1.0, countryCode = "NG", exactAmount = false,
        active = true, areaUnit = "acre", sortOrder = 0, currencySymbol = "N", currencyCode = "NGN"
    )

    @Test
    fun `fertilizer prices keep only the ids just fetched`() = runTest {
        val dao = mockk<FertilizerPriceDao>(relaxed = true)
        FertilizerPriceRepo(dao).pruneStale(listOf(fertilizerPrice(101), fertilizerPrice(1000050)))
        coVerify { dao.deleteStale(listOf(101, 1000050)) }
    }

    @Test
    fun `cassava prices keep only the ids just fetched`() = runTest {
        val dao = mockk<CassavaMarketPriceDao>(relaxed = true)
        CassavaMarketPriceRepo(dao).pruneStale(listOf(cassavaPrice(11), cassavaPrice(12)))
        coVerify { dao.deleteStale(listOf(11, 12)) }
    }

    @Test
    fun `investment amounts keep only the ids just fetched`() = runTest {
        val dao = mockk<InvestmentAmountDao>(relaxed = true)
        InvestmentRepo(dao).pruneStale(listOf(investment(21)))
        coVerify { dao.deleteStale(listOf(21)) }
    }

    @Test
    fun `an empty fetch never wipes the cache`() = runTest {
        val fertilizerDao = mockk<FertilizerPriceDao>(relaxed = true)
        val cassavaDao = mockk<CassavaMarketPriceDao>(relaxed = true)
        val investmentDao = mockk<InvestmentAmountDao>(relaxed = true)
        FertilizerPriceRepo(fertilizerDao).pruneStale(emptyList())
        CassavaMarketPriceRepo(cassavaDao).pruneStale(emptyList())
        InvestmentRepo(investmentDao).pruneStale(emptyList())
        coVerify(exactly = 0) { fertilizerDao.deleteStale(any()) }
        coVerify(exactly = 0) { cassavaDao.deleteStale(any()) }
        coVerify(exactly = 0) { investmentDao.deleteStale(any()) }
    }
}

