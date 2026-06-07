# PureRacing

PureRacing is an unofficial racing news and results client for Android and Windows Desktop. It is built with Compose Multiplatform and uses the public RacingDaily/PureRacing API endpoints documented in `origin/reverses`.

The app focuses on read-only browsing. Login, registration, comments, and user chat features are intentionally not included.

## Features

- News feed with category tabs, article detail pages, media rendering, and system sharing.
- Race calendar with Grand Prix details, session start times, session results, weather, and track links.
- Driver and team rankings with profile/detail entry points.
- Track pages with circuit information and historical records.
- More section for custom championships, MotoGP, TCR, and app information.
- Liquid glass UI based on Kyant0 AndroidLiquidGlass / Backdrop.
- Cross-platform targets:
  - Android APK
  - Windows Desktop distributable

## Tech Stack

- Kotlin Multiplatform
- Compose Multiplatform
- Ktor Client
- kotlinx.serialization
- Coil 3
- Kyant Backdrop `2.0.0-alpha03`
- Android Activity Compose
- SWT Browser on Windows Desktop for article HTML rendering

## API

Base URL:

```text
https://api.romielf.com
```

The API generally returns:

```json
{
  "code": 200,
  "msg": "success",
  "data": {}
}
```

Article media is loaded with the required referer:

```text
news.romielf.com
```

The reverse-engineering notes are kept under:

```text
origin/reverses/
```

## Project Structure

```text
composeApp/
  src/
    commonMain/     Shared Compose UI, models, API service, platform contracts
    androidMain/    Android activity, WebView, share/back handlers, Android glass components
    desktopMain/    Windows Desktop entrypoint, SWT article view, desktop handlers
gradle/
  libs.versions.toml
.github/
  workflows/build.yml
```

## Build

This repository is mainly built through GitHub Actions.

The workflow builds:

- `racingdaily-android-release`: signed Android release APK
- `racingdaily-windows`: Windows Desktop distributable

After pushing to `main`, check the build with:

```bash
gh run list --branch main --limit 5
gh run view <run-id>
```

Artifacts can be downloaded from the successful workflow run page.

## Local Development

If you want to run Gradle locally, install a suitable JDK first.

Android:

```bash
./gradlew :composeApp:assembleDebug
```

Desktop:

```bash
./gradlew :composeApp:run
```

Windows distributable:

```bash
./gradlew :composeApp:createDistributable
```

## Third-Party Code

PureRacing includes adapted source from Kyant0 AndroidLiquidGlass catalog examples and depends on Kyant Backdrop libraries. See:

```text
THIRD_PARTY_NOTICES.md
```

## License

This project is licensed under the MIT License. See `LICENSE` for details.

PureRacing is an unofficial third-party client and is not affiliated with RacingDaily or the upstream API provider.
