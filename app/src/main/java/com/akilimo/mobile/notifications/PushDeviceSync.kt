package com.akilimo.mobile.notifications

import android.content.Context
import com.akilimo.mobile.BuildConfig
import com.akilimo.mobile.config.AppConfig
import com.akilimo.mobile.data.AppSettingsDataStore
import com.akilimo.mobile.data.account.AccountSessionStore
import com.akilimo.mobile.enums.EnumServiceType
import com.akilimo.mobile.network.ApiClient
import com.akilimo.mobile.network.DeviceApi
import com.akilimo.mobile.network.dto.DeviceRegistrationRequest
import com.akilimo.mobile.network.dto.DeviceUnregisterRequest
import com.akilimo.mobile.repos.AkilimoUserRepo
import com.google.firebase.messaging.FirebaseMessaging
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/** The phone's current FCM registration token. Behind an interface so the sync logic is testable without Firebase. */
interface FcmTokenSource {
    /** The token, or null when Firebase cannot give one (offline, no Play services). */
    suspend fun token(): String?
}

@Singleton
class FirebaseTokenSource @Inject constructor() : FcmTokenSource {
    override suspend fun token(): String? = suspendCancellableCoroutine { cont ->
        FirebaseMessaging.getInstance().token.addOnCompleteListener { done ->
            cont.resume(if (done.isSuccessful) done.result?.takeIf { it.isNotBlank() } else null)
        }
    }
}

/** Where the registration goes. Behind an interface so the sync logic is testable without a network. */
interface DeviceRegistrar {
    /** Throws when the API did not accept the registration. */
    suspend fun register(request: DeviceRegistrationRequest)

    /** Throws when the API could not be reached or refused. */
    suspend fun unregister(deviceToken: String)
}

@Singleton
class ApiDeviceRegistrar @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val sessionStore: AccountSessionStore,
) : DeviceRegistrar {
    private fun api() = ApiClient.createService<DeviceApi>(
        context = context,
        baseUrl = AppConfig.resolveBaseUrlFor(context, EnumServiceType.AKILIMO),
        bearerToken = { sessionStore.token },
    )

    override suspend fun register(request: DeviceRegistrationRequest) {
        val response = api().register(request)
        check(response.isSuccessful) { "Device registration failed (${response.code()})" }
    }

    override suspend fun unregister(deviceToken: String) {
        val response = api().unregister(DeviceUnregisterRequest(deviceToken))
        check(response.isSuccessful) { "Device unregistration failed (${response.code()})" }
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class PushDeviceModule {
    @Binds
    abstract fun bindFcmTokenSource(impl: FirebaseTokenSource): FcmTokenSource

    @Binds
    abstract fun bindDeviceRegistrar(impl: ApiDeviceRegistrar): DeviceRegistrar
}

/**
 * Keeps the API's record of this phone (install id, FCM token, app version, country, language, signed-in account) in
 * line with the phone, so a message can be pushed to one farmer or one phone and not only to a topic.
 *
 * Only while notifications are switched on: switching them off, or signing out, removes the phone from the API. The last
 * registered token and details are kept in [AppSettingsDataStore], so a call that changes nothing sends nothing.
 * Safe to call at any time; it never throws.
 */
@Singleton
class PushDeviceSync @Inject constructor(
    private val appSettings: AppSettingsDataStore,
    private val userRepo: AkilimoUserRepo,
    private val sessionStore: AccountSessionStore,
    private val tokenSource: FcmTokenSource,
    private val registrar: DeviceRegistrar,
) {
    private val lock = Mutex()

    /**
     * Registers (or refreshes) the phone, or removes it when notifications are off. [knownToken] is a token Firebase has
     * just handed over (`onNewToken`); without it the current one is asked from Firebase.
     * Returns true when the API is in line with the phone; false if something failed (the next call retries).
     */
    suspend fun sync(knownToken: String? = null): Boolean = lock.withLock {
        try {
            if (!appSettings.pushEnabled()) return@withLock removeLocked()

            val token = knownToken?.takeIf { it.isNotBlank() } ?: tokenSource.token() ?: return@withLock false
            val details = details()
            if (token == appSettings.registeredPushToken() && details.encode() == appSettings.registeredPushDetails()) {
                return@withLock true
            }

            registrar.register(
                DeviceRegistrationRequest(
                    deviceToken = appSettings.deviceToken,
                    fcmToken = token,
                    appVersion = BuildConfig.VERSION_NAME,
                    country = details.country,
                    language = details.language,
                )
            )
            appSettings.setRegisteredPush(token, details.encode())
            true
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.w(e, "Push device registration failed")
            false
        }
    }

    /** Removes the phone from the API (sign-out, notifications off). Does nothing if it was never registered. */
    suspend fun unregister(): Boolean = lock.withLock {
        try {
            removeLocked()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.w(e, "Push device unregistration failed")
            false
        }
    }

    private suspend fun removeLocked(): Boolean {
        if (appSettings.registeredPushToken() == null) return true
        registrar.unregister(appSettings.deviceToken)
        appSettings.setRegisteredPush(null, null)
        return true
    }

    private suspend fun details(): Details {
        val country = NotificationTopics.countryCode(userRepo.getUser(appSettings.akilimoUser)?.enumCountry)
        val language = NotificationTopics.languageCode(appSettings.languageTag)
        // A farmer who has not yet agreed to the consent text is not linked by the API, so is not counted as signed in here
        val account = sessionStore.session.value?.takeUnless { it.consentRequired }?.accountId
        return Details(country, language, account, BuildConfig.VERSION_NAME)
    }

    /** What was last sent besides the token. Stored as one string; a change in any part means register again. */
    private data class Details(val country: String?, val language: String?, val account: Long?, val version: String) {
        fun encode() = "${country.orEmpty()}|${language.orEmpty()}|${account ?: ""}|$version"
    }
}
