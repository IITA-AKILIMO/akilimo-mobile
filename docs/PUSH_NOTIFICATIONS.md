# Push notifications: sender guide

*IITA Internal Use*

How push messages reach AKILIMO installs. The akilimo-api stores messages and sends them (see its `docs/MESSAGES_API.md`): to **topics** for everyone in a country and/or language, and to **one farmer or one phone** by FCM token. This document describes the app side.

## How delivery works

There are two ways in, and both end in the same inbox:

1. **By FCM topic** (broadcasts). The app subscribes the install to topics derived from the farmer's country and app language, so the sender needs no device token.
2. **To one phone or one farmer** (private messages). The app registers its FCM token with the API (`PUT /v1/devices`, see "Per-user targeting" below) and the API pushes to that token. Registration is skipped while notifications are off.

Notifications are **opt-in**. The switch is off until the farmer allows it (one-time prompt on the Hub, or the Settings switch). While it is off the app is unsubscribed from every topic, and the service also drops any message that still arrives.

## Topics

| Topic | Who is in it | Example |
|---|---|---|
| `country_<EnumCountry name>` | Installs whose saved profile country is that country | `country_TZ`, `country_NG` |
| `lang_<language>` | Installs whose app language is that language | `lang_en`, `lang_sw`, `lang_rw` |

Rules (see `NotificationTopics` in `notifications/NotificationModel.kt`):

- Country comes from the saved profile (`AkilimoUser.enumCountry`). `Unsupported` or no profile gives no country topic. Use the exact `EnumCountry` enum name in the topic.
- Language comes from the app's BCP-47 tag (`sw`, `rw`, `en`; `sw-TZ` becomes `sw`), lower-cased, letters only.
- Topics are re-synced when the switch changes, at app start, when the app language changes, and when a saved profile changes the country. A farmer who moves country is on the new topics after saving the profile.
- Each topic is a separate audience. To reach "Swahili speakers in Tanzania" use a condition: `'country_TZ' in topics && 'lang_sw' in topics`.

## Message format: data-only

Send **data-only** messages (no `notification` block). The app then builds the notification itself, in the right channel and with the farmer's settings applied.

| Key | Required | Values |
|---|---|---|
| `type` | no | `advice`, `reminder`, `announcement`. Missing or unknown is treated as `announcement`. |
| `title` | yes | Shown as the title. Write it in the language of the topic you send to. |
| `body` | yes | Shown as the text; long text expands. |
| `target` | no | `hub`, `recommendations`, `settings`. Overrides the type's default screen. |

A message without `title` or `body` is dropped. All values are strings.

If a `notification` block is sent anyway, FCM shows it itself while the app is in the background (icon, colour and the `announcements` channel come from the manifest defaults). A tap still delivers `type` and `target` if they are in `data`, but the app cannot localise it or check the in-app switch for that display. Avoid it.

### Types, channels and default screens

| `type` | Channel id (user-visible name) | Default tap target |
|---|---|---|
| `advice` | `advice_updates` (Advice and price updates) | Recommendations |
| `reminder` | `reminders` (Reminders) | Hub |
| `announcement` | `announcements` (Announcements) | Hub |

Farmers can mute one kind in the phone's notification settings without losing the others.

### Tap behaviour

The notification opens `MainActivity` with `type` and `target` as extras. The target is resolved as: explicit `target`, else the type's default.

| Target | Compose route |
|---|---|
| `hub` | `HubRoute` |
| `recommendations` | `RecommendationsRoute` |
| `settings` | `SettingsRoute` |

Farmers who have not finished onboarding (first run) are not routed; the app opens its normal first-run flow.

## Sending from the Firebase console

The console always adds a `notification` block, so it is only good for a quick test:

1. Firebase console > Engage > Messaging > New campaign > Notifications.
2. Enter title and text, then in Target choose **Topic** and enter e.g. `country_TZ`.
3. Under Additional options > Custom data, add `type` (and `target` if needed).

For real sends use the HTTP v1 API below so the message stays data-only.

## Sending from akilimo-api (FCM HTTP v1)

The API does this itself (`app/Service/Fcm/FcmClient.php`); this is what it sends, for reference. A private message uses `"token": "<fcm token>"` instead of `topic`.

1. **Service account.** In the Firebase project create a service account with the "Firebase Cloud Messaging API Admin" role (or `firebasecloudmessaging.messages.create`), and store its JSON key as a server secret. Never put it in the repo or the app.
2. **Access token.** Exchange the service account for an OAuth 2.0 token with scope `https://www.googleapis.com/auth/firebase.messaging` (a Google auth library does this and refreshes it).
3. **Send.** `POST https://fcm.googleapis.com/v1/projects/<firebase-project-id>/messages:send` with `Authorization: Bearer <token>`:

```json
{
  "message": {
    "topic": "country_TZ",
    "data": {
      "type": "advice",
      "title": "Bei mpya za muhogo",
      "body": "Bei ya muhogo imebadilika katika eneo lako.",
      "target": "recommendations"
    },
    "android": { "priority": "HIGH" }
  }
}
```

For several audiences use `"condition": "'country_TZ' in topics && 'lang_sw' in topics"` instead of `topic`. A condition may combine at most five topics.

Notes:

- Send one message per language so the text matches the audience (for example `lang_sw` in Swahili, `lang_rw` in Kinyarwanda, `lang_en` in English).
- Topic delivery is best effort and not per-user: there is no read receipt and no way to tell which installs got it.
- Keep titles and bodies free of personal data. Anyone subscribed to a topic receives it.

## Per-user targeting

The API can push to one farmer's phones or to one install. The app's part is `PushDeviceSync` (`notifications/PushDeviceSync.kt`):

- **What it sends.** `PUT v1/devices` with `device_token` (the install id the app already sends with account and compute requests), `fcm_token`, `platform` (`android`), `app_version`, `country` and `language` (the same codes as the topics). A signed-in farmer's Bearer token goes with the call, which links the phone to the account on the server (only after the farmer has given consent).
- **When.** At app start, when Firebase rotates the token (`AkilimoMessagingService.onNewToken`), and when the app language, the profile country, the notifications switch or the signed-in account changes. The last registered token and details are kept in `AppSettingsDataStore` (`registeredPushToken`, `registeredPushDetails`), so a call that changes nothing sends nothing, and a failed call is retried at the next trigger.
- **Removal.** `DELETE v1/devices` (with the `device_token`) when notifications are switched off and when the farmer signs out or deletes the account. After a sign-out the phone is registered again, without the account, at the next app start. If a session ends on its own (expired token), the account link stays on the server until the next start re-registers the phone.
- **Receiving.** A private push is the same data-only message as a topic one, with the API's message `id`, so it lands in the inbox and shows a notification under the same rules. `MessageSync` also sends the `device_token` with `GET v1/messages`, so a private message the phone missed (push off, phone off, FCM failure) arrives with the next sync. The API returns private messages to no one else.
- **Dead tokens.** The API deletes a token FCM reports as `UNREGISTERED`; the app registers a fresh one at its next start.

Needs the API with the `push_devices` migration deployed **before** this app version is released; against an older API the registration answers 404, is logged and retried at the next start, and nothing else breaks.

For IITA review: the server now stores the FCM token and install id (and the account link for signed-in farmers). The privacy policy needs wording for it; see the API's `docs/MESSAGES_API.md`, "For IITA review".

## In-app inbox (message history)

Every data message that reaches the phone is saved to a local history (Room table `inbox_messages`, newest 500 kept) and shown in the **Messages** screen, opened from a Hub card with an unread badge. Saving does not depend on the notifications switch; the switch only decides whether a system notification is shown. A message is stored once per `id`.

Extra data key for the inbox:

| Key | Required | Meaning |
|---|---|---|
| `id` | recommended | Stable message id (the API's message id). Without it FCM's own id is used, and then a random one, so a resend would show twice. |

`target` may also be `inbox` (opens the Messages screen). The **Open** button on an expanded message follows the same target rules as a notification tap.

Limits today: only data-only messages reach the app code while it is in the background. A message sent with a `notification` block in the background is shown by Android and is not saved until the API sync below exists. Messages sent while the phone was off or the switch was off (the app is then unsubscribed) are never received.

### Recommended design: the API is the source of truth, FCM only announces

Sending through FCM from the API and syncing back for history is sound, with one change: let the API own the message, and let the app fetch what it missed, instead of the app pushing messages back.

1. An admin creates a message in the API: `id`, `type`, `title`, `body`, optional `target`, audience (`country`, `language`, or both), `created_at`, optional `expires_at`. The API stores it, then sends the FCM data message above with the same `id`.
2. The app keeps the FCM message in the inbox as it does today (instant, works offline after delivery).
3. During the existing sync (`SyncWorker`) the app calls `GET /messages?since=<last id or time>&country=<code>&language=<tag>` and inserts what it got. Inserts are idempotent on `id`, so a message that arrived both ways shows once. This fills the gaps FCM leaves: phone off, switch off, background messages Android showed itself, reinstall, and a new country or language.
4. Read and unread state stays on the phone. If wanted later, add `POST /messages/{id}/read` for registered phones or signed-in farmers only.
5. Keep the audience filter on the server: one query per country and language, not per farmer, so no personal data is sent.

This is built: the `messages` table and endpoints are in akilimo-api, and `MessageSync` is the app's sync step. The app inbox table matches the fields above.

## Not yet verified on a device

- Delivery of a topic message to a real install, on Android 13+ and on API 21 to 32.
- The permission dialog, the "Allow in phone settings" row, and the tap routes from a real notification.
- That the Firebase project and `google-services.json` allow topic messaging.
