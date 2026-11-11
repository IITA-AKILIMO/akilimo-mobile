package com.akilimo.mobile.ui.viewmodels

import com.akilimo.mobile.data.AppSettingsDataStore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import com.akilimo.mobile.entities.AkilimoUser
import com.akilimo.mobile.entities.CassavaUnit
import com.akilimo.mobile.entities.SelectedCassavaMarket
import com.akilimo.mobile.entities.StarchFactory
import com.akilimo.mobile.entities.relations.SelectedCassavaMarketWithDetails
import com.akilimo.mobile.enums.EnumCassavaProduceType
import com.akilimo.mobile.enums.EnumUnitOfSale
import com.akilimo.mobile.repos.AkilimoUserRepo
import com.akilimo.mobile.repos.CassavaMarketPriceRepo
import com.akilimo.mobile.repos.CassavaUnitRepo
import com.akilimo.mobile.repos.SelectedCassavaMarketRepo
import com.akilimo.mobile.repos.StarchFactoryRepo
import com.akilimo.mobile.rules.TestDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import com.akilimo.mobile.workers.DataRefresher
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Assert.assertFalse
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CassavaMarketViewModelTest {

    @get:Rule
    val dispatcherRule = TestDispatcherRule()

    private val userRepo: AkilimoUserRepo = mockk(relaxed = true)
    private val factoryRepo: StarchFactoryRepo = mockk(relaxed = true)
    private val selectedRepo: SelectedCassavaMarketRepo = mockk(relaxed = true)
    private val priceRepo: CassavaMarketPriceRepo = mockk(relaxed = true)
    private val cassavaUnitRepo: CassavaUnitRepo = mockk(relaxed = true)
    private val appSettings: AppSettingsDataStore = mockk(relaxed = true)
    private val dataRefresher: DataRefresher = mockk(relaxed = true)

    private lateinit var viewModel: CassavaMarketViewModel

    private val factory1 = StarchFactory(id = 1, name = "Factory A", label = "Factory A")
    private val factory2 = StarchFactory(id = 2, name = "Factory B", label = "Factory B")

    @Before
    fun setUp() {
        coEvery { userRepo.getUser("test") } returns AkilimoUser(id = 1, userName = "test")
        coEvery { selectedRepo.getSelectedByUser(1) } returns null
        every { factoryRepo.observeAll() } returns flowOf(listOf(factory1, factory2))
        every { selectedRepo.observeSelected(1) } returns flowOf(null)
        every { cassavaUnitRepo.observeAll() } returns flowOf(emptyList())

        viewModel = CassavaMarketViewModel(
            userRepo = userRepo,
            factoryRepo = factoryRepo,
            selectedRepo = selectedRepo,
            priceRepo = priceRepo,
            cassavaUnitRepo = cassavaUnitRepo,
            appSettings = appSettings,
            dataRefresher = dataRefresher
        )
    }

    @Test
    fun `loadData sets userId from user`() = runTest {
        viewModel.loadData("test")
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.userId == 1)
    }

    @Test
    fun `loadData populates factories from repo`() = runTest {
        viewModel.loadData("test")
        advanceUntilIdle()

        val factories = viewModel.uiState.value.factories
        assertTrue(factories.size == 2)
        assertTrue(factories.any { it.id == 1 })
        assertTrue(factories.any { it.id == 2 })
    }

    @Test
    fun `loadData returns early when user not found`() = runTest {
        coEvery { userRepo.getUser("unknown") } returns null

        viewModel.loadData("unknown")
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.userId == 0)
    }

    @Test
    fun `selectFactory marks matching factory as selected and deselects others`() = runTest {
        viewModel.loadData("test")
        advanceUntilIdle()

        viewModel.selectFactory(factory1)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.selectedFactoryId == 1)
    }

    @Test
    fun `selectFactory calls selectedRepo select`() = runTest {
        viewModel.loadData("test")
        advanceUntilIdle()

        viewModel.selectFactory(factory1)
        advanceUntilIdle()

        coVerify { selectedRepo.select(any()) }
    }

    @Test
    fun `selectFactory switching selection moves isSelected to new factory`() = runTest {
        viewModel.loadData("test")
        advanceUntilIdle()

        viewModel.selectFactory(factory1)
        advanceUntilIdle()

        viewModel.selectFactory(factory2)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.selectedFactoryId == 2)
    }

    private fun existingSelectionWithYield(yieldId: Int): SelectedCassavaMarketWithDetails {
        val saved = SelectedCassavaMarket(
            id = 9,
            userId = 1,
            yieldId = yieldId,
            produceType = EnumCassavaProduceType.entries.last()
        )
        return mockk(relaxed = true) { every { selectedCassavaMarket } returns saved }
    }

    @Test
    fun `selectFactory keeps the previously selected yield`() = runTest {
        viewModel.loadData("test")
        advanceUntilIdle()
        coEvery { selectedRepo.getSelectedByUser(1) } returns existingSelectionWithYield(yieldId = 5)

        viewModel.selectFactory(factory1)
        advanceUntilIdle()

        coVerify {
            selectedRepo.select(match {
                it.starchFactoryId == 1 && it.yieldId == 5 &&
                    it.produceType == EnumCassavaProduceType.entries.last()
            })
        }
    }

    @Test
    fun `saveSelectedPrice keeps the previously selected yield`() = runTest {
        every { appSettings.akilimoUser } returns "test"
        coEvery { selectedRepo.getSelectedByUser(1) } returns existingSelectionWithYield(yieldId = 5)
        val unit: CassavaUnit = mockk(relaxed = true) { every { id } returns 3 }

        viewModel.saveSelectedPrice(unit, EnumUnitOfSale.THOUSAND_KG, selectedPrice = null)
        advanceUntilIdle()

        coVerify {
            selectedRepo.select(match { it.cassavaUnitId == 3 && it.yieldId == 5 })
        }
    }

    @Test
    fun `refresh fetches the cassava market data again and stops the spinner`() = runTest {
        viewModel.refresh()
        advanceUntilIdle()

        coVerify(exactly = 1) { dataRefresher.cassavaMarket() }
        assertFalse(viewModel.uiState.value.isRefreshing)
    }
}
