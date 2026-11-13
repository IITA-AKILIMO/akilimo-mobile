package com.akilimo.mobile.repos.account

import com.akilimo.mobile.data.AppSettingsDataStore
import com.akilimo.mobile.data.account.AccountSession
import com.akilimo.mobile.data.account.AccountSessionStore
import com.akilimo.mobile.data.account.FakeSessionStorage
import com.akilimo.mobile.data.account.FakeTokenCipher
import com.akilimo.mobile.network.AccountApi
import com.akilimo.mobile.network.dto.AccountUserDto
import com.akilimo.mobile.network.dto.AuthResponse
import com.akilimo.mobile.network.dto.MessageResponse
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import java.io.IOException

class AccountRepoTest {

    private val api = mockk<AccountApi>()
    private val factory = mockk<AccountApiFactory> { every { create() } returns api }
    private val settings = mockk<AppSettingsDataStore> { every { deviceToken } returns "phone-1" }
    private val savedAdvice = mockk<SavedAdviceRepo>(relaxed = true)
    private lateinit var store: AccountSessionStore
    private lateinit var repo: AccountRepo

    private fun error(code: Int, body: String = "{}") =
        body.toResponseBody("application/json".toMediaType())

    private fun auth(consentRequired: Boolean = false, claimed: Int = 0) = AuthResponse(
        tokenType = "Bearer", token = "tok-1", expiresAt = "2099-01-01T00:00:00+00:00",
        consentRequired = consentRequired, claimedRequests = claimed,
        user = AccountUserDto(id = 5, name = "Amina", email = "amina@example.com", emailVerified = false, consentRequired = consentRequired),
    )

    private fun signInAs(consentRequired: Boolean = false) = store.save(
        AccountSession("tok-1", Long.MAX_VALUE, 5, "Amina", "amina@example.com", false, consentRequired)
    )

    @Before
    fun setUp() {
        store = AccountSessionStore(FakeSessionStorage(), FakeTokenCipher())
        repo = AccountRepo(AccountCalls(factory, store), store, settings, savedAdvice)
    }

    @Test
    fun `login signs the farmer in and sends the device token`() = runTest {
        val sent = slot<com.akilimo.mobile.network.dto.AccountLoginRequest>()
        coEvery { api.login(capture(sent)) } returns Response.success(auth(claimed = 3))

        val result = repo.login(" amina@example.com ", "long-enough-1")

        assertEquals(AccountResult.Success(SignInOutcome(consentRequired = false, claimedRequests = 3)), result)
        assertEquals("amina@example.com", sent.captured.email)
        assertEquals("phone-1", sent.captured.deviceToken)
        assertEquals("tok-1", store.token)
        assertEquals("Amina", store.session.value?.name)
    }

    @Test
    fun `login with a wrong password is wrong credentials and signs nobody in`() = runTest {
        coEvery { api.login(any()) } returns Response.error(401, error(401))

        val result = repo.login("amina@example.com", "nope")

        assertEquals(AccountResult.Failure(AccountError.WRONG_CREDENTIALS), result)
        assertNull(store.token)
    }

    @Test
    fun `an older account that must consent is signed in but flagged`() = runTest {
        coEvery { api.login(any()) } returns Response.success(auth(consentRequired = true))

        val result = repo.login("amina@example.com", "long-enough-1") as AccountResult.Success

        assertTrue(result.value.consentRequired)
        assertTrue(store.session.value!!.consentRequired)
    }

    @Test
    fun `register sends consent and surfaces the field errors of a refused input`() = runTest {
        coEvery { api.register(any()) } returns Response.error(
            422, error(422, """{"message":"x","errors":{"email":["The email has already been taken."]}}""")
        )

        val result = repo.register("amina@example.com", "long-enough-1", "Amina", consent = true) as AccountResult.Failure

        assertEquals(AccountError.VALIDATION, result.error)
        assertEquals(listOf("The email has already been taken."), result.fieldErrors["email"])
        coVerify { api.register(match { it.consent && it.deviceToken == "phone-1" && it.name == "Amina" }) }
    }

    @Test
    fun `a blank name is not sent`() = runTest {
        coEvery { api.register(any()) } returns Response.success(auth())

        repo.register("amina@example.com", "long-enough-1", "  ", consent = true)

        coVerify { api.register(match { it.name == null }) }
    }

    @Test
    fun `a provider sign-in without consent on first use asks for consent`() = runTest {
        coEvery { api.social("google", any()) } returns Response.error(
            422, error(422, """{"message":"Consent is needed to create the account.","code":"consent_required"}""")
        )

        val result = repo.signInWithProvider("google", "access-token", consent = false)

        assertEquals(AccountResult.Failure(AccountError.CONSENT_REQUIRED), result)
    }

    @Test
    fun `a provider email that belongs to an unverified account is a conflict`() = runTest {
        coEvery { api.social("google", any()) } returns Response.error(409, error(409, """{"code":"email_unverified"}"""))

        val result = repo.signInWithProvider("google", "access-token", consent = true)

        assertEquals(AccountResult.Failure(AccountError.EMAIL_BELONGS_TO_ACCOUNT), result)
    }

    @Test
    fun `a provider that is not switched on is unavailable`() = runTest {
        coEvery { api.social("google", any()) } returns Response.error(404, error(404))

        assertEquals(
            AccountResult.Failure(AccountError.UNAVAILABLE),
            repo.signInWithProvider("google", "access-token", consent = true),
        )
    }

    @Test
    fun `no connection is a network failure`() = runTest {
        coEvery { api.login(any()) } throws IOException("offline")

        assertEquals(AccountResult.Failure(AccountError.NETWORK), repo.login("a@b.co", "x"))
    }

    @Test
    fun `too many attempts is reported`() = runTest {
        coEvery { api.login(any()) } returns Response.error(429, error(429))

        assertEquals(AccountResult.Failure(AccountError.TOO_MANY_ATTEMPTS), repo.login("a@b.co", "x"))
    }

    @Test
    fun `accepting consent clears the flag and reports the advice that was linked`() = runTest {
        signInAs(consentRequired = true)
        coEvery { api.consent(any()) } returns Response.success(MessageResponse("ok", claimedRequests = 2))

        val result = repo.acceptConsent()

        assertEquals(AccountResult.Success(2), result)
        assertFalse(store.session.value!!.consentRequired)
        coVerify { api.consent(match { it.consent && it.deviceToken == "phone-1" }) }
    }

    @Test
    fun `a signed-in call that answers 401 ends the session`() = runTest {
        signInAs()
        coEvery { api.consent(any()) } returns Response.error(401, error(401))

        val result = repo.acceptConsent()

        assertEquals(AccountResult.Failure(AccountError.SESSION_EXPIRED), result)
        assertNull(store.session.value)
    }

    @Test
    fun `verifying the email marks the session verified`() = runTest {
        signInAs()
        coEvery { api.verifyEmail(any()) } returns Response.success(MessageResponse("ok"))

        val result = repo.verifyEmail(" ABCD1234 ")

        assertEquals(AccountResult.Success(Unit), result)
        assertTrue(store.session.value!!.emailVerified)
        coVerify { api.verifyEmail(match { it.email == "amina@example.com" && it.code == "ABCD1234" }) }
    }

    @Test
    fun `a wrong verification code is reported and the session stays unverified`() = runTest {
        signInAs()
        coEvery { api.verifyEmail(any()) } returns Response.error(422, error(422, """{"message":"The code is wrong or has expired."}"""))

        assertEquals(AccountResult.Failure(AccountError.BAD_CODE), repo.verifyEmail("WRONG"))
        assertFalse(store.session.value!!.emailVerified)
    }

    @Test
    fun `resending the code while email is off is unavailable`() = runTest {
        signInAs()
        coEvery { api.resendVerification() } returns Response.error(503, error(503))

        assertEquals(AccountResult.Failure(AccountError.UNAVAILABLE), repo.resendVerification())
    }

    @Test
    fun `forgot and reset password go through`() = runTest {
        coEvery { api.forgotPassword(any()) } returns Response.success(MessageResponse("sent"))
        coEvery { api.resetPassword(any()) } returns Response.success(MessageResponse("changed"))

        assertEquals(AccountResult.Success(Unit), repo.forgotPassword(" a@b.co "))
        assertEquals(AccountResult.Success(Unit), repo.resetPassword("a@b.co", " CODE ", "new-password-1"))
        coVerify { api.forgotPassword(match { it.email == "a@b.co" }) }
        coVerify { api.resetPassword(match { it.code == "CODE" && it.password == "new-password-1" }) }
    }

    @Test
    fun `sign out forgets the session and the saved copies even when the server cannot be reached`() = runTest {
        signInAs()
        coEvery { api.logout() } throws IOException("offline")

        repo.signOut()

        assertNull(store.session.value)
        coVerify { savedAdvice.clearAll() }
    }

    @Test
    fun `deleting the account clears the session and the saved copies only after the server agrees`() = runTest {
        signInAs()
        coEvery { api.deleteAccount() } returns Response.error(500, error(500))

        assertEquals(AccountResult.Failure(AccountError.SERVER), repo.deleteAccount())
        assertTrue(store.isSignedIn)
        coVerify(exactly = 0) { savedAdvice.clearAll() }

        coEvery { api.deleteAccount() } returns Response.success(MessageResponse("deleted"))

        assertEquals(AccountResult.Success(Unit), repo.deleteAccount())
        assertFalse(store.isSignedIn)
        coVerify { savedAdvice.clearAll() }
    }
}
