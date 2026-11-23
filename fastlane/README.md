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

### android upload_screenshots

```sh
[bundle exec] fastlane android upload_screenshots
```

Upload fastlane/screenshots/<locale>/*.png to the Play Store listing (no AAB, metadata or changelogs)

----

This README.md is auto-generated and will be re-generated every time [_fastlane_](https://fastlane.tools) is run.

More information about _fastlane_ can be found on [fastlane.tools](https://fastlane.tools).

The documentation of _fastlane_ can be found on [docs.fastlane.tools](https://docs.fastlane.tools).
