package com.akilimo.mobile.ui.viewmodels

import app.cash.turbine.test
import com.akilimo.mobile.data.AppSettingsDataStore
import com.akilimo.mobile.entities.AkilimoUser
import com.akilimo.mobile.entities.UserPreferences
import com.akilimo.mobile.repos.AkilimoUserRepo
import com.akilimo.mobile.repos.UserPreferencesRepo
import com.akilimo.mobile.rules.TestDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class UserSettingsViewModelTest {

    @get:Rule
    val dispatcherRule = TestDispatcherRule()

    private val prefsRepo: UserPreferencesRepo = mockk(relaxed = true)
    private val userRepo: AkilimoUserRepo = mockk(relaxed = true)
    private val appSettings: AppSettingsDataStore = mockk(relaxed = true)

    private lateinit var viewModel: UserSettingsViewModel

    @Before
    fun setUp() {
        every { appSettings.languageTagFlow } returns flowOf("en-US")
        viewModel = UserSettingsViewModel(prefsRepo, userRepo, appSettings)
    }

    @Test
    fun `loadPreferences populates uiState with normalised language code`() = runTest {
        coEvery { prefsRepo.getOrDefault() } returns UserPreferences(languageCode = "sw")

        viewModel.loadPreferences()

        assertEquals("sw-TZ", viewModel.uiState.value.preferences?.languageCode)
        assertEquals("en-US", viewModel.uiState.value.previousLanguageCode)
    }

    @Test
    fun `savePreferences sets saved=true and emits languageChanged when language differs`() =
        runTest {
            coEvery { prefsRepo.getOrDefault() } returns UserPreferences(languageCode = "en-US")
            coEvery { userRepo.getUser("user1") } returns AkilimoUser(userName = "user1")

            val newPrefs = UserPreferences(languageCode = "sw-TZ")

            viewModel.uiState.test {
                awaitItem() // initial

                viewModel.savePreferences(newPrefs)

                val saved = awaitItem()
                assertTrue(saved.saved)
                assertTrue(saved.languageChanged)
                assertEquals("sw-TZ", saved.newLanguageCode)
                cancelAndIgnoreRemainingEvents()
            }

            coVerify { appSettings.setLanguageTag("sw-TZ") }
        }

    @Test
    fun `savePreferences does not set languageChanged when language is same`() = runTest {
        coEvery { prefsRepo.getOrDefault() } returns UserPreferences(languageCode = "en-US")
        coEvery { userRepo.getUser("user1") } returns AkilimoUser(userName = "user1")

        // Load first so previousLanguageCode = "en-US"
        viewModel.loadPreferences()

        val sameLanguagePrefs = UserPreferences(languageCode = "en-US")
        viewModel.savePreferences(sameLanguagePrefs)

        assertFalse(viewModel.uiState.value.languageChanged)
    }

    @Test
    fun `onSaveHandled resets saved and languageChanged flags`() = runTest {
        coEvery { prefsRepo.getOrDefault() } returns UserPreferences()
        coEvery { userRepo.getUser("user1") } returns AkilimoUser(userName = "user1")

        viewModel.savePreferences(UserPreferences(languageCode = "sw-TZ"))
        assertTrue(viewModel.uiState.value.saved)

        viewModel.onSaveHandled()

        assertFalse(viewModel.uiState.value.saved)
        assertFalse(viewModel.uiState.value.languageChanged)
    }
}
