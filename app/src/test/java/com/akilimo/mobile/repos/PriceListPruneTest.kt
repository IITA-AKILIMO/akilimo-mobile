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
        currencyCode = "NGN", currencySymbol = "N",
        priceRange = "", description = null
    )

    private fun cassavaPrice(id: Int) = CassavaMarketPrice(
        id = id, countryCode = EnumCountry.NG, currencyCode = "NGN", currencySymbol = "N",
        averagePrice = 1.5, exactPrice = false,
        itemTag = "cassUP", active = true, sortOrder = 0
    )

    private fun investment(id: Int) = InvestmentAmount(
        id = id, investmentAmount = 1.0, countryCode = "NG", exactAmount = false,
        active = true, areaUnit = "acre", sortOrder = 0, currencySymbol = "N", currencyCode = "NGN"
    )

    @Test
    fun `fertilizer prices keep only the ids just fetched`() = runTest {
        val dao = mockk<FertilizerPriceDao>(relaxed = true)
        FertilizerPriceRepo(dao).pruneStale(listOf(fertilizerPrice(101), fertilizerPrice(1000050)))
        coVerify { dao.pruneStale(listOf(101, 1000050)) }
    }

    @Test
    fun `cassava prices keep only the ids just fetched`() = runTest {
        val dao = mockk<CassavaMarketPriceDao>(relaxed = true)
        CassavaMarketPriceRepo(dao).pruneStale(listOf(cassavaPrice(11), cassavaPrice(12)))
        coVerify { dao.pruneStale(listOf(11, 12)) }
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
        coVerify(exactly = 0) { fertilizerDao.pruneStale(any()) }
        coVerify(exactly = 0) { cassavaDao.pruneStale(any()) }
        coVerify(exactly = 0) { investmentDao.deleteStale(any()) }
    }
}

