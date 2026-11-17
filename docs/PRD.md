# AKILIMO Mobile — Product Requirements Document

> Checked against the code on 2026-11-17 (Android app, `develop`). Where a statement depends on the
> API or the recommendation engine, the API contract in `app/.../network/` is the reference.

---

## 1. Purpose

AKILIMO Mobile delivers site-specific agronomic recommendations to smallholder farmers and extension agents in Sub-Saharan Africa. It collects farm-specific inputs (location, crop, market prices, investment level) and returns personalised fertilizer recommendations, planting schedules, and profitability projections via a remote recommendation engine. Farmers can optionally create an account to keep a history of their saved advice.

---

## 2. Target Users

| Persona | Description |
|---------|-------------|
| Smallholder farmer | Primary user; limited literacy; works in local language (Swahili, Kinyarwanda) |
| Extension agent | Assists farmers; uses English; needs reliable offline access to reference data |
| Agronomist/IITA staff | Reviews recommendation quality; uses internal tooling |

**Supported countries:** Nigeria (NG), Tanzania (TZ), Ghana (GH), Rwanda (RW), Burundi (BI)

**Supported languages:** English, Swahili (`sw-TZ`), Kinyarwanda (`rw-RW`)

---

## 3. Current Features

### First run: legal wizard and onboarding
- A one-time wizard of 11 sections: welcome → disclaimer → terms → bio data → country → location → area unit → planting dates → tillage → investment preference → summary
- Bio data: first and last name (optional), gender (optional), interest in AKILIMO, recommendation language
- Country selection (Nigeria, Tanzania, Ghana, Rwanda, Burundi) and area unit (Acre, Hectare, Are)
- Farm location from GPS or a map picker (Mapbox, with LocationIQ reverse geocoding and current weather). The picker shows whether the farm is inside the area AKILIMO can compute recommendations for, from the coverage area served by the API

### Home (Hub)
- The app home after onboarding, with cards for **Recommendations**, **Profile** (edit the onboarding details), **Your farm** (country and location, field size, investment preference), **Settings**, and an account card (sign in, create an account, or "signed in as")
- Planting and harvest dates, tillage and weed control are edited in their own recommendation steps, not on the farm screen

### Recommendations
- Advice types: Fertilizer Recommendations (FR), Best Planting Practices (BPP), Scheduled Planting and High-Starch (SP/SPH), Intercropping with Maize (CIM), Intercropping with Sweet Potato (CIS)
- Each advice type is a checklist of use-case steps that must be completed before advice is requested:
  - Fertilizers available to the farmer, each with a price (choose a price option, "I don't know", or an exact price)
  - Investment amount
  - Planting and harvest dates (harvest must be 235 to 452 days after planting, the range the engine accepts)
  - Cassava yield, cassava market outlet (starch factory, or regular market with a unit of sale and price)
  - Maize performance, maize market outlet (fresh cob or dry grain, with a unit of sale and price)
  - Sweet potato market outlet
  - Tillage operations, manual tillage cost, tractor access, weed control cost
- Advice is computed by the API (`/v1/recommendations/compute`) and shown as readable text with a text size control and an optional landscape mode
- In-app feedback (satisfaction rating and a 0 to 10 recommendation score) after advice is shown

### Account (optional)
- Register with email and password, sign in, verify email, reset a forgotten password with an emailed code, or sign in with Google
- Consent is recorded; nothing is read or written on the server unless a signed-in farmer has given consent
- Saved advice: a copy on the phone (opens offline) kept in step with the account; filterable, paged list for long histories; open, share, delete one or several, delete all, and delete the account

### Settings
- Language, with an option to lock the app language independently of the phone
- Theme (system, light, dark) and text size for reading advice
- Fertilizer list as list or grid, and remember the area unit
- Farm location and the coverage-area check
- Data and storage: clear saved recommendations, reset the notification badge
- About: app version, privacy policy, terms

### Infrastructure
- Offline reference data via WorkManager background sync: fertilizers and their prices, cassava prices and units of sale, maize units, starch factories, investment amounts. Pull down on the fertilizer, cassava market and investment screens to refresh on demand. Rows the API stops serving are removed after a full sync
- Network connectivity banner (`NetworkMonitor`)
- Crash and error reporting through the Sentry SDK
- Usage analytics (Firebase Analytics)

---

## 4. Known Bugs

None recorded here. Open problems are tracked in GitHub issues and the error-reporting service.

---

## 5. Gaps & Missing Functionality

### Functional Gaps
- **No offline recommendation calculation** — network required for `/v1/recommendations/compute`
- **Email/SMS preference flags are stored but not exposed** — there is no screen to set them and no notification delivery is wired
- **No deep links** — recommendations cannot be shared or bookmarked by a URL that opens the app
- **No offline cache of the last recommendation per crop** — a recommendation can be saved to the account, but only a signed-in, consenting farmer gets that
- **Accessibility audit** — TalkBack, font scaling and content descriptions have not had a full pass

### Technical Gaps
- **Map SDK** — the map picker still uses Mapbox (a secret download token is needed to build); a MapLibre migration is planned (`docs/MAPLIBRE_MIGRATION.md`)
- **Test coverage** — unit tests cover the ViewModels and several repositories, but not the whole feature surface

---

## 6. Functional Requirements

### FR-1: Language Persistence
- Language selection MUST persist across cold starts.
- The language MUST be stored as a BCP-47 tag (`sw-TZ`, `rw-RW`, `en-US`), never a display name.
- The tag MUST be written to `AppSettingsDataStore` before the locale is applied.
- The language MUST be applied before any UI is shown, and a change MUST NOT need an app restart.

### FR-2: Settings Persistence
- All settings (language, theme, text size, list layout, area unit choice) MUST survive app restart.
- The theme MUST be applied on startup; the app starts in the light theme until the user chooses another.

### FR-3: Offline Access
- Reference data (fertilizers and prices, cassava units and prices, maize units, starch factories, investment amounts) MUST be available offline after the first sync.
- An empty or failed sync MUST NOT erase data already on the phone.
- Recommendations MUST clearly indicate when network is required.

### FR-4: Recommendation Flow
- The user MUST be able to complete the steps for an advice type and receive a recommendation.
- The planting and harvest dates the user can pick MUST be within what the engine accepts (harvest 235 to 452 days after planting).
- The advice MUST be readable in the user's selected language and text size.

### FR-5: Location
- GPS location MUST be acquired with the user's permission.
- Manual location via the map picker MUST be available as a fallback.
- The app MUST tell the user when the farm is outside the area where recommendations can be computed.

### FR-6: Accounts and Saved Advice
- An account MUST be optional; every recommendation feature works without one.
- Saved advice MUST only be uploaded or read when a signed-in farmer has given consent.
- A farmer MUST be able to delete saved advice and the account.
- The saved-advice list MUST open offline from the copy on the phone.

---

## 7. Non-Functional Requirements

| Requirement | Target |
|-------------|--------|
| Min Android version | API 21 (Android 5.0); target and compile SDK 36 |
| Offline capability | Reference data available offline after first sync |
| Crash rate | < 0.5% crash-free sessions |
| Network timeout | 60 seconds max per API call |
| Language support | English, Swahili (sw-TZ), Kinyarwanda (rw-RW) |
| Code obfuscation | R8 minification enabled in release builds |
| Accessibility | TalkBack compatibility; scalable text |
| Build reproducibility | Version name and code computed in the Gradle script from CI environment variables |
| Local database | Room with explicit migrations; the destructive fallback is used only on downgrade |
