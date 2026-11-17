# AKILIMO Mobile — Product Requirements Document

---

## 1. Purpose

AKILIMO Mobile delivers site-specific agronomic recommendations to smallholder farmers and extension agents in Sub-Saharan Africa. It collects farm-specific inputs (location, crop, market prices, investment level) and returns personalized fertilizer recommendations, planting schedules, and profitability projections via a remote recommendation engine.

---

## 2. Target Users

| Persona | Description |
|---------|-------------|
| Smallholder farmer | Primary user; limited literacy; works in local language (Swahili, Kinyarwanda) |
| Extension agent | Assists farmers; uses English; needs reliable offline access |
| Agronomist/IITA staff | Reviews recommendation quality; uses internal tooling |

**Supported countries:** Nigeria (NG), Tanzania (TZ), Ghana (GH), Rwanda (RW), Burundi (BI)

---

## 3. Current Features

### Onboarding & Profile
- 11-step guided stepper: language → disclaimer → terms → bio → country → location → area unit → planting dates → tillage → investment → summary
- Language selection: English, Swahili (Kiswahili), Kinyarwanda
- Country and area unit selection (Acre, Hectare, Are, m²)
- GPS and manual map-based location picker (Mapbox + LocationIQ)

### Crop Workflows
- Cassava fertilizer recommendations
- Maize intercrop recommendations
- Sweet potato intercrop recommendations
- Scheduled planting & high starch (SPH) workflow
- Best planting practices (BPP) workflow
- Weed management workflow

### Market & Economic Inputs
- Cassava, maize, sweet potato market price entry
- Cassava yield estimation
- Investment amount / budget preference selection
- Tillage and manual operation cost calculation

### Settings
- Profile: name, email, phone, gender, country, area unit
- Language (with app restart)
- Email/SMS notification preferences
- Dark mode toggle (saved; currently not applied — see known bugs)

### Infrastructure
- Offline reference data via WorkManager background sync (fertilizer catalog, prices, market data)
- Network connectivity banner (`NetworkMonitor` StateFlow)
- Crash reporting (Sentry 8.23)
- Usage analytics (Firebase Analytics)

---

## 4. Known Bugs

None recorded here. Open problems are tracked in GitHub issues and GlitchTip.

---

## 5. Gaps & Missing Functionality

### Functional Gaps
- **No offline recommendation calculation** — network required for `/v1/recommendations/compute`
- **No recommendation history** — users cannot review past recommendations without re-entering data
- **Push notification delivery** — email/SMS preference flags exist but no notification delivery is wired
- **No deep links** — recommendations cannot be shared or bookmarked by URL
- **Accessibility audit** — TalkBack, font scaling and content descriptions have not had a full pass

### Non-Functional Gaps
- **Test coverage** — unit tests cover the ViewModels and several repositories, but not the whole feature surface

---
## 6. Functional Requirements

### FR-1: Language Persistence (MUN-16 fix)
- Language selection MUST persist across cold starts.
- Full BCP-47 tag (`sw-TZ`, `rw-RW`, `en-US`) MUST be stored — not short code.
- Language MUST be applied via `LocaleHelper.wrap()` before any UI is inflated.
- `AppLocale.desiredLocale` MUST be set synchronously with user's choice.

### FR-2: Settings Persistence
- All `UserPreferences` fields MUST survive app restart.
- Dark mode MUST be applied on startup, not forced off.

### FR-3: Offline Access
- Reference data (fertilizer catalog, market prices, cassava units) MUST be available offline after first sync.
- Recommendations MUST clearly indicate when network is required.

### FR-4: Recommendation Flow
- User MUST be able to complete crop input workflow and receive recommendation response.
- Recommendation display MUST be readable in user's selected language.

### FR-5: Location
- GPS location MUST be acquired with user permission.
- Manual location via map picker MUST be available as fallback.

---

## 7. Non-Functional Requirements

| Requirement | Target |
|-------------|--------|
| Min Android version | API 21 (Android 5.0) |
| Offline capability | Reference data available offline after first launch |
| Crash rate | < 0.5% crash-free sessions (Sentry) |
| Network timeout | 60 seconds max per API call |
| Language support | English, Swahili (sw-TZ), Kinyarwanda (rw-RW) |
| Code obfuscation | R8 minification enabled in release builds |
| Accessibility | TalkBack compatibility; scalable text |
| Build reproducibility | Deterministic version code from CI environment variables |
