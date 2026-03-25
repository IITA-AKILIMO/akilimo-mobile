# Package Audit Checklist

This checklist tracks the audit of packages that were identified as likely sources of structural drift:

- `helper`
- `utils`
- `interfaces`
- `extensions`
- `wizard`
- `network`
- `rest`

Use this document to classify each file before moving or deleting anything.

## How To Use This Checklist

For each file:

- decide whether it should stay where it is, move, merge, or be deleted
- note the target package if it should move
- check the item only after the decision is made and documented in code or a follow-up task

Suggested disposition labels:

- `Keep`
- `Move`
- `Merge`
- `Delete`
- `Review later`

## Audit Status

- Overall audit status: `Not started`
- Last updated: `2026-03-25`
- Owner: `[ ] Unassigned`

## Helper

Status: `[ ] Not started` `[ ] In progress` `[ ] Done`

- [ ] `helper/LocaleHelper.kt`
  Disposition: `Review later`
  Suggested target: `data/settings` or `ui` locale infrastructure
- [ ] `helper/WorkerError.kt`
  Disposition: `Review later`
  Suggested target: `workers` or `domain/models`
- [ ] `helper/WorkStateMapper.kt`
  Disposition: `Review later`
  Suggested target: `workers` or `ui` state-mapping package

## Utils

Status: `[ ] Not started` `[ ] In progress` `[ ] Done`

- [ ] `utils/AnimationHelper.kt`
  Disposition: `Review later`
  Suggested target: `ui/components` or `ui/theme`
- [ ] `utils/Converters.kt`
  Disposition: `Review later`
  Suggested target: `database` or `data/local`
- [ ] `utils/DateHelper.kt`
  Disposition: `Review later`
  Suggested target: `domain` or a narrower date/time utility package
- [ ] `utils/GeoCodingService.kt`
  Disposition: `Review later`
  Suggested target: `data/remote` or `network`
- [ ] `utils/LocationHelper.kt`
  Disposition: `Review later`
  Suggested target: `data`, `ui`, or location-specific infrastructure
- [ ] `utils/MathHelper.kt`
  Disposition: `Review later`
  Suggested target: `domain`
- [ ] `utils/NumberHelper.kt`
  Disposition: `Review later`
  Suggested target: `domain` or a narrower formatting utility package
- [ ] `utils/PermissionHelper.kt`
  Disposition: `Review later`
  Suggested target: `ui` or platform-specific infrastructure
- [ ] `utils/RecommendationBuilder.kt`
  Disposition: `Review later`
  Suggested target: feature-specific recommendations package or `domain`
- [ ] `utils/StartupManager.kt`
  Disposition: `Review later`
  Suggested target: `ui/activities`, `navigation`, or app startup infrastructure
- [ ] `utils/StringHelper.kt`
  Disposition: `Review later`
  Suggested target: narrower shared formatting package
- [ ] `utils/WeatherService.kt`
  Disposition: `Review later`
  Suggested target: `data/remote` or `network`

## Interfaces

Status: `[ ] Not started` `[ ] In progress` `[ ] Done`

- [ ] `interfaces/IDispatcherProvider.kt`
  Disposition: `Review later`
  Suggested target: `data`, `domain`, or coroutine infrastructure package
- [ ] `interfaces/ILabelProvider.kt`
  Disposition: `Review later`
  Suggested target: feature-specific package or `ui`
- [ ] `interfaces/IProduceType.kt`
  Disposition: `Review later`
  Suggested target: `domain/models` or `domain/contracts`

## Extensions

Status: `[ ] Not started` `[ ] In progress` `[ ] Done`

- [ ] `extensions/WorkerExtensions.kt`
  Disposition: `Review later`
  Suggested target: `workers`

## Wizard

Status: `[ ] Not started` `[ ] In progress` `[ ] Done`

- [ ] `wizard/OnboardingSection.kt`
  Disposition: `Review later`
  Suggested target: `ui/features/onboarding` or `navigation`

## Network

Status: `[ ] Not started` `[ ] In progress` `[ ] Done`

- [ ] `network/AkilimoApi.kt`
  Disposition: `Review later`
  Suggested target: `data/remote`
- [ ] `network/ApiClient.kt`
  Disposition: `Review later`
  Suggested target: `data/remote`
- [ ] `network/LocalDateAdapter.kt`
  Disposition: `Review later`
  Suggested target: `data/remote`
- [ ] `network/LocationIqApi.kt`
  Disposition: `Review later`
  Suggested target: `data/remote`
- [ ] `network/NetworkMonitor.kt`
  Disposition: `Review later`
  Suggested target: `data/remote` or app infrastructure
- [ ] `network/NetworkUtils.kt`
  Disposition: `Review later`
  Suggested target: `data/remote` or app infrastructure
- [ ] `network/RetryInterceptor.kt`
  Disposition: `Review later`
  Suggested target: `data/remote`
- [ ] `network/WeatherApi.kt`
  Disposition: `Review later`
  Suggested target: `data/remote`

## Rest

Status: `[ ] Not started` `[ ] In progress` `[ ] Done`

- [ ] `rest/request/ComputeRequest.kt`
  Disposition: `Review later`
  Suggested target: `data/remote/request`
- [ ] `rest/request/FertilizerRequest.kt`
  Disposition: `Review later`
  Suggested target: `data/remote/request`
- [ ] `rest/request/RecommendationRequest.kt`
  Disposition: `Review later`
  Suggested target: `data/remote/request`
- [ ] `rest/request/UserInfo.kt`
  Disposition: `Review later`
  Suggested target: `data/remote/request`
- [ ] `rest/response/ReverseGeocodeResponse.kt`
  Disposition: `Review later`
  Suggested target: `data/remote/response`
- [ ] `rest/response/WeatherResponse.kt`
  Disposition: `Review later`
  Suggested target: `data/remote/response`

## Exit Criteria

- [ ] Every file in the target packages has an explicit disposition.
- [ ] Every file marked `Move` has a destination package.
- [ ] Every file marked `Delete` has a safety check or replacement noted.
- [ ] The audit is detailed enough to execute refactors in small slices.
