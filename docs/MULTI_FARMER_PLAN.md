# Plan: one phone, several farmers (extension agents)

*IITA Internal Use*

**Status: deferred.** No user needs this today. Agents who share a phone only need broadcast messages (country and language topics), which work as they are. This document keeps the analysis so the work can start from it later. Nothing described here is built.

## The need

An extension agent uses one phone to get recommendations for several farmers. Today the app assumes one farmer per phone, plus at most one signed-in account.

## What the code already gives us (checked in the code, 2026-11-27)

- `akilimo_users` already holds many rows (unique `user_name`). Farmer data tables (current practice, field operation costs, maize performance, produce market, selected fertilizers, investments and cassava markets) are keyed by that user.
- The active farmer is a single setting, `AppSettingsDataStore.akilimoUser` (a user name). Switching farmers is changing that value.
- Country and language of the active profile drive: the push topics (`PushTopicSync`), the registration with the API (`PushDeviceSync`) and the inbox sync (`MessageSync`).
- `saved_advice` is keyed by account (`account_id`), not by farmer.
- The install id (`AppSettingsDataStore.deviceToken`) is one per phone and is what push, accounts and `MessageSync` use. `AkilimoUser` also has its own `deviceToken` field, which compute requests (`GetRecommendationViewModel`) use.
- On the API, `api_requests` records `device_token`, the account (`user_id`) and `user_info` (name, phone). Messaging for a phone is by country and language topics, or by device or account.

## Plan

1. **Farmers list and switch (the core).** An opt-in "I assist farmers" setting, off by default, so ordinary users see no change. Hub shows the active farmer's name with a switcher. Settings gets a "Farmers" list with add, switch and delete. "Add farmer" reuses the onboarding sections (the same ones the profile screen uses). No new table is needed at first.
2. **Switching side effects.** On a switch, call `PushTopicSync.sync()` and `PushDeviceSync.sync()` so topics and registration follow the new farmer. `MessageSync` already refetches when the country changes and stores the old messages as read; it does not clear the inbox.
3. **Keep each farmer's data apart.** Audit every screen and view model that reads `appSettings.akilimoUser` so no state is shared across farmers (selections, in-progress forms). Not yet checked one by one. Show the farmer's name or phone in the saved-advice list; compute requests already send `user_info`, so the API needs no change for this.
4. **Attribution.** Keep the install id for push and the account. Use the per-farmer `deviceToken` of `AkilimoUser` for compute requests, so the API's statistics do not count all of an agent's farmers as one.
5. **Consent and privacy.** The agent enters other people's data. Record a consent confirmation when each farmer is created, and make delete-farmer remove all of their local data. The wording needs IITA review.
6. **Room migration.** Only if new fields are needed (agent-mode flag, per-farmer consent): an explicit `Migration` from v10 to v11 and an updated exported schema (see AGENTS.md).
7. **Tests.** View-model tests for switching, isolation and deletion, following the AGENTS.md patterns.

Order: decisions first, then 1 and 2, then 3 and 4, then 5; 6 and 7 go along with each step.

## Decisions needed before starting

1. Is agent mode opt-in (the default in this plan), or does everyone see the farmers list?
2. Do broadcasts follow the active farmer's country, or the agent's home country?
3. Does each farmer need a recorded consent, or is the agent's own consent enough?
4. Do agents sign in to their own account while the farmers have none?

## Not covered here

- **Messages about one farmer.** The API has no "client of this agent" concept, so a message for farmer X served by agent Y cannot be addressed. Only the agent's own phone or account can. This would need a model change (agent account with client records, advice saved per client, delivery to the agent or by SMS) and is out of scope for now.
- **Shared-phone privacy of device messages.** Anything pushed to a device is readable by whoever uses the phone, so personal details about one farmer must not go in device-addressed messages. Signing out already empties the inbox and fetches it again for the phone alone.
