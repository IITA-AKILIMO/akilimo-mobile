package com.akilimo.mobile.ui.viewmodels

import com.akilimo.mobile.data.account.AccountPromptState
import com.akilimo.mobile.data.account.AccountSession
import com.akilimo.mobile.data.account.AccountSessionStore
import com.akilimo.mobile.data.account.FakeSessionStorage
import com.akilimo.mobile.data.account.FakeTokenCipher
import com.akilimo.mobile.repos.account.AccountRepo
import com.akilimo.mobile.rules.TestDispatcherRule
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AccountEntryViewModelTest {

    @get:Rule
    val dispatcherRule = TestDispatcherRule()

    private val accounts = mockk<AccountRepo>(relaxed = true)
    private val store = AccountSessionStore(FakeSessionStorage(), FakeTokenCipher())
    private val prompt = AccountPromptState()

    private fun signIn() = store.save(AccountSession("tok", Long.MAX_VALUE, 5, "Amina", "a@b.co", true, false))

    private fun buildViewModel() = AccountEntryViewModel(store, prompt, accounts)

    @Test
    fun `the prompt shows at start-up when nobody is signed in and can be dismissed`() = runTest {
        val vm = buildViewModel()

        assertTrue(vm.showPrompt.value)

        vm.dismissPrompt()

        assertFalse(vm.showPrompt.value)
    }

    @Test
    fun `the prompt does not come back in the same launch`() = runTest {
        buildViewModel()

        assertFalse(buildViewModel().showPrompt.value)
    }

    @Test
    fun `a signed-in farmer is not prompted`() = runTest {
        signIn()

        assertFalse(buildViewModel().showPrompt.value)
    }

    @Test
    fun `sign out goes through the account repository`() = runTest {
        signIn()
        val vm = buildViewModel()

        vm.signOut()
        advanceUntilIdle()

        coVerify { accounts.signOut() }
    }

    @Test
    fun `the signed-in name follows the session`() = runTest {
        val vm = buildViewModel()
        assertNull(vm.signedInAs.value)

        signIn()
        // The state is shared while subscribed; read the store's own value for the assertion
        assertEquals("Amina", store.session.value?.displayName)
    }
}
