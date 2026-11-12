package com.akilimo.mobile.ui.viewmodels

import com.akilimo.mobile.R
import com.akilimo.mobile.entities.SavedAdvice
import com.akilimo.mobile.repos.account.AccountError
import com.akilimo.mobile.repos.account.AccountResult
import com.akilimo.mobile.repos.account.SavedAdviceRepo
import com.akilimo.mobile.rules.TestDispatcherRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SavedAdviceViewModelTest {

    @get:Rule
    val dispatcherRule = TestDispatcherRule()

    private val repo = mockk<SavedAdviceRepo>(relaxed = true)
    private val list = MutableStateFlow(listOf(SavedAdvice("r1", 5, "NG", "FR", "FR", "text", 1L)))

    @Test
    fun `the list shows the phone copy and a quiet note when the refresh fails`() = runTest {
        every { repo.observeAll() } returns list
        coEvery { repo.refresh() } returns AccountResult.Failure(AccountError.NETWORK)
        val vm = SavedAdviceViewModel(repo)
        backgroundScope.launch { vm.items.collect { } }
        advanceUntilIdle()

        assertEquals(R.string.saved_advice_refresh_failed, vm.uiState.value.messageRes)
        assertFalse(vm.uiState.value.refreshing)
        assertEquals("r1", vm.items.value?.single()?.requestId)
    }

    @Test
    fun `a good refresh leaves no message`() = runTest {
        every { repo.observeAll() } returns list
        coEvery { repo.refresh() } returns AccountResult.Success(Unit)
        val vm = SavedAdviceViewModel(repo)
        advanceUntilIdle()

        assertNull(vm.uiState.value.messageRes)
    }
}
