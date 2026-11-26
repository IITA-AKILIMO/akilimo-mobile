package com.akilimo.mobile.repos

import com.akilimo.mobile.network.AppVersionApi
import com.akilimo.mobile.network.dto.PlatformVersionDto
import com.akilimo.mobile.network.reportError
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/** How urgent an update is for the installed build. */
enum class UpdateLevel { NONE, AVAILABLE, REQUIRED }

/** The release gate the API answered with. */
data class AppVersionPolicy(
    val minSupported: Int,
    val latest: Int,
    val message: String?,
    val storeUrl: String?,
) {
    fun levelFor(installedVersionCode: Int): UpdateLevel = when {
        installedVersionCode < minSupported -> UpdateLevel.REQUIRED
        installedVersionCode < latest -> UpdateLevel.AVAILABLE
        else -> UpdateLevel.NONE
    }
}

/** Reads the release gate. The only layer the update view model calls. */
@Singleton
class AppVersionRepo @Inject constructor(private val apiFactory: AppVersionApiFactory) {

    /**
     * The release gate, or null when it cannot be known (offline, server error, no entry for android).
     * Never throws: not knowing must never lock a farmer out.
     */
    suspend fun fetchPolicy(): AppVersionPolicy? = withContext(Dispatchers.IO) {
        try {
            val response = apiFactory.create().appVersion()
            response.body()?.data?.get(AppVersionApi.PLATFORM_ANDROID)
                ?.takeIf { response.isSuccessful }
                ?.toPolicy()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Offline is normal in the field: reportError drops cancellations and tones expected network failures down
            reportError(e)
            null
        }
    }

    private fun PlatformVersionDto.toPolicy() = AppVersionPolicy(
        minSupported = minSupported,
        latest = maxOf(latest, minSupported),
        message = message?.takeIf { it.isNotBlank() },
        storeUrl = storeUrl?.takeIf { it.isNotBlank() },
    )
}
