fastlane documentation
----

# Installation

Make sure you have the latest version of the Xcode command line tools installed:

```sh
xcode-select --install
```

For _fastlane_ installation instructions, see [Installing _fastlane_](https://docs.fastlane.tools/#installing-fastlane)

# Available Actions

## Android

### android screenshots

```sh
[bundle exec] fastlane android screenshots
```

Capture Play Store screenshots on the connected device in one language: fastlane screenshots locale:sw

### android update_release_notes

```sh
[bundle exec] fastlane android update_release_notes
```

Correct the release notes of a published release: fastlane update_release_notes version_code:123 [track:production] [dry_run:true]

### android upload_screenshots

```sh
[bundle exec] fastlane android upload_screenshots
```

Upload the store_shots (max 8) from fastlane/screenshots/<locale>/ to the Play listing; dry_run:true only lists them

----

This README.md is auto-generated and will be re-generated every time [_fastlane_](https://fastlane.tools) is run.

More information about _fastlane_ can be found on [fastlane.tools](https://fastlane.tools).

The documentation of _fastlane_ can be found on [docs.fastlane.tools](https://docs.fastlane.tools).
