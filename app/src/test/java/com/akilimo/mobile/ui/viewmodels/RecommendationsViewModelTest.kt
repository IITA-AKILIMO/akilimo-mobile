package com.akilimo.mobile.ui.viewmodels

import com.akilimo.mobile.entities.AkilimoUser
import com.akilimo.mobile.enums.EnumAdvice
import com.akilimo.mobile.enums.EnumCountry
import com.akilimo.mobile.repos.AkilimoUserRepo
import com.akilimo.mobile.rules.TestDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
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
    private lateinit var viewModel: RecommendationsViewModel

    @Before
    fun setUp() {
        viewModel = RecommendationsViewModel(userRepo)
    }

    private fun userFor(country: EnumCountry) =
        AkilimoUser(id = 1, userName = "user1", enumCountry = country)

    @Test
    fun `loadAdviceOptions for NG includes intercropping maize and not sweet potato`() = runTest {
        coEvery { userRepo.getUser("user1") } returns userFor(EnumCountry.NG)

        viewModel.loadAdviceOptions("user1")
        advanceUntilIdle()

        val options = viewModel.uiState.value.adviceOptions.map { it.valueOption }
        assertTrue(EnumAdvice.INTERCROPPING_MAIZE in options)
        assertTrue(EnumAdvice.INTERCROPPING_SWEET_POTATO !in options)
    }

    @Test
    fun `loadAdviceOptions for TZ includes intercropping sweet potato and not maize`() = runTest {
        coEvery { userRepo.getUser("user1") } returns userFor(EnumCountry.TZ)

        viewModel.loadAdviceOptions("user1")
        advanceUntilIdle()

        val options = viewModel.uiState.value.adviceOptions.map { it.valueOption }
        assertTrue(EnumAdvice.INTERCROPPING_SWEET_POTATO in options)
        assertTrue(EnumAdvice.INTERCROPPING_MAIZE !in options)
    }

    @Test
    fun `loadAdviceOptions for unsupported country has only 3 base options`() = runTest {
        coEvery { userRepo.getUser("user1") } returns userFor(EnumCountry.Unsupported)

        viewModel.loadAdviceOptions("user1")
        advanceUntilIdle()

        val options = viewModel.uiState.value.adviceOptions
        assertEquals(3, options.size)
    }

    @Test
    fun `loadAdviceOptions always includes the 3 base recommendations`() = runTest {
        coEvery { userRepo.getUser("user1") } returns userFor(EnumCountry.NG)

        viewModel.loadAdviceOptions("user1")
        advanceUntilIdle()

        val options = viewModel.uiState.value.adviceOptions.map { it.valueOption }
        assertTrue(EnumAdvice.FERTILIZER_RECOMMENDATIONS in options)
        assertTrue(EnumAdvice.BEST_PLANTING_PRACTICES in options)
        assertTrue(EnumAdvice.SCHEDULED_PLANTING_HIGH_STARCH in options)
    }

    @Test
    fun `loadAdviceOptions does nothing when user not found`() = runTest {
        coEvery { userRepo.getUser("unknown") } returns null

        viewModel.loadAdviceOptions("unknown")
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.adviceOptions.isEmpty())
    }

    @Test
    fun `trackActiveAdvice saves selected advice on user`() = runTest {
        val user = userFor(EnumCountry.NG)
        coEvery { userRepo.getUser("user1") } returns user

        viewModel.trackActiveAdvice("user1", EnumAdvice.FERTILIZER_RECOMMENDATIONS)
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
        coEvery { userRepo.getUser("unknown") } returns null

        viewModel.trackActiveAdvice("unknown", EnumAdvice.FERTILIZER_RECOMMENDATIONS)
        advanceUntilIdle()

        coVerify(exactly = 0) { userRepo.saveOrUpdateUser(any(), any()) }
    }
}
