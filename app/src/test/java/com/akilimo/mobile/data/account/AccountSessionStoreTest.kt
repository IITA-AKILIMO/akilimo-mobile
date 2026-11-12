package com.akilimo.mobile.data.account

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** A reversible fake: stands in for the Keystore, so the test also proves the stored text is not the plain session. */
class FakeTokenCipher(override val persistent: Boolean = true, private val readable: Boolean = true) : TokenCipher {
    override fun encrypt(plain: String): String = "enc:" + plain.reversed()
    override fun decrypt(encrypted: String): String? =
        if (readable && encrypted.startsWith("enc:")) encrypted.removePrefix("enc:").reversed() else null
}

class FakeSessionStorage : SessionStorage {
    var value: String? = null
    override fun read(): String? = value
    override fun write(value: String) { this.value = value }
    override fun clear() { value = null }
}

class AccountSessionStoreTest {

    private val storage = FakeSessionStorage()
    private var now = 1_000L

    private fun session(expiresAt: Long = 10_000L, consentRequired: Boolean = false) = AccountSession(
        token = "secret-token", expiresAt = expiresAt, accountId = 7, name = "Amina", email = "amina@example.com",
        emailVerified = true, consentRequired = consentRequired,
    )

    private fun store(cipher: TokenCipher = FakeTokenCipher()) = AccountSessionStore(storage, cipher) { now }

    @Test
    fun `a saved session is exposed, encrypted on disk and found again after a restart`() {
        val first = store()
        first.save(session())

        assertEquals("secret-token", first.token)
        assertTrue(first.isSignedIn)
        assertNotNull(storage.value)
        assertFalse("the token must not be stored in plain text", storage.value!!.contains("secret-token"))

        val restarted = store()
        assertEquals(session(), restarted.session.value)
    }

    @Test
    fun `an expired session is dropped at start-up`() {
        store().save(session(expiresAt = 5_000L))
        now = 6_000L

        val restarted = store()

        assertNull(restarted.session.value)
        assertNull(restarted.token)
        assertNull(storage.value)
    }

    @Test
    fun `a stored value that cannot be decrypted is dropped`() {
        store().save(session())

        val restarted = store(FakeTokenCipher(readable = false))

        assertNull(restarted.session.value)
        assertNull(storage.value)
    }

    @Test
    fun `a phone without a keystore keeps the session in memory only`() {
        val memoryOnly = store(FakeTokenCipher(persistent = false))
        memoryOnly.save(session())

        assertTrue(memoryOnly.isSignedIn)
        assertNull("nothing is written to disk", storage.value)
        assertNull(store(FakeTokenCipher(persistent = false)).session.value)
    }

    @Test
    fun `clear signs out and removes the stored value`() {
        val store = store()
        store.save(session())

        store.clear()

        assertFalse(store.isSignedIn)
        assertNull(store.token)
        assertNull(storage.value)
    }

    @Test
    fun `update changes the signed-in session and stores it again`() {
        val store = store()
        store.save(session(consentRequired = true))

        store.update { it.copy(consentRequired = false) }

        assertFalse(store.session.value!!.consentRequired)
        assertFalse(store(FakeTokenCipher()).session.value!!.consentRequired)
    }

    @Test
    fun `update does nothing when nobody is signed in`() {
        val store = store()

        store.update { it.copy(name = "x") }

        assertNull(store.session.value)
    }
}
