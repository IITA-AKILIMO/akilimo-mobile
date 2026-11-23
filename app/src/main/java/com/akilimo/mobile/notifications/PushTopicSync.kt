package com.akilimo.mobile.notifications

import com.akilimo.mobile.data.AppSettingsDataStore
import com.akilimo.mobile.repos.AkilimoUserRepo
import com.google.firebase.messaging.FirebaseMessaging
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import javax.inject.Inject
import javax.inject.Singleton

/** Subscribes to and leaves FCM topics. Behind an interface so sync logic is testable without Firebase. */
interface TopicSubscriber {
    suspend fun subscribe(topic: String)
    suspend fun unsubscribe(topic: String)
}

@Singleton
class FirebaseTopicSubscriber @Inject constructor() : TopicSubscriber {
    override suspend fun subscribe(topic: String) = await(FirebaseMessaging.getInstance().subscribeToTopic(topic))
    override suspend fun unsubscribe(topic: String) = await(FirebaseMessaging.getInstance().unsubscribeFromTopic(topic))

    private suspend fun await(task: com.google.android.gms.tasks.Task<Void>) = suspendCoroutine { cont ->
        task.addOnCompleteListener { done ->
            if (done.isSuccessful) cont.resume(Unit) else cont.resumeWithException(
                done.exception ?: IllegalStateException("FCM topic request failed")
            )
        }
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class NotificationModule {
    @Binds
    abstract fun bindTopicSubscriber(impl: FirebaseTopicSubscriber): TopicSubscriber
}

/**
 * Brings the FCM topic subscriptions in line with the notifications switch, the farmer's country
 * and the app language. Safe to call any time; it only talks to FCM for topics that changed.
 */
@Singleton
class PushTopicSync @Inject constructor(
    private val appSettings: AppSettingsDataStore,
    private val userRepo: AkilimoUserRepo,
    private val subscriber: TopicSubscriber,
) {
    private val lock = Mutex()

    /** Returns true when the stored topics now match what is wanted; false if FCM failed part-way. */
    suspend fun sync(): Boolean = lock.withLock {
        val wanted = if (appSettings.pushEnabled()) {
            val country = userRepo.getUser(appSettings.akilimoUser)?.enumCountry
            NotificationTopics.forUser(country, appSettings.languageTag)
        } else {
            emptySet()
        }
        val current = appSettings.subscribedPushTopics().toMutableSet()

        var complete = true
        for (topic in current - wanted) {
            if (runCatching { subscriber.unsubscribe(topic) }.isSuccess) current.remove(topic) else complete = false
        }
        for (topic in wanted - current) {
            if (runCatching { subscriber.subscribe(topic) }.isSuccess) current.add(topic) else complete = false
        }
        appSettings.setSubscribedPushTopics(current)
        complete
    }
}
