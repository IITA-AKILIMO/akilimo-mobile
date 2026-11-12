package com.akilimo.mobile.data.account

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/** Encrypts the farmer's session before it is written to disk. */
interface TokenCipher {

    /** False when this phone cannot protect the token: the session then lives in memory only. */
    val persistent: Boolean

    fun encrypt(plain: String): String

    /** The text, or null when it cannot be read (a changed key, a damaged value). */
    fun decrypt(encrypted: String): String?
}

/** AES-GCM with a key that never leaves the Android Keystore (API 23+). */
class KeystoreTokenCipher : TokenCipher {

    override val persistent: Boolean = true

    override fun encrypt(plain: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key())
        val encrypted = cipher.doFinal(plain.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(cipher.iv + encrypted, Base64.NO_WRAP)
    }

    override fun decrypt(encrypted: String): String? = try {
        val bytes = Base64.decode(encrypted, Base64.NO_WRAP)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(TAG_BITS, bytes, 0, IV_BYTES))
        String(cipher.doFinal(bytes, IV_BYTES, bytes.size - IV_BYTES), Charsets.UTF_8)
    } catch (_: Exception) {
        null
    }

    private fun key(): SecretKey {
        val store = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (store.getKey(ALIAS, null) as? SecretKey)?.let { return it }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        generator.init(
            KeyGenParameterSpec.Builder(ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(KEY_BITS)
                .build()
        )
        return generator.generateKey()
    }

    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val ALIAS = "akilimo_account_session"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val IV_BYTES = 12
        private const val TAG_BITS = 128
        private const val KEY_BITS = 256

        /** The Keystore cipher on API 23+; older phones get [UnavailableTokenCipher]. */
        fun create(): TokenCipher =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) KeystoreTokenCipher() else UnavailableTokenCipher
    }
}

/** For phones below API 23, where the Keystore cannot hold an AES key: nothing is written to disk. */
object UnavailableTokenCipher : TokenCipher {
    override val persistent: Boolean = false
    override fun encrypt(plain: String): String = error("Not available on this phone")
    override fun decrypt(encrypted: String): String? = null
}
