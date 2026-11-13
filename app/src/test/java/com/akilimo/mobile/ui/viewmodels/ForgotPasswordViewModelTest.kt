package com.akilimo.mobile.ui.viewmodels

import com.akilimo.mobile.R
import com.akilimo.mobile.repos.account.AccountError
import com.akilimo.mobile.repos.account.AccountRepo
import com.akilimo.mobile.repos.account.AccountResult
import com.akilimo.mobile.rules.TestDispatcherRule
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
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ForgotPasswordViewModelTest {

    @get:Rule
    val dispatcherRule = TestDispatcherRule()

    private val accounts = mockk<AccountRepo>(relaxed = true)

    private fun buildViewModel() = ForgotPasswordViewModel(accounts)

    @Test
    fun `an invalid email is refused before any call`() = runTest {
        val vm = buildViewModel()
        vm.onEmail("nope")

        vm.sendCode()

        assertEquals(R.string.account_err_email, vm.uiState.value.emailError)
        coVerify(exactly = 0) { accounts.forgotPassword(any()) }
    }

    @Test
    fun `asking for a code moves to the code step whatever the address`() = runTest {
        coEvery { accounts.forgotPassword("amina@example.com") } returns AccountResult.Success(Unit)
        val vm = buildViewModel()
        vm.onEmail("amina@example.com")

        vm.sendCode()
        advanceUntilIdle()

        assertEquals(ForgotPasswordViewModel.Step.CODE, vm.uiState.value.step)
    }

    @Test
    fun `a network failure stays on the email step with a message`() = runTest {
        coEvery { accounts.forgotPassword(any()) } returns AccountResult.Failure(AccountError.NETWORK)
        val vm = buildViewModel()
        vm.onEmail("amina@example.com")

        vm.sendCode()
        advanceUntilIdle()

        assertEquals(ForgotPasswordViewModel.Step.EMAIL, vm.uiState.value.step)
        assertEquals(R.string.account_err_network, vm.uiState.value.messageRes)
    }

    @Test
    fun `a short new password or a missing code is refused before any call`() = runTest {
        val vm = buildViewModel()
        vm.onEmail("amina@example.com")
        vm.onPassword("short")

        vm.resetPassword()
        assertEquals(R.string.account_err_password, vm.uiState.value.passwordError)

        vm.onPassword("long-enough-1")
        vm.resetPassword()
        assertEquals(R.string.account_err_bad_code, vm.uiState.value.messageRes)
        coVerify(exactly = 0) { accounts.resetPassword(any(), any(), any()) }
    }

    @Test
    fun `a good code and password finish the reset and drop the password`() = runTest {
        coEvery { accounts.resetPassword("amina@example.com", "CODE1234", "long-enough-1") } returns AccountResult.Success(Unit)
        val vm = buildViewModel()
        vm.onEmail("amina@example.com")
        vm.onCode("CODE1234")
        vm.onPassword("long-enough-1")

        vm.resetPassword()
        advanceUntilIdle()

        assertEquals(ForgotPasswordViewModel.Step.DONE, vm.uiState.value.step)
        assertEquals("", vm.uiState.value.password)
    }

    @Test
    fun `a wrong code keeps the code step and says so`() = runTest {
        coEvery { accounts.resetPassword(any(), any(), any()) } returns AccountResult.Failure(AccountError.BAD_CODE)
        val vm = buildViewModel()
        vm.onEmail("amina@example.com")
        vm.onCode("WRONG")
        vm.onPassword("long-enough-1")

        vm.resetPassword()
        advanceUntilIdle()

        assertEquals(R.string.account_err_bad_code, vm.uiState.value.messageRes)
        assertFalse(vm.uiState.value.step == ForgotPasswordViewModel.Step.DONE)
        assertNull(vm.uiState.value.passwordError)
        assertTrue(vm.uiState.value.code == "WRONG")
    }
}
