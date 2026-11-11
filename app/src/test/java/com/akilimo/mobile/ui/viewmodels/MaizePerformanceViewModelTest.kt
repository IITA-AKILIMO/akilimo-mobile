package com.akilimo.mobile.ui.viewmodels

import com.akilimo.mobile.data.AppSettingsDataStore
import com.akilimo.mobile.entities.AkilimoUser
import com.akilimo.mobile.entities.MaizePerformance
import com.akilimo.mobile.enums.EnumMaizePerformance
import com.akilimo.mobile.repos.AkilimoUserRepo
import com.akilimo.mobile.repos.MaizePerformanceRepo
import com.akilimo.mobile.rules.TestDispatcherRule
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MaizePerformanceViewModelTest {

    @get:Rule
    val dispatcherRule = TestDispatcherRule()

    private val userRepo: AkilimoUserRepo = mockk(relaxed = true)
    private val maizeRepo: MaizePerformanceRepo = mockk(relaxed = true)
    private val appSettings: AppSettingsDataStore = mockk(relaxed = true)
    private val testUser = AkilimoUser(id = 3, userName = "user1")

    @Before
    fun setUp() {
        clearAllMocks()
        every { appSettings.akilimoUser } returns "user1"
        coEvery { userRepo.getUser("user1") } returns testUser
        every { maizeRepo.observeByUserId(3) } returns flowOf(null)
    }

    private fun buildViewModel() = MaizePerformanceViewModel(userRepo, maizeRepo, appSettings)

    @Test
    fun `saved performance is pre-selected`() = runTest {
        val saved = EnumMaizePerformance.entries.last()
        every { maizeRepo.observeByUserId(3) } returns flowOf(MaizePerformance(userId = 3, maizePerformance = saved))
        val viewModel = buildViewModel()
        advanceUntilIdle()

        assertEquals(saved, viewModel.uiState.value.selected)
    }

    @Test
    fun `select only highlights and does not write`() = runTest {
        val viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.select(EnumMaizePerformance.entries.first())
        advanceUntilIdle()

        assertEquals(EnumMaizePerformance.entries.first(), viewModel.uiState.value.selected)
        coVerify(exactly = 0) { maizeRepo.saveOrUpdatePerformance(any()) }
    }

    @Test
    fun `saveSelection with nothing selected writes nothing`() = runTest {
        val viewModel = buildViewModel()
        advanceUntilIdle()
        assertNull(viewModel.uiState.value.selected)

        viewModel.saveSelection()
        advanceUntilIdle()

        coVerify(exactly = 0) { maizeRepo.saveOrUpdatePerformance(any()) }
        assertFalse(viewModel.uiState.value.saved)
    }

    @Test
    fun `saveSelection persists then sets saved`() = runTest {
        val viewModel = buildViewModel()
        advanceUntilIdle()
        val choice = EnumMaizePerformance.entries.first()
        viewModel.select(choice)

        viewModel.saveSelection()
        advanceUntilIdle()

        coVerify { maizeRepo.saveOrUpdatePerformance(match { it.userId == 3 && it.maizePerformance == choice }) }
        assertTrue(viewModel.uiState.value.saved)
    }

    @Test
    fun `saveSelection failure sets saveFailed`() = runTest {
        coEvery { maizeRepo.saveOrUpdatePerformance(any()) } throws IllegalStateException("db error")
        val viewModel = buildViewModel()
        advanceUntilIdle()
        viewModel.select(EnumMaizePerformance.entries.first())

        viewModel.saveSelection()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.saveFailed)
        assertFalse(viewModel.uiState.value.isSaving)
    }

    @Test
    fun `toggleLayout flips grid flag`() = runTest {
        val viewModel = buildViewModel()
        val before = viewModel.uiState.value.isGridLayout
        viewModel.toggleLayout()
        assertEquals(!before, viewModel.uiState.value.isGridLayout)
    }
}
