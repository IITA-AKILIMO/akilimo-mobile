package com.akilimo.mobile.ui.viewmodels

import com.akilimo.mobile.repos.AppVersionPolicy
import com.akilimo.mobile.repos.AppVersionRepo
import com.akilimo.mobile.repos.UpdateLevel
import com.akilimo.mobile.rules.TestDispatcherRule
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AppUpdateViewModelTest {

    @get:Rule
    val dispatcherRule = TestDispatcherRule()

    private val repo: AppVersionRepo = mockk(relaxed = true)

    @Before
    fun setUp() {
        clearAllMocks()
    }

    private fun buildViewModel(installed: Int) = AppUpdateViewModel(repo, installed)

    private fun policy(min: Int, latest: Int, message: String? = null, storeUrl: String? = null) =
        AppVersionPolicy(minSupported = min, latest = latest, message = message, storeUrl = storeUrl)

    @Test
    fun `an install below the minimum is blocked`() = runTest {
        coEvery { repo.fetchPolicy() } returns policy(min = 10, latest = 12, message = "Please update")

        val viewModel = buildViewModel(installed = 9)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(UpdateLevel.REQUIRED, state.level)
        assertTrue(state.blocking)
        assertFalse(state.showBanner)
        assertEquals("Please update", state.message)
    }

    @Test
    fun `an install between minimum and latest sees a dismissible banner`() = runTest {
        coEvery { repo.fetchPolicy() } returns policy(min = 5, latest = 12, storeUrl = "https://play.google.com/x")

        val viewModel = buildViewModel(installed = 8)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.blocking)
        assertTrue(viewModel.uiState.value.showBanner)
        assertEquals("https://play.google.com/x", viewModel.uiState.value.storeUrl)

        viewModel.dismissBanner()

        assertFalse(viewModel.uiState.value.showBanner)
        assertEquals(UpdateLevel.AVAILABLE, viewModel.uiState.value.level)
    }

    @Test
    fun `an up to date install sees nothing`() = runTest {
        coEvery { repo.fetchPolicy() } returns policy(min = 5, latest = 12)

        val viewModel = buildViewModel(installed = 12)
        advanceUntilIdle()

        assertEquals(UpdateLevel.NONE, viewModel.uiState.value.level)
        assertFalse(viewModel.uiState.value.showBanner)
    }

    @Test
    fun `an unknown answer never blocks`() = runTest {
        coEvery { repo.fetchPolicy() } returns null

        val viewModel = buildViewModel(installed = 1)
        advanceUntilIdle()

        assertEquals(UpdateLevel.NONE, viewModel.uiState.value.level)
        assertFalse(viewModel.uiState.value.blocking)
        assertNull(viewModel.uiState.value.message)
    }

    @Test
    fun `recheck inside the interval does not ask again, after it does`() = runTest {
        coEvery { repo.fetchPolicy() } returns policy(min = 1, latest = 1)
        var now = System.currentTimeMillis() + 1_000L
        val viewModel = buildViewModel(installed = 1)
        viewModel.clock = { now }
        viewModel.recheck() // sets the time of the last answer
        advanceUntilIdle()
        coVerify(exactly = 2) { repo.fetchPolicy() }

        now += 1_000L
        viewModel.recheck(minIntervalMs = 5_000L)
        advanceUntilIdle()
        coVerify(exactly = 2) { repo.fetchPolicy() }

        now += 10_000L
        coEvery { repo.fetchPolicy() } returns policy(min = 5, latest = 5)
        viewModel.recheck(minIntervalMs = 5_000L)
        advanceUntilIdle()

        coVerify(exactly = 3) { repo.fetchPolicy() }
        assertEquals(UpdateLevel.REQUIRED, viewModel.uiState.value.level)
    }

    @Test
    fun `a failed recheck keeps the last known answer`() = runTest {
        coEvery { repo.fetchPolicy() } returns policy(min = 10, latest = 12)
        val viewModel = buildViewModel(installed = 9)
        advanceUntilIdle()

        coEvery { repo.fetchPolicy() } returns null
        viewModel.recheck()
        advanceUntilIdle()

        assertEquals(UpdateLevel.REQUIRED, viewModel.uiState.value.level)
    }

    @Test
    fun `the policy decides the level from the installed version code`() {
        val policy = policy(min = 10, latest = 20)

        assertEquals(UpdateLevel.REQUIRED, policy.levelFor(9))
        assertEquals(UpdateLevel.AVAILABLE, policy.levelFor(10))
        assertEquals(UpdateLevel.AVAILABLE, policy.levelFor(19))
        assertEquals(UpdateLevel.NONE, policy.levelFor(20))
        assertEquals(UpdateLevel.NONE, policy.levelFor(21))
    }
}
