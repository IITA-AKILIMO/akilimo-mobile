package com.akilimo.mobile.ui.viewmodels

import app.cash.turbine.test
import com.akilimo.mobile.data.AppSettingsDataStore
import com.akilimo.mobile.dto.LanguageOption
import com.akilimo.mobile.entities.AkilimoUser
import com.akilimo.mobile.entities.UserPreferences
import com.akilimo.mobile.repos.AkilimoUserRepo
import com.akilimo.mobile.repos.UserPreferencesRepo
import com.akilimo.mobile.rules.TestDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class WelcomeViewModelTest {

    @get:Rule
    val dispatcherRule = TestDispatcherRule()

    private val userRepo: AkilimoUserRepo = mockk(relaxed = true)
    private val prefsRepo: UserPreferencesRepo = mockk(relaxed = true)
    private val appSettings: AppSettingsDataStore = mockk(relaxed = true)

    private lateinit var viewModel: WelcomeViewModel

    @Before
    fun setUp() {
        viewModel = WelcomeViewModel(userRepo, prefsRepo, appSettings)
    }

    @Test
    fun `loadLanguage uses user language when available`() = runTest {
        coEvery { userRepo.getUser("user1") } returns AkilimoUser(
            userName = "user1",
            languageCode = "sw-TZ"
        )
        coEvery { prefsRepo.getOrDefault() } returns UserPreferences()

        viewModel.loadLanguage("user1")

        assertEquals("sw-TZ", viewModel.uiState.value.currentLanguageCode)
    }

    @Test
    fun `loadLanguage falls back to prefs when user language is blank`() = runTest {
        coEvery { userRepo.getUser("user1") } returns AkilimoUser(
            userName = "user1",
            languageCode = ""
        )
        coEvery { prefsRepo.getOrDefault() } returns UserPreferences(languageCode = "rw-RW")

        viewModel.loadLanguage("user1")

        assertEquals("rw-RW", viewModel.uiState.value.currentLanguageCode)
    }

    @Test
    fun `loadLanguage falls back to prefs when user is null`() = runTest {
        coEvery { userRepo.getUser("user1") } returns null
        coEvery { prefsRepo.getOrDefault() } returns UserPreferences(languageCode = "sw-TZ")

        viewModel.loadLanguage("user1")

        assertEquals("sw-TZ", viewModel.uiState.value.currentLanguageCode)
    }

    @Test
    fun `saveLanguage persists tag and updates uiState`() = runTest {
        val selected = LanguageOption(
            displayLabel = "Swahili",
            valueOption = "sw-TZ",
            languageCode = "sw-TZ"
        )
        coEvery { userRepo.getUser("user1") } returns AkilimoUser(userName = "user1")
        coEvery { prefsRepo.getOrDefault() } returns UserPreferences()

        viewModel.uiState.test {
            awaitItem() // initial state

            viewModel.saveLanguage(selected, "user1")

            val updated = awaitItem()
            assertEquals("sw-TZ", updated.currentLanguageCode)
            cancelAndIgnoreRemainingEvents()
        }

        coVerify { appSettings.setLanguageTag("sw-TZ") }
        coVerify { prefsRepo.save(any()) }
    }
}
