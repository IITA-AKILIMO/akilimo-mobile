package com.akilimo.mobile.ui.viewmodels

import com.akilimo.mobile.data.AppSettingsDataStore
import com.akilimo.mobile.entities.AkilimoUser
import com.akilimo.mobile.enums.EnumAdvice
import com.akilimo.mobile.enums.EnumCountry
import com.akilimo.mobile.repos.AkilimoUserRepo
import com.akilimo.mobile.rules.TestDispatcherRule
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class RecommendationsViewModelTest {

    @get:Rule
    val dispatcherRule = TestDispatcherRule()

    private val userRepo: AkilimoUserRepo = mockk(relaxed = true)
    private val appSettings: AppSettingsDataStore = mockk(relaxed = true)

    @Before
    fun setUp() {
        clearAllMocks()
        every { appSettings.akilimoUser } returns "user1"
    }

    private fun buildViewModel() = RecommendationsViewModel(userRepo, appSettings)

    private fun userFor(country: EnumCountry) =
        AkilimoUser(id = 1, userName = "user1", enumCountry = country)

    @Test
    fun `loads intercropping maize for NG country`() = runTest {
        coEvery { userRepo.getUser("user1") } returns userFor(EnumCountry.NG)
        val viewModel = buildViewModel()
        advanceUntilIdle()

        val options = viewModel.uiState.value.adviceOptions.map { it.valueOption }
        assertTrue(EnumAdvice.INTERCROPPING_MAIZE in options)
        assertTrue(EnumAdvice.INTERCROPPING_SWEET_POTATO !in options)
    }

    @Test
    fun `loads intercropping sweet potato for TZ country`() = runTest {
        coEvery { userRepo.getUser("user1") } returns userFor(EnumCountry.TZ)
        val viewModel = buildViewModel()
        advanceUntilIdle()

        val options = viewModel.uiState.value.adviceOptions.map { it.valueOption }
        assertTrue(EnumAdvice.INTERCROPPING_SWEET_POTATO in options)
        assertTrue(EnumAdvice.INTERCROPPING_MAIZE !in options)
    }

    @Test
    fun `loads only 3 base options for unsupported country`() = runTest {
        coEvery { userRepo.getUser("user1") } returns userFor(EnumCountry.Unsupported)
        val viewModel = buildViewModel()
        advanceUntilIdle()

        assertEquals(3, viewModel.uiState.value.adviceOptions.size)
    }

    @Test
    fun `always includes 3 base recommendations`() = runTest {
        coEvery { userRepo.getUser("user1") } returns userFor(EnumCountry.NG)
        val viewModel = buildViewModel()
        advanceUntilIdle()

        val options = viewModel.uiState.value.adviceOptions.map { it.valueOption }
        assertTrue(EnumAdvice.FERTILIZER_RECOMMENDATIONS in options)
        assertTrue(EnumAdvice.BEST_PLANTING_PRACTICES in options)
        assertTrue(EnumAdvice.SCHEDULED_PLANTING_HIGH_STARCH in options)
    }

    @Test
    fun `does nothing when user not found`() = runTest {
        coEvery { userRepo.getUser("user1") } returns null
        val viewModel = buildViewModel()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.adviceOptions.isEmpty())
    }

    @Test
    fun `trackActiveAdvice saves selected advice on user`() = runTest {
        val user = userFor(EnumCountry.NG)
        coEvery { userRepo.getUser("user1") } returns user
        val viewModel = buildViewModel()

        viewModel.trackActiveAdvice(EnumAdvice.FERTILIZER_RECOMMENDATIONS)
        advanceUntilIdle()

        coVerify {
            userRepo.saveOrUpdateUser(
                user.copy(activeAdvise = EnumAdvice.FERTILIZER_RECOMMENDATIONS),
                "user1"
            )
        }
    }

    @Test
    fun `trackActiveAdvice does nothing when user not found`() = runTest {
        coEvery { userRepo.getUser("user1") } returns null
        val viewModel = buildViewModel()

        viewModel.trackActiveAdvice(EnumAdvice.FERTILIZER_RECOMMENDATIONS)
        advanceUntilIdle()

        coVerify(exactly = 0) { userRepo.saveOrUpdateUser(any(), any()) }
    }
}
