package com.akilimo.mobile.ui.viewmodels

import app.cash.turbine.test
import com.akilimo.mobile.data.AppSettingsDataStore
import com.akilimo.mobile.entities.AkilimoUser
import com.akilimo.mobile.entities.UserPreferences
import com.akilimo.mobile.repos.AdviceCompletionRepo
import com.akilimo.mobile.repos.AkilimoUserRepo
import com.akilimo.mobile.repos.UserPreferencesRepo
import com.akilimo.mobile.rules.TestDispatcherRule
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    @get:Rule
    val dispatcherRule = TestDispatcherRule()

    private val appSettings: AppSettingsDataStore = mockk(relaxed = true)
    private val userRepo: AkilimoUserRepo = mockk(relaxed = true)
    private val prefsRepo: UserPreferencesRepo = mockk(relaxed = true)
    private val adviceRepo: AdviceCompletionRepo = mockk(relaxed = true)

    @Before
    fun setUp() {
        clearAllMocks()
        every { appSettings.darkModeFlow } returns flowOf(false)
        every { appSettings.languageTagFlow } returns flowOf("en")
        every { appSettings.rememberAreaUnitFlow } returns flowOf(false)
        every { appSettings.fertilizerGridFlow } returns flowOf(false)
        every { appSettings.lockAppLanguageFlow } returns flowOf(false)
        every { appSettings.akilimoUser } returns "user1"
        every { appSettings.languageTag } returns "en"
    }

    private fun buildViewModel() = SettingsViewModel(appSettings, userRepo, prefsRepo, adviceRepo)

    // ── init / state loading ──────────────────────────────────────────────────

    @Test
    fun `init populates state from all DataStore flows`() = runTest {
        every { appSettings.darkModeFlow } returns flowOf(true)
        every { appSettings.languageTagFlow } returns flowOf("sw-TZ")
        every { appSettings.rememberAreaUnitFlow } returns flowOf(true)
        every { appSettings.fertilizerGridFlow } returns flowOf(true)
        every { appSettings.lockAppLanguageFlow } returns flowOf(true)

        val viewModel = buildViewModel()
        advanceUntilIdle()

        with(viewModel.uiState.value) {
            assertTrue(darkMode)
            assertEquals("sw-TZ", languageTag)
            assertTrue(rememberAreaUnit)
            assertTrue(fertilizerGrid)
            assertTrue(lockAppLanguage)
        }
    }

    @Test
    fun `init defaults all state to false and english when DataStore returns defaults`() = runTest {
        val viewModel = buildViewModel()
        advanceUntilIdle()

        with(viewModel.uiState.value) {
            assertFalse(darkMode)
            assertEquals("en", languageTag)
            assertFalse(rememberAreaUnit)
            assertFalse(fertilizerGrid)
            assertFalse(lockAppLanguage)
        }
    }

    // ── Dark mode ─────────────────────────────────────────────────────────────

    @Test
    fun `setDarkMode persists enabled state to DataStore`() = runTest {
        val viewModel = buildViewModel()
        viewModel.setDarkMode(true)
        advanceUntilIdle()

        coVerify { appSettings.setDarkMode(true) }
    }

    @Test
    fun `setDarkMode persists disabled state to DataStore`() = runTest {
        val viewModel = buildViewModel()
        viewModel.setDarkMode(false)
        advanceUntilIdle()

        coVerify { appSettings.setDarkMode(false) }
    }

    // ── Display toggles ───────────────────────────────────────────────────────

    @Test
    fun `setFertilizerGrid persists value to DataStore`() = runTest {
        val viewModel = buildViewModel()
        viewModel.setFertilizerGrid(true)
        advanceUntilIdle()

        coVerify { appSettings.setFertilizerGrid(true) }
    }

    @Test
    fun `setRememberAreaUnit persists value to DataStore`() = runTest {
        val viewModel = buildViewModel()
        viewModel.setRememberAreaUnit(true)
        advanceUntilIdle()

        coVerify { appSettings.setRememberAreaUnit(true) }
    }

    // ── Language ──────────────────────────────────────────────────────────────

    @Test
    fun `setLanguage saves tag to all three stores and sends LanguageChanged effect`() = runTest {
        coEvery { userRepo.getUser("user1") } returns AkilimoUser(userName = "user1")
        coEvery { prefsRepo.getOrDefault() } returns UserPreferences()

        val viewModel = buildViewModel()

        viewModel.effect.test {
            viewModel.setLanguage("sw-TZ")
            advanceUntilIdle()

            coVerify { appSettings.setLanguageTag("sw-TZ") }
            coVerify { userRepo.saveOrUpdateUser(any(), "user1") }
            coVerify { prefsRepo.save(any()) }

            val effect = awaitItem() as SettingsViewModel.Effect.LanguageChanged
            assertEquals("sw-TZ", effect.languageTag)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setLanguage creates new user when no existing user found`() = runTest {
        coEvery { userRepo.getUser("user1") } returns null
        coEvery { prefsRepo.getOrDefault() } returns UserPreferences()

        val viewModel = buildViewModel()
        viewModel.setLanguage("ha")
        advanceUntilIdle()

        coVerify { userRepo.saveOrUpdateUser(match { it.userName == "user1" && it.languageCode == "ha" }, "user1") }
    }

    // ── Lock language ─────────────────────────────────────────────────────────

    @Test
    fun `setLockAppLanguage true persists and sends LockAppLanguageChanged effect`() = runTest {
        every { appSettings.languageTag } returns "sw-TZ"

        val viewModel = buildViewModel()

        viewModel.effect.test {
            viewModel.setLockAppLanguage(true)
            advanceUntilIdle()

            coVerify { appSettings.setLockAppLanguage(true) }
            val effect = awaitItem() as SettingsViewModel.Effect.LockAppLanguageChanged
            assertTrue(effect.locked)
            assertEquals("sw-TZ", effect.languageTag)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setLockAppLanguage false sends unlocked LockAppLanguageChanged effect`() = runTest {
        val viewModel = buildViewModel()

        viewModel.effect.test {
            viewModel.setLockAppLanguage(false)
            advanceUntilIdle()

            val effect = awaitItem() as SettingsViewModel.Effect.LockAppLanguageChanged
            assertFalse(effect.locked)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── Data & Storage — clear recommendations ────────────────────────────────

    @Test
    fun `clearRecommendations calls adviceRepo clearAll`() = runTest {
        val viewModel = buildViewModel()
        viewModel.clearRecommendations("Cleared")
        advanceUntilIdle()

        coVerify { adviceRepo.clearAll() }
    }

    @Test
    fun `clearRecommendations sends ShowSnackbar effect with provided message`() = runTest {
        val viewModel = buildViewModel()

        viewModel.effect.test {
            viewModel.clearRecommendations("Recommendation history cleared")
            advanceUntilIdle()

            val effect = awaitItem() as SettingsViewModel.Effect.ShowSnackbar
            assertEquals("Recommendation history cleared", effect.message)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── Data & Storage — reset notification badge ─────────────────────────────

    @Test
    fun `resetNotificationCount sets notificationCount to 3`() = runTest {
        val viewModel = buildViewModel()
        viewModel.resetNotificationCount("Reset")
        advanceUntilIdle()

        verify { appSettings.notificationCount = 3 }
    }

    @Test
    fun `resetNotificationCount sends ShowSnackbar effect with provided message`() = runTest {
        val viewModel = buildViewModel()

        viewModel.effect.test {
            viewModel.resetNotificationCount("Notification badge reset")
            advanceUntilIdle()

            val effect = awaitItem() as SettingsViewModel.Effect.ShowSnackbar
            assertEquals("Notification badge reset", effect.message)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
