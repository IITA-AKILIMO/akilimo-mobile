package com.akilimo.mobile.ui.viewmodels

import com.akilimo.mobile.data.AppSettingsDataStore
import com.akilimo.mobile.entities.AkilimoUser
import com.akilimo.mobile.entities.ProduceMarket
import com.akilimo.mobile.enums.EnumCountry
import com.akilimo.mobile.enums.EnumMarketType
import com.akilimo.mobile.enums.EnumProduceType
import com.akilimo.mobile.enums.EnumUnitOfSale
import com.akilimo.mobile.repos.AkilimoUserRepo
import com.akilimo.mobile.repos.ProduceMarketRepo
import com.akilimo.mobile.rules.TestDispatcherRule
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProduceMarketViewModelTest {

    @get:Rule
    val dispatcherRule = TestDispatcherRule()

    private val userRepo: AkilimoUserRepo = mockk(relaxed = true)
    private val marketRepo: ProduceMarketRepo = mockk(relaxed = true)
    private val appSettings: AppSettingsDataStore = mockk(relaxed = true)

    private val lastMaize = ProduceMarket(
        userId = 5,
        unitPrice = 1200.0,
        marketType = EnumMarketType.MAIZE_MARKET,
        produceType = EnumProduceType.MAIZE_GRAIN,
        unitOfSale = EnumUnitOfSale.entries.first { it.isUniversal }
    )

    @Before
    fun setUp() {
        clearAllMocks()
        every { appSettings.akilimoUser } returns "user1"
        coEvery { userRepo.getUser("user1") } returns
                AkilimoUser(id = 5, userName = "user1", enumCountry = EnumCountry.NG)
    }

    private fun buildViewModel(type: EnumMarketType = EnumMarketType.MAIZE_MARKET) =
        ProduceMarketViewModel(userRepo, marketRepo, appSettings, type)

    @Test
    fun `init loads last entry for the requested market type and currency`() = runTest {
        coEvery { marketRepo.getLastEntryForUser(5, EnumMarketType.MAIZE_MARKET) } returns lastMaize

        val vm = buildViewModel()
        advanceUntilIdle()

        with(vm.uiState.value) {
            assertEquals(5, userId)
            assertEquals(EnumCountry.NG.currencyCode, currencyCode)
            assertEquals(lastMaize, lastEntry)
        }
        coVerify(exactly = 0) { marketRepo.getLastEntryForUser(any(), EnumMarketType.SWEET_POTATO_MARKET) }
    }

    @Test
    fun `saveMarketEntry persists and flags saved, onSaveHandled resets`() = runTest {
        val vm = buildViewModel()
        advanceUntilIdle()

        vm.saveMarketEntry(lastMaize)
        advanceUntilIdle()

        coVerify { marketRepo.saveMarketEntry(lastMaize) }
        assertTrue(vm.uiState.value.saved)

        vm.onSaveHandled()
        assertFalse(vm.uiState.value.saved)
    }
}
