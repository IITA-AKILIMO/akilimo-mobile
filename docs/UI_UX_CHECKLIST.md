# AKILIMO UI/UX checklist

IITA Internal Use

A working reference for building and reviewing screens in the AKILIMO Android app. It complements
`AGENTS.md` (conventions) and `docs/COMPOSE_MIGRATION.md`. Tick the boxes in a PR description; do
not edit the ticks here.

## 1. Who we design for

- [ ] Smallholder farmers and extension workers, often outdoors, in bright light, on mid-range phones.
- [ ] Limited or no connectivity: the app must stay usable and say what is saved.
- [ ] Mixed literacy and several languages (en, sw, rw, ha): words stay short, icons support them.
- [ ] One clear primary action per screen.

## 2. Look and feel

- [ ] Colours come from the theme (`MaterialTheme.colorScheme`), never hard-coded in a screen.
- [ ] Palette follows the logo: deep green `#006F45`; leaf lime `#9ACA3C` only as a fill with dark text
      (white on lime fails contrast).
- [ ] Cards and buttons use `MaterialTheme.shapes.medium` (12dp); spacing uses `AkilimoSpacing`.
- [ ] Raised cards: surface fill, thin outline-variant border, small shadow (`cardSurface()`).
- [ ] Status is shown in words plus colour (`StatusChip`), never colour or an icon alone.
- [ ] The app starts in the light theme; System and Dark are the user's choice in Settings.
- [ ] Back arrow is the quiet outlined 22dp icon (`BackTopAppBar`); keep the 48dp tap area.

## 3. Reuse before you build

Check `ui/components/compose/` first.

| Need | Component |
|------|-----------|
| Top bar with back and subtitle | `BackTopAppBar` |
| Group fields or sections on a card | `FormCard`, `Modifier.cardSurface()` |
| Text input, dropdown, date (label above the field) | `AkilimoTextField`, `AkilimoDropdown`, `DateInputField` |
| Pick one option as a card | `RadioButtonRow` (optional `supportingText`) |
| Two to four exclusive choices | `SegmentedTabRow`, `BinaryToggleChips` |
| Status pill | `StatusChip`, `StatusTone` |
| Key figures / progress at the top | `StatSummaryCard`, `ProgressSummaryCard` |
| Icon on a soft tile for rows | `IconTile` |
| Save / confirm bar | `SaveBottomBar` |
| Multi-step flow bar | `WizardBottomBar` |
| Guidance or notice | `InfoCard` |

- [ ] A new component is added only when none of these fits, and it takes theme tokens only.
- [ ] No duplicated card styling: use `FormCard` or `cardSurface()`.

## 4. New screen checklist

- [ ] Compose only (the app has no fragments or XML layouts).
- [ ] One ViewModel with a nested `UiState`, `StateFlow`, Hilt `@HiltViewModel`; no `LiveData`.
- [ ] Route added in `Route.kt` and registered in `FeatureGraphs.kt`.
- [ ] Opted in to `ExperimentalMaterial3Api` if it uses `BackTopAppBar`, `ModalBottomSheet` or `DatePicker`.
- [ ] Forward navigation uses `navigateIfResumed` so a double tap cannot push twice.
- [ ] Loading, empty, error and offline states exist and read well.
- [ ] Save is disabled with a reason shown, not silently greyed out.

## 5. Readability and accessibility

- [ ] Body text is 16sp or larger; secondary text is not smaller than 14sp.
- [ ] Sizes are in `sp` so the phone's font size applies; check at 200% font scale.
- [ ] Long labels wrap instead of being cut off; equal-size controls stay equal when a label wraps.
- [ ] Contrast of at least 4.5:1 for text (3:1 for large text and icons), in light and dark.
- [ ] Tap targets are at least 48dp.
- [ ] TalkBack reads each control once, with its role and state (selected, checked, error).
- [ ] Visible labels are not read twice (the field carries the name; the label text is cleared).
- [ ] Decorative icons have a null content description; icon-only buttons have a description.

## 6. Orientation and large screens

- [ ] Content stays readable in landscape: cap the line width (about 600dp) and keep the title above the text.
- [ ] The title or key figure is never hidden by the keyboard (`imePadding`, scrollable form).
- [ ] If a screen forces an orientation, it restores portrait and rotation when the user leaves.
- [ ] Tablet and fold layouts checked, or noted as not supported.

## 7. Text and languages

- [ ] Every user-facing string is in `strings*.xml`; none is built in code.
- [ ] No crop name in generic text (say "farm", not "cassava farm").
- [ ] New strings added in English, with a note if sw/rw/ha translations are still pending.
- [ ] Numbers and currency are formatted for the current locale.
- [ ] Layout checked with the longest translation, not only English.
- [ ] Language tags are BCP-47 and written to `AppSettingsDataStore` before applying locales.

## 8. Offline, data and errors

- [ ] Reads work offline from Room; lists support pull to refresh where data is synced.
- [ ] Errors say what happened and what to do, in the farmer's language; no raw server text.
- [ ] Schema changes come with a Room `Migration`, a version bump and the exported schema.

## 9. Before you open a PR

- [ ] `./gradlew :app:compileDebugKotlin`
- [ ] `./gradlew testDebugUnitTest`
- [ ] `./gradlew detekt` (and `lintDebug` when resources change)
- [ ] Checked on a phone in light and dark, portrait and landscape.
- [ ] Walked the first-run flow (legal, onboarding, hub) and a normal launch (hub).
- [ ] No hard-coded colours or sizes, no unused imports, no dead components.
- [ ] Commit messages follow the repo style; no tool co-author lines.

## 10. Where things stand

Done on the UI design work:

- [x] Logo palette and 12dp shapes applied app-wide; light theme by default.
- [x] Recommendation result: readable text, text-size and landscape controls, hidden bars for space.
- [x] Home hub with Recommendations, Profile, Your farm and Settings cards.
- [x] Standalone Profile and Your farm screens (one shared editor); onboarding is first-run only; Settings has no profile entry.
- [x] Steps screen: progress summary and raised step cards with status chips.
- [x] Option cards, segmented market choice, label-above fields, grouped form cards.
- [x] Settings as grouped cards; dates and onboarding sections on cards.
- [x] Settings: Light / System / Dark selector. (The app-wide Text size selector is deferred; see
      "Open decisions".)
- [x] Fertilizer list: search box and category bar (All, Single nutrient, Compound, Other), only for lists over 10 items.
- [x] Dates: planting picker opens on today; each date field shows its allowed window.
- [x] Shared loading, empty and error look: skeleton placeholder rows, and one centred message with a soft icon, hint and Retry.
- [x] Hub sync/offline status strip: offline, updating, last update time, and Retry after a failed update.

Open decisions and known gaps:

- [x] Profile holds About you only. Your farm holds Country, Location, Field size and Investment preference
      (the engine's risk attitude, named the same in the MCP agent). Planting and harvest dates are edited
      in the planting and harvest step; tillage and weed control in their cost steps (the wizard still
      asks for all of them on first run).
- [ ] App-wide text size (Normal / Large / Largest in Settings): **DEFERRED** (decided 2026-11-12)
      until users ask for it. Before release decide whether to keep or remove the first version that
      is already on `feat/ui-design-system` (a `textScale` in `AkilimoTheme`, the Settings selector,
      and the recommendation result screen no longer scaling its own text). If it stays deferred,
      keep the result screen's own size control as in #559 and do not add a second scale.
- [ ] Translations (sw, rw, ha) for the new hub, profile, text-size and progress strings (to be done later).
- [x] "Total weeding cost" summary row on the weed control form.

## 11. Backlog ideas

Quick wins:
- [ ] App-wide text size setting in Settings (Normal, Large, Largest; applies through the theme).
      DEFERRED until users ask for it (2026-11-12); see "Open decisions".
- [ ] Hub greeting with the farmer's name (skipped: the welcome text was removed from the hub header).
- [x] Fertilizer category filter and search (shown only when the list has more than 10 items).
- [x] Planting date picker starting at today, with a plain note on the allowed window.

Medium:
- [x] Sync and offline status strip (sync-state source: `SyncStatusSource`).
- [ ] Save, share and print a recommendation; list of past recommendations, readable offline.
- [x] Location picker opens on the chosen country (not Nairobi) in the manual flow and no longer jumps to the phone's GPS. (The Location step already had "Use current location".)
- [x] Location step: plain one-line reason, a "Where is your farm?" title, and an inside/outside status chip.
- [x] Skeleton loaders and one shared look for empty and error states (StateMessage, SkeletonList;
      list screens still have no error state: their view models do not report load failures).

Larger or needs a decision:
- [ ] Read-aloud (text to speech) for advice; icons plus short words on key actions. - SKIP
- [ ] Shorter first run: ask only what a first recommendation needs, collect the rest later.
- [ ] Landscape and tablet layouts on every screen. - SKIP
- [ ] Full TalkBack, 200% font and dark-mode accessibility pass.  - SKIP
