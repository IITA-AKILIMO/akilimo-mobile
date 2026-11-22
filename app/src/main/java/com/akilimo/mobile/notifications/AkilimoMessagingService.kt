package com.akilimo.mobile.notifications

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.akilimo.mobile.R
import com.akilimo.mobile.data.AppSettingsDataStore
import com.akilimo.mobile.repos.InboxRepo
import java.util.UUID
import com.akilimo.mobile.ui.activities.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

/**
 * Shows push messages. Messages are topic-based (see [NotificationTopics]) and should be sent as
 * data-only so this service builds the notification in the farmer's channel and language:
 * `type` (advice | reminder | announcement), `title`, `body` and an optional `target`
 * (hub | recommendations | settings | inbox) that overrides the type's default screen.
 *
 * Messages that carry a `notification` block are shown by FCM itself while the app is in the
 * background; a tap then delivers `type`/`target` as launch extras handled by MainActivity.
 */
@AndroidEntryPoint
class AkilimoMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var appSettings: AppSettingsDataStore

    @Inject
    lateinit var inbox: InboxRepo

    override fun onMessageReceived(message: RemoteMessage) {
        val data = message.data
        // The history keeps every message that reaches the phone, whatever the switch says: the switch
        // only decides whether a system notification is shown. FCM calls this on a background thread.
        InboxMessages.fromPayload(
            data = data,
            fallbackTitle = message.notification?.title,
            fallbackBody = message.notification?.body,
            messageId = message.messageId,
            generatedId = UUID.randomUUID().toString(),
            receivedAt = System.currentTimeMillis(),
        )?.let { entry -> runBlocking { inbox.save(entry) } }

        // A notification is shown only if the farmer opted in and Android allows it.
        if (!runBlocking { appSettings.pushEnabled() }) return
        if (!hasPostPermission(this)) return

        val title = data[NotificationKeys.TITLE] ?: message.notification?.title ?: return
        val body = data[NotificationKeys.BODY] ?: message.notification?.body ?: return
        val type = NotificationType.fromWireName(data[NotificationKeys.TYPE])
        val target = NotificationRouting.resolveTarget(type, data[NotificationKeys.TARGET])

        NotificationChannels.ensure(this)
        val notification = NotificationCompat.Builder(this, type.channelId)
            .setSmallIcon(R.drawable.ic_stat_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setContentIntent(tapIntent(this, type, target))
            .build()
        val manager = NotificationManagerCompat.from(this)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
        ) {
            manager.notify(System.currentTimeMillis().toInt(), notification)
        }
    }

    /** The FCM token is not stored: delivery is by topic, so the token never leaves the device. */
    override fun onNewToken(token: String) = Unit

    companion object {
        /** True when Android lets the app post: permission granted (13+) and not blocked in settings. */
        fun hasPostPermission(context: Context): Boolean {
            val permitted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            return permitted && NotificationManagerCompat.from(context).areNotificationsEnabled()
        }

        fun tapIntent(context: Context, type: NotificationType, target: NotificationTarget): PendingIntent {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(NotificationKeys.TYPE, type.wireName)
                putExtra(NotificationKeys.TARGET, target.wireName)
            }
            return PendingIntent.getActivity(
                context, target.ordinal, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        }
    }
}
