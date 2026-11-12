package com.akilimo.mobile.data.account

import android.content.Context
import android.content.SharedPreferences
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/** A farmer who is signed in. [token] is the Bearer token of the API; it is never shown or logged. */
@JsonClass(generateAdapter = true)
data class AccountSession(
    val token: String,
    /** Epoch millis; the API token lasts 30 days, then the farmer signs in again. */
    val expiresAt: Long,
    val accountId: Long,
    val name: String,
    val email: String,
    val emailVerified: Boolean,
    /** True until the farmer has agreed to the consent text (an older account); no advice is saved or shown meanwhile. */
    val consentRequired: Boolean,
) {
    /** What the screens may show: the token stays out of it. */
    val displayName: String get() = name.ifBlank { email }
}

/** Where the encrypted session is kept; a seam so the store can be tested without a phone. */
interface SessionStorage {
    fun read(): String?
    fun write(value: String)
    fun clear()
}

class PrefsSessionStorage(private val prefs: SharedPreferences) : SessionStorage {
    override fun read(): String? = prefs.getString(KEY, null)
    override fun write(value: String) = prefs.edit().putString(KEY, value).apply()
    override fun clear() = prefs.edit().remove(KEY).apply()

    private companion object {
        const val KEY = "session"
    }
}

/**
 * The signed-in farmer, if any. The session is encrypted with a Keystore key before it is stored, loaded again at
 * start-up (an expired one is dropped), and exposed as a flow so every screen follows a sign-in or sign-out.
 * The API token is read through [token] by the network layer on every call.
 */
@Singleton
class AccountSessionStore(
    private val storage: SessionStorage,
    private val cipher: TokenCipher,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    @Inject
    constructor(@ApplicationContext context: Context, cipher: TokenCipher) : this(
        PrefsSessionStorage(context.getSharedPreferences("account_session", Context.MODE_PRIVATE)),
        cipher,
    )

    private val adapter = Moshi.Builder().build().adapter(AccountSession::class.java)

    private val _session = MutableStateFlow(load())
    val session: StateFlow<AccountSession?> = _session.asStateFlow()

    /** The Bearer token for the API, or null when nobody is signed in. */
    val token: String? get() = _session.value?.token

    val isSignedIn: Boolean get() = _session.value != null

    fun save(session: AccountSession) {
        _session.value = session
        if (cipher.persistent) {
            runCatching { storage.write(cipher.encrypt(adapter.toJson(session))) }
        }
    }

    fun update(change: (AccountSession) -> AccountSession) {
        _session.value?.let { save(change(it)) }
    }

    fun clear() {
        _session.value = null
        storage.clear()
    }

    private fun load(): AccountSession? {
        if (!cipher.persistent) return null
        val stored = storage.read() ?: return null
        val session = cipher.decrypt(stored)?.let { runCatching { adapter.fromJson(it) }.getOrNull() }
        if (session == null || session.expiresAt <= clock()) {
            storage.clear()
            return null
        }
        return session
    }
}
