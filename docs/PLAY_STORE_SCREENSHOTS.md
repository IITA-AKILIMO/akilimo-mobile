# Play Store screenshots: capture and upload

*IITA Internal Use*

How to refresh the AKILIMO Play Store listing screenshots. A Compose UI test drives the app on a device and takes the pictures; fastlane builds it, pulls the PNGs and uploads them. Nothing here changes the app itself.

## How it works

1. `StoreScreenshotsTest` (`app/src/androidTest/.../screenshots/`) starts the real app past onboarding.
2. `DemoData` first seeds a generic farmer (Jane Doe, "Doe Family Farm", Mwanza region) and sample Tanzania reference data (fertilizers, investment amounts, starch factories, cassava units with prices) and three inbox messages. A fresh install has none of this because the app downloads it from the API, so without it the forms would show empty states. None of it is real farmer data.
3. The test walks the screens in a fixed order and takes one screenshot per screen. It pauses before each capture so transitions and loading animations finish.
4. The `screenshots` lane builds the app and test APKs, runs the test for one language, and copies the PNGs off the device into `fastlane/screenshots/<locale>/`.
5. The `upload_screenshots` lane sends those folders to the Play listing.

### What gets captured

Files are numbered in capture order, so Play shows them in that order. The numbers and names come from the test.

| Screens | Notes |
|---|---|
| Hub, Recommendations | |
| Fertilizer, Best planting practices, Scheduled planting | Each advice type's step list, then every form behind it. A step shared by several advice types (markets, yield, dates) is captured once. |
| Profile, Farm | |
| Map picker | Opened from the Farm screen. Needs internet for the map tiles. |
| Messages, Settings | |

To change the set, edit `captureStoreScreenshots()` and `ADVICE_TOURS` in `StoreScreenshotsTest.kt`. `ADVICE_TOURS` mirrors the step lists in `navigation/FeatureGraphs.kt`. Advice types that need the network (results) or a specific country (intercropping, planting date and cultivar) are not in the tour.

### Languages

The language comes from the `testLocale` argument and is applied through the app's own language setting. The device locale is never changed, so a real phone works. Pass the Play locale code you want as the folder name: `en-GB`, `sw` or `rw`.

## Quick commands (PowerShell)

Load the short commands once per session from the repo root:

```powershell
. .\scripts\fastlane-aliases.ps1
```

To have them in every new PowerShell window, run `fl-install` once. It adds a small marked block to your PowerShell profile that loads the script from this checkout, and repeating it changes nothing. `fl-uninstall` removes the block again. If you move the repo, run `fl-uninstall` and then `fl-install` from the new location. The commands run from any folder.

| Command | What it does |
|---|---|
| `fl-help` | Prints this list in the terminal |
| `fl-install` / `fl-uninstall` | Loads the commands in every new window / removes that again |
| `fl-shots` | Captures `en-GB`, `sw` and `rw` in turn (about 6 minutes) |
| `fl-shots -Locale sw` | Captures one language |
| `fl-shots-en`, `fl-shots-sw`, `fl-shots-rw` | One language, nothing else to type |
| `fl-upload` | Uploads the 8 `store_shots` per language to Play |
| `fl-upload -DryRun` | Lists what would be sent, uploads nothing |
| `fl-notes -Code 123` | Corrects the release notes of version code 123 |
| `fl-notes -Code 123 -Track beta -DryRun` | Same on another track, checking the files only |

They call `bundle exec fastlane` for you, so the full commands in the sections below still work.

## One-time setup

You need:

- A Ruby install **with a C build toolchain**. Gems such as `io-console` compile native code and fail to install without one. On Windows, install the MSYS2 build tools (`ridk install`) or use the RubyInstaller "with Devkit" build.
- `adb` on your `PATH` (comes with the Android SDK platform tools).
- A device or emulator. A physical phone needs USB debugging on and must stay unlocked and awake during the run. In Developer options, set the window, transition and animator scales to off so screens are still when captured.

Then, from the repo root:

```powershell
bundle install
```

Check the device is visible:

```powershell
adb devices
```

With more than one device attached, choose one with the `ANDROID_SERIAL` environment variable.

## Capture

Run one language at a time. The lane clears that language's earlier images from the device, but keeps the other languages' folders on your machine.

```powershell
bundle exec fastlane screenshots locale:en-GB
bundle exec fastlane screenshots locale:sw
bundle exec fastlane screenshots locale:rw
```

Without `locale:` it defaults to `en-GB`. A run takes about two minutes, most of it the deliberate pauses and the map tiles.

Output goes to `fastlane/screenshots/<locale>/`, for example `fastlane/screenshots/sw/04_step_available_fertilizers.png`. That folder is git-ignored.

### Review before uploading

Look at the images. Typical things to check:

- The map picker shows loaded tiles and the pin on Mwanza. Raise `MAP_TILES_MS` in the test if tiles are still blank.
- No screen caught a transition. Raise `SETTLE_MS` if one did.
- Swahili and Kinyarwanda screens show translated text, not English fallbacks.
- The phone's own status bar and navigation bar are trimmed off by `PlayScreenshotStrategy`, so no clock, battery or notification icons show. The trimmed images are 1080 x 2160 (exactly 2:1) because Play rejects images whose long side is more than twice the short side, and a full 1080 x 2340 phone screen is over that. Anything still too tall is cut from the bottom, so a very bottom line such as the hub's "Updated ... ago" can be clipped.

The tour captures 18 screenshots per language for review, but Play shows at most 8. The upload sends only the ones named in `store_shots` at the top of `fastlane/Fastfile`, in that order: hub, recommendations, fertilizer advice, available fertilizers, cassava market, planting and harvest dates, map picker and settings. Edit that list to change the selection (the names are the capture names without the number, for example `step_tractor_access`). The lane refuses more than 8 entries, skips a name with no matching capture and warns, and needs at least 2 per language.

## Upload

The upload lane reads every locale folder in `fastlane/screenshots/` and sends only the `store_shots` selection from each, so check which locale folders are there first. List what would be sent without uploading by running `bundle exec fastlane upload_screenshots dry_run:true`. It uploads **only screenshots**: no app bundle, listing text or changelogs. It targets the production listing.

Credentials come from the Play Console service-account key. Locally, put the JSON file in the repo root as `akilimo-dst-tools-825f76f13f36-fastlane.json`; `fastlane/Appfile` picks it up and `.gitignore` keeps it out of git. Then:

```powershell
bundle exec fastlane upload_screenshots
```

To use a key somewhere else, set `SUPPLY_JSON_KEY` to its path, or `SUPPLY_JSON_KEY_DATA` to its JSON text; either one overrides the root file. In CI the key is the `GOOGLE_SERVICE_ACCOUNT` secret that `publish.yml` already uses, passed as `SUPPLY_JSON_KEY_DATA`.

The service account needs the "Manage store presence" permission. Never commit the key or paste it into chat, issues or PRs.

After uploading, send the store listing changes for review in Play Console. You can also upload by hand there under Main store listing, Phone screenshots, per language.

## Correcting release notes

New releases get their notes from `publish.yml`, which uploads `release/distribution/whatsnew/whatsnew-<locale>` together with the app bundle. To fix the notes of a release that is already out (a typo, or a missing language), use the `update_release_notes` lane. It attaches the notes to an existing version code and uploads no bundle, screenshots or listing text.

The lane needs the release's **version code**, a whole number such as `345`. The version name (`33.0.3`) will not work. The code is not derived from the tag: CI fetches it from the version endpoint, so look it up in Play Console under Release > App bundle explorer (the "Version code" column) or in the release's bundle details.

Edit the files in `release/distribution/whatsnew/` first. Play allows at most 500 characters per language, and the lane stops with the language name if a file is longer or empty. The file name suffix is the Play locale, so `whatsnew-en-GB` and `whatsnew-sw` exist today. Do not add a file for a language the Play listing does not have: Play rejects the whole update. Kinyarwanda (`rw`) is not available on Google Play, so there is no `whatsnew-rw` and no `rw` screenshots to upload (the `upload_screenshots` lane uploads every folder in `fastlane/screenshots/`, so delete `fastlane/screenshots/rw` first if you captured it).

Check the files without uploading anything:

```powershell
bundle exec fastlane update_release_notes version_code:123 dry_run:true
```

Then upload. `track:` defaults to `production`:

```powershell
bundle exec fastlane update_release_notes version_code:123
bundle exec fastlane update_release_notes version_code:123 track:beta
```

It uses the same service-account key as the screenshot upload. It has not been run against Play yet, so try it on a beta release first.

## CI

The manual workflow `.github/workflows/store-screenshots.yml` runs the capture for `en-GB`, `sw` and `rw` on an emulator and keeps the PNGs as a `store-screenshots` artifact. It uploads to Play only when the `upload` input is ticked, so capture and review the artifact first. It has not been run end to end yet; emulator images and timings may need adjusting.

## Troubleshooting

| Symptom | Cause and fix |
|---|---|
| `bundle install` fails building a gem | No C toolchain. Install the MSYS2 build tools (see setup). |
| `adb install ... failed` | No device, device locked, or the phone is waiting for an install confirmation. The error text from `adb` is printed. |
| Test process crashes with "WorkManager is not initialized" | `HiltTestRunner` must start WorkManager in `callApplicationOnCreate`. A leftover scheduled job otherwise starts `SystemJobService` before any test code runs. |
| "Screenshot test failed" with a "No node found" error | A screen label changed or is off screen. The tour taps by translated text; check the label and `ADVICE_TOURS`. |
| Forms show "Nothing to show" | `DemoData` is missing rows for that screen. Add them to `seedReferenceData`. |
| Blurry or half-drawn screens | Increase `SETTLE_MS`, and turn the phone's animation scales off. |
| Map is blank or says the supported area could not load | No internet on the device, or tiles had not finished. Raise `MAP_TILES_MS`. |
| Images are corrupt after capture | Do not pull with a text-mode pipe. The lane copies with `adb exec-out run-as ... cat` in binary mode; fastlane's own pull produced corrupt files on Windows. |
| Device locale does not change | Expected. The test never changes it; the app language is set through the app's own setting. |

## Files

| File | Role |
|---|---|
| `app/src/androidTest/.../screenshots/StoreScreenshotsTest.kt` | The tour and the pauses |
| `app/src/androidTest/.../screenshots/DemoData.kt` | Demo farmer, sample reference data and inbox messages |
| `app/src/androidTest/.../screenshots/PlayScreenshotStrategy.kt` | Takes the screenshot and trims the system bars to a 2:1 image |
| `app/src/androidTest/.../HiltTestRunner.kt` | Test runner; starts WorkManager |
| `fastlane/Fastfile` | `screenshots`, `shots`, `upload_screenshots` and `update_release_notes` lanes |
| `scripts/fastlane-aliases.ps1` | Short PowerShell commands (`fl-shots`, `fl-upload`, `fl-notes`, `fl-help`) |
| `.github/workflows/store-screenshots.yml` | Manual CI capture and optional upload |
| `Gemfile`, `Gemfile.lock` | Pinned fastlane version |
