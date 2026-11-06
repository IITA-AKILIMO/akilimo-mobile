package com.akilimo.mobile.repos

import com.akilimo.mobile.dao.SelectedInvestmentDao
import com.akilimo.mobile.entities.SelectedInvestment
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SelectedInvestmentRepoTest {

    private val dao: SelectedInvestmentDao = mockk(relaxed = true)
    private val repo = SelectedInvestmentRepo(dao)

    @Test
    fun `saveOrUpdate looks up the existing row by userId and updates it`() = runTest {
        // Row id (7) deliberately differs from userId (1): the old code looked up by row id
        val existing = SelectedInvestment(id = 7, userId = 1, investmentId = 3, chosenAmount = 100.0)
        coEvery { dao.getSelectedByUser(1) } returns existing

        repo.saveOrUpdate(SelectedInvestment(userId = 1, investmentId = 4, chosenAmount = 250.0))

        coVerify { dao.getSelectedByUser(1) }
        coVerify {
            dao.update(match { it.id == 7 && it.userId == 1 && it.investmentId == 4 && it.chosenAmount == 250.0 })
        }
        coVerify(exactly = 0) { dao.insert(any()) }
    }

    @Test
    fun `saveOrUpdate inserts when the user has no existing selection`() = runTest {
        coEvery { dao.getSelectedByUser(1) } returns null

        repo.saveOrUpdate(SelectedInvestment(userId = 1, investmentId = 4, chosenAmount = 250.0))

        coVerify { dao.insert(match { it.userId == 1 && it.investmentId == 4 }) }
        coVerify(exactly = 0) { dao.update(any()) }
    }
}
