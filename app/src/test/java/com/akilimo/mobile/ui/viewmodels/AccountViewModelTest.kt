package com.akilimo.mobile.ui.viewmodels

import com.akilimo.mobile.R
import com.akilimo.mobile.data.account.AccountSession
import com.akilimo.mobile.data.account.AccountSessionStore
import com.akilimo.mobile.data.account.FakeSessionStorage
import com.akilimo.mobile.data.account.FakeTokenCipher
import com.akilimo.mobile.repos.account.AccountError
import com.akilimo.mobile.repos.account.AccountRepo
import com.akilimo.mobile.repos.account.AccountResult
import com.akilimo.mobile.repos.account.SavedAdviceRepo
import com.akilimo.mobile.repos.account.SignInOutcome
import com.akilimo.mobile.rules.TestDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AccountViewModelTest {

    @get:Rule
    val dispatcherRule = TestDispatcherRule()

    private val accounts = mockk<AccountRepo>(relaxed = true)
    private val savedAdvice = mockk<SavedAdviceRepo>(relaxed = true)
    private lateinit var store: AccountSessionStore

    private fun session(consentRequired: Boolean = false) =
        AccountSession("tok", Long.MAX_VALUE, 5, "Amina", "amina@example.com", false, consentRequired)

    private fun buildViewModel() = AccountViewModel(accounts, savedAdvice, store)

    private fun done() = AccountResult.Success(SignInOutcome(consentRequired = false, claimedRequests = 0))

    @Before
    fun setUp() {
        store = AccountSessionStore(FakeSessionStorage(), FakeTokenCipher())
        coEvery { savedAdvice.refresh() } returns AccountResult.Success(Unit)
    }

    @Test
    fun `sign in with a bad email asks for a valid one and calls nothing`() = runTest {
        val vm = buildViewModel()
        vm.onEmail("not-an-email")
        vm.onPassword("long-enough-1")

        vm.submit()

        assertEquals(R.string.account_err_email, vm.uiState.value.emailError)
        coVerify(exactly = 0) { accounts.login(any(), any()) }
    }

    @Test
    fun `sign in needs a password`() = runTest {
        val vm = buildViewModel()
        vm.onEmail("amina@example.com")

        vm.submit()

        assertEquals(R.string.account_err_password_required, vm.uiState.value.passwordError)
    }

    @Test
    fun `sign up needs a long enough password and the consent box`() = runTest {
        val vm = buildViewModel()
        vm.setMode(AccountViewModel.Mode.SIGN_UP)
        vm.onEmail("amina@example.com")
        vm.onPassword("short")

        vm.submit()

        assertEquals(R.string.account_err_password, vm.uiState.value.passwordError)
        assertEquals(R.string.account_err_consent, vm.uiState.value.consentError)
        coVerify(exactly = 0) { accounts.register(any(), any(), any(), any()) }
    }

    @Test
    fun `a successful sign in refreshes the saved advice and finishes`() = runTest {
        coEvery { accounts.login("amina@example.com", "long-enough-1") } returns done()
        val vm = buildViewModel()
        vm.onEmail("amina@example.com")
        vm.onPassword("long-enough-1")

        vm.submit()
        advanceUntilIdle()

        assertEquals(R.string.account_signed_in_done, vm.uiState.value.messageRes)
        assertFalse(vm.uiState.value.busy)
        assertEquals("the password is not kept", "", vm.uiState.value.password)
        coVerify { savedAdvice.refresh() }
        assertFalse("a success is not shown as an error", vm.uiState.value.messageIsError)
    }

    @Test
    fun `a sign up sends the consent that was ticked`() = runTest {
        coEvery { accounts.register(any(), any(), any(), any()) } returns done()
        val vm = buildViewModel()
        vm.setMode(AccountViewModel.Mode.SIGN_UP)
        vm.onEmail("amina@example.com")
        vm.onPassword("long-enough-1")
        vm.onName("Amina")
        vm.onConsent(true)

        vm.submit()
        advanceUntilIdle()

        coVerify { accounts.register("amina@example.com", "long-enough-1", "Amina", true) }
        assertEquals(R.string.account_signed_in_done, vm.uiState.value.messageRes)
    }

    @Test
    fun `wrong credentials show a message and stay on the screen`() = runTest {
        coEvery { accounts.login(any(), any()) } returns AccountResult.Failure(AccountError.WRONG_CREDENTIALS)
        val vm = buildViewModel()
        vm.onEmail("amina@example.com")
        vm.onPassword("wrong-password")

        vm.submit()
        advanceUntilIdle()

        assertEquals(R.string.account_err_wrong_credentials, vm.uiState.value.messageRes)
        assertNotEquals(R.string.account_signed_in_done, vm.uiState.value.messageRes)
        assertFalse(vm.uiState.value.busy)
    }

    @Test
    fun `an email the server refuses is shown on the email field`() = runTest {
        coEvery { accounts.register(any(), any(), any(), any()) } returns AccountResult.Failure(AccountError.VALIDATION)
        val vm = buildViewModel()
        vm.setMode(AccountViewModel.Mode.SIGN_UP)
        vm.onEmail("amina@example.com")
        vm.onPassword("long-enough-1")
        vm.onConsent(true)

        vm.submit()
        advanceUntilIdle()

        assertEquals(R.string.account_err_email_used, vm.uiState.value.emailError)
    }

    @Test
    fun `an older account that must consent is not finished and shows the consent step`() = runTest {
        coEvery { accounts.login(any(), any()) } coAnswers {
            store.save(session(consentRequired = true))
            AccountResult.Success(SignInOutcome(consentRequired = true, claimedRequests = 0))
        }
        val vm = buildViewModel()
        vm.onEmail("amina@example.com")
        vm.onPassword("long-enough-1")

        vm.submit()
        advanceUntilIdle()

        assertNotEquals(R.string.account_signed_in_done, vm.uiState.value.messageRes)
        assertEquals(AccountViewModel.ConsentStep.EXISTING_ACCOUNT, vm.consentStep(store.session.value, vm.uiState.value))
        coVerify(exactly = 0) { savedAdvice.refresh() }
    }

    @Test
    fun `agreeing records consent, links the advice and finishes`() = runTest {
        store.save(session(consentRequired = true))
        coEvery { accounts.acceptConsent() } returns AccountResult.Success(2)
        val vm = buildViewModel()

        vm.agree()
        advanceUntilIdle()

        assertEquals(R.string.account_signed_in_done, vm.uiState.value.messageRes)
        coVerify { savedAdvice.refresh() }
    }

    @Test
    fun `declining consent signs the farmer out`() = runTest {
        store.save(session(consentRequired = true))
        val vm = buildViewModel()

        vm.declineConsent()
        advanceUntilIdle()

        coVerify { accounts.signOut() }
    }

    @Test
    fun `a first Google sign in asks for consent, then finishes with the same token`() = runTest {
        coEvery { accounts.signInWithProvider("google", "g-token", false) } returns AccountResult.Failure(AccountError.CONSENT_REQUIRED)
        coEvery { accounts.signInWithProvider("google", "g-token", true) } returns done()
        val vm = buildViewModel()

        vm.onGoogleToken("g-token")
        advanceUntilIdle()

        assertEquals(AccountViewModel.ConsentStep.GOOGLE_FIRST_USE, vm.consentStep(null, vm.uiState.value))
        assertNotEquals(R.string.account_signed_in_done, vm.uiState.value.messageRes)

        vm.agree()
        advanceUntilIdle()

        assertEquals(R.string.account_signed_in_done, vm.uiState.value.messageRes)
        assertFalse(vm.uiState.value.googleConsent)
        coVerify { accounts.signInWithProvider("google", "g-token", true) }
    }

    @Test
    fun `a farmer who ticked the box on the sign up tab is not asked again for Google`() = runTest {
        coEvery { accounts.signInWithProvider("google", "g-token", true) } returns done()
        val vm = buildViewModel()
        vm.setMode(AccountViewModel.Mode.SIGN_UP)
        vm.onConsent(true)

        vm.onGoogleToken("g-token")
        advanceUntilIdle()

        assertEquals(R.string.account_signed_in_done, vm.uiState.value.messageRes)
    }

    @Test
    fun `declining the Google consent forgets the token`() = runTest {
        coEvery { accounts.signInWithProvider("google", "g-token", false) } returns AccountResult.Failure(AccountError.CONSENT_REQUIRED)
        val vm = buildViewModel()
        vm.onGoogleToken("g-token")
        advanceUntilIdle()

        vm.declineConsent()
        advanceUntilIdle()
        vm.agree()
        advanceUntilIdle()

        coVerify(exactly = 0) { accounts.signInWithProvider("google", "g-token", true) }
        assertFalse(vm.uiState.value.googleConsent)
    }

    @Test
    fun `a Google sign in that fails on the phone shows a message`() = runTest {
        val vm = buildViewModel()

        vm.onGoogleFailed()

        assertEquals(R.string.account_err_google, vm.uiState.value.messageRes)
    }

    @Test
    fun `deleting the account resets the screen and a failure keeps it`() = runTest {
        store.save(session())
        coEvery { accounts.deleteAccount() } returns AccountResult.Failure(AccountError.NETWORK)
        val vm = buildViewModel()

        vm.deleteAccount()
        advanceUntilIdle()

        assertEquals(R.string.account_err_network, vm.uiState.value.messageRes)

        coEvery { accounts.deleteAccount() } returns AccountResult.Success(Unit)
        vm.deleteAccount()
        advanceUntilIdle()

        assertNull(vm.uiState.value.messageRes)
    }

    @Test
    fun `a wrong verification code shows the message and a right one confirms`() = runTest {
        store.save(session())
        coEvery { accounts.verifyEmail("WRONG") } returns AccountResult.Failure(AccountError.BAD_CODE)
        coEvery { accounts.verifyEmail("RIGHT") } returns AccountResult.Success(Unit)
        val vm = buildViewModel()
        var closed = false

        vm.verifyEmail("WRONG") { closed = true }
        advanceUntilIdle()
        assertEquals(R.string.account_err_bad_code, vm.uiState.value.messageRes)
        assertFalse(closed)

        vm.verifyEmail("RIGHT") { closed = true }
        advanceUntilIdle()
        assertEquals(R.string.account_verified_done, vm.uiState.value.messageRes)
        assertFalse(vm.uiState.value.messageIsError)
        assertTrue(closed)
    }
}
