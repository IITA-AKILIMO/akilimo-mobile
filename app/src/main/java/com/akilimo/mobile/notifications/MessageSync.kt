package com.akilimo.mobile.notifications

import android.content.Context
import com.akilimo.mobile.config.AppConfig
import com.akilimo.mobile.data.AppSettingsDataStore
import com.akilimo.mobile.enums.EnumServiceType
import com.akilimo.mobile.network.AkilimoApi
import com.akilimo.mobile.network.ApiClient
import com.akilimo.mobile.network.dto.MessageDto
import com.akilimo.mobile.repos.AkilimoUserRepo
import com.akilimo.mobile.repos.InboxRepo
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/** Where messages come from. Behind an interface so the sync logic is testable without a network. */
interface MessageFetcher {
    /**
     * Messages for the audience from [since] on (ISO 8601, inclusive), oldest first, at most [limit]. [deviceToken] adds the
     * messages private to this phone or its account; the API returns those to no one else.
     */
    suspend fun fetch(
        country: String?,
        language: String?,
        since: String?,
        limit: Int,
        deviceToken: String?,
    ): List<MessageDto>
}

@Singleton
class ApiMessageFetcher @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : MessageFetcher {
    override suspend fun fetch(
        country: String?,
        language: String?,
        since: String?,
        limit: Int,
        deviceToken: String?,
    ): List<MessageDto> {
        val base = AppConfig.resolveBaseUrlFor(context, EnumServiceType.AKILIMO)
        val api = ApiClient.createService<AkilimoApi>(context, base)
        return api.getMessages(country = country, language = language, since = since, limit = limit, deviceToken = deviceToken).data
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class MessageSyncModule {
    @Binds
    abstract fun bindMessageFetcher(impl: ApiMessageFetcher): MessageFetcher
}

/**
 * Fills the inbox with the messages push did not deliver (phone off, notifications switched off, a reinstall, a new
 * country or language). The API is the source of truth; each message has an id, so one that also arrived by push is stored once.
 *
 * Only a cursor is kept: the server time of the newest message fetched. The next fetch asks from one second later, so a
 * message the farmer deleted does not come back. A first fetch for an audience (fresh install, or a new country or language)
 * stores what it finds as already read, so old messages do not all show as new.
 */
@Singleton
class MessageSync @Inject constructor(
    private val appSettings: AppSettingsDataStore,
    private val userRepo: AkilimoUserRepo,
    private val inbox: InboxRepo,
    private val fetcher: MessageFetcher,
) {
    private val lock = Mutex()

    /**
     * Empties the inbox and starts the sync over, for when the farmer signs out: messages addressed to the account must
     * not stay on the phone, and the API now answers for the phone alone. What it returns is stored as already read.
     * Returns true when the inbox is up to date again; false if the API could not be reached (the inbox stays empty).
     */
    suspend fun resetAndSync(): Boolean {
        lock.withLock {
            inbox.deleteAll()
            appSettings.setMessageSync("", null)
        }
        return sync()
    }

    /** Returns true when the inbox is up to date; false if the API could not be reached. Never throws. */
    suspend fun sync(): Boolean = lock.withLock {
        val country = NotificationTopics.countryCode(userRepo.getUser(appSettings.akilimoUser)?.enumCountry)
        val language = NotificationTopics.languageCode(appSettings.languageTag)
        val audience = "${country.orEmpty()}|${language.orEmpty()}"
        // Lets the API include the messages addressed to this phone or to its account
        val deviceToken = appSettings.deviceToken

        val sameAudience = appSettings.messageSyncAudience() == audience
        var cursor = if (sameAudience) appSettings.messageSyncCursor() else null
        // Old messages are marked read the first time an audience is fetched, not every time the cursor is empty
        val markRead = !sameAudience

        try {
            var pages = 0
            while (pages < MAX_PAGES) {
                pages++
                val page = fetcher.fetch(country, language, cursor?.let(::oneSecondAfter), PAGE_SIZE, deviceToken)
                val now = System.currentTimeMillis()
                page.forEach { dto -> dto.toEntity(fallbackTime = now, read = markRead)?.let { inbox.save(it) } }

                val newest = page.lastOrNull()?.createdAt
                // Remember how far we got (and for whom) even if a later page fails
                appSettings.setMessageSync(audience, newest ?: cursor)
                if (newest == null || newest == cursor || page.size < PAGE_SIZE) break
                cursor = newest
            }
            true
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.w(e, "Message sync failed")
            false
        }
    }

    private fun oneSecondAfter(time: String): String =
        runCatching { java.time.OffsetDateTime.parse(time).plusSeconds(1).toString() }.getOrDefault(time)

    private companion object {
        const val PAGE_SIZE = AkilimoApi.MESSAGES_PAGE_SIZE
        const val MAX_PAGES = 5
    }
}
