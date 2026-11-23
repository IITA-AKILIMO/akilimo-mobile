package com.akilimo.mobile.notifications

import com.akilimo.mobile.entities.InboxMessage
import com.akilimo.mobile.enums.EnumCountry

/**
 * Kinds of push notification the app understands. A message names its kind in the `type` data key;
 * each kind has its own Android channel so farmers can mute one kind in system settings.
 */
enum class NotificationType(val wireName: String, val channelId: String, val defaultTarget: NotificationTarget) {
    ADVICE("advice", "advice_updates", NotificationTarget.RECOMMENDATIONS),
    REMINDER("reminder", "reminders", NotificationTarget.HUB),
    ANNOUNCEMENT("announcement", "announcements", NotificationTarget.HUB);

    companion object {
        /** Unknown or missing kinds are treated as announcements so nothing is silently dropped. */
        fun fromWireName(name: String?): NotificationType =
            entries.firstOrNull { it.wireName.equals(name?.trim(), ignoreCase = true) } ?: ANNOUNCEMENT
    }
}

/** Screens a notification tap can open. Mapped to Compose routes in the navigation layer. */
enum class NotificationTarget(val wireName: String) {
    HUB("hub"),
    RECOMMENDATIONS("recommendations"),
    SETTINGS("settings"),
    INBOX("inbox");

    companion object {
        fun fromWireName(name: String?): NotificationTarget? =
            entries.firstOrNull { it.wireName.equals(name?.trim(), ignoreCase = true) }
    }
}

/** Keys of the FCM data payload and of the extras carried on the tap intent. */
object NotificationKeys {
    const val TYPE = "type"
    const val TARGET = "target"
    const val TITLE = "title"
    const val BODY = "body"

    /** Optional sender id; the same id arriving twice is stored once in the inbox. */
    const val ID = "id"
}

/** Decides where a tapped notification goes. */
object NotificationRouting {
    /** An explicit `target` wins; otherwise the kind's default. */
    fun resolveTarget(type: NotificationType, targetName: String?): NotificationTarget =
        NotificationTarget.fromWireName(targetName) ?: type.defaultTarget

    /** Target for an intent's extras, or null when the intent did not come from a notification. */
    fun fromExtras(typeName: String?, targetName: String?): NotificationTarget? {
        if (typeName == null && targetName == null) return null
        return resolveTarget(NotificationType.fromWireName(typeName), targetName)
    }
}

/**
 * FCM topics for one install: everyone in a country, and everyone using a language. Topic names
 * may only contain letters, digits and `-_.~%`.
 */
object NotificationTopics {
    private const val COUNTRY_PREFIX = "country_"
    private const val LANGUAGE_PREFIX = "lang_"

    /** Country part of the audience: the enum name (`TZ`), or null for no or an unsupported country. */
    fun countryCode(country: EnumCountry?): String? =
        country?.takeIf { it != EnumCountry.Unsupported }?.name

    /** Language part of the audience: the lower-case language of a BCP-47 tag (`sw-TZ` gives `sw`), or null if unusable. */
    fun languageCode(languageTag: String?): String? =
        languageTag?.substringBefore('-')?.substringBefore('_')?.lowercase()
            ?.takeIf { it.isNotEmpty() && it.all { c -> c in 'a'..'z' } }

    /** [languageTag] is a BCP-47 tag such as `sw` or `sw-TZ`; only its language part is used. */
    fun forUser(country: EnumCountry?, languageTag: String?): Set<String> = buildSet {
        countryCode(country)?.let { add(COUNTRY_PREFIX + it) }
        languageCode(languageTag)?.let { add(LANGUAGE_PREFIX + it) }
    }
}

/** Turns a received payload into an inbox entry. */
object InboxMessages {
    /**
     * Null when the payload has no title or no body (nothing worth showing). [fallbackTitle] and [fallbackBody] come from a
     * `notification` block if there is one; [messageId] is FCM's own id, used when the sender gave none; [generatedId] is the last resort.
     */
    fun fromPayload(
        data: Map<String, String>,
        fallbackTitle: String?,
        fallbackBody: String?,
        messageId: String?,
        generatedId: String,
        receivedAt: Long,
    ): InboxMessage? {
        val title = data[NotificationKeys.TITLE] ?: fallbackTitle ?: return null
        val body = data[NotificationKeys.BODY] ?: fallbackBody ?: return null
        val type = NotificationType.fromWireName(data[NotificationKeys.TYPE])
        val id = data[NotificationKeys.ID]?.takeIf { it.isNotBlank() } ?: messageId?.takeIf { it.isNotBlank() } ?: generatedId
        return InboxMessage(
            id = id,
            type = type.wireName,
            title = title,
            body = body,
            target = NotificationTarget.fromWireName(data[NotificationKeys.TARGET])?.wireName,
            receivedAt = receivedAt,
        )
    }
}
