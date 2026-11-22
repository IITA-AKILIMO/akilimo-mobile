package com.akilimo.mobile.notifications

import android.content.Context
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat
import com.akilimo.mobile.R

/** Creates the notification channels. Idempotent, and a no-op below Android 8 where channels do not exist. */
object NotificationChannels {

    /** Channel FCM uses for notification messages it shows itself while the app is in the background. */
    val fallbackChannelId: String = NotificationType.ANNOUNCEMENT.channelId

    fun ensure(context: Context) {
        val manager = NotificationManagerCompat.from(context)
        manager.createNotificationChannel(
            channel(NotificationType.ADVICE, R.string.notif_channel_advice, R.string.notif_channel_advice_desc, context)
        )
        manager.createNotificationChannel(
            channel(NotificationType.REMINDER, R.string.notif_channel_reminders, R.string.notif_channel_reminders_desc, context)
        )
        manager.createNotificationChannel(
            channel(NotificationType.ANNOUNCEMENT, R.string.notif_channel_announcements, R.string.notif_channel_announcements_desc, context)
        )
    }

    private fun channel(type: NotificationType, name: Int, description: Int, context: Context) =
        NotificationChannelCompat.Builder(type.channelId, NotificationManagerCompat.IMPORTANCE_DEFAULT)
            .setName(context.getString(name))
            .setDescription(context.getString(description))
            .build()
}
