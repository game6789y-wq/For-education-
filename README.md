# REALITY Local Edition

An independent, offline-first Android starter app for private, on-device features. This project is **not an official REALITY client** and is not affiliated with Wright Flyer Live Entertainment.

## Features

- **Offline by design** — the app manifest does not request Internet access.
- **Local chat journal** — save private notes in an app-private SQLite database; notes are not sent to REALITY or other users.
- **Local profile** — edit a nickname and bio stored on this device.
- **Local Super Admin** — create an owner PIN to unlock local-only controls. The PIN is salted and derived with PBKDF2-HMAC-SHA256.
- **No automatic backup** — Android app backup is disabled for this starter.

## Important limitations

This is a separate local utility, not a replacement for the REALITY service. It does not provide REALITY sign-in, cross-user messaging, live streaming, video calls, purchases, push notifications, remote moderation, or server-side administrator privileges. Those features require authorized service APIs and backend infrastructure.

## Build an APK with GitHub Actions

1. Open the **Actions** tab in this repository.
2. Select **Android Debug APK**.
3. Choose **Run workflow** on the `main` branch (or let a push to app/build files trigger it).
4. Wait for the workflow to finish successfully.
5. Open the completed run and download the `reality-local-edition-debug` artifact.

The workflow builds a **debug-signed APK for testing**. It does not publish a GitHub Release.

## Build locally

Requirements: JDK 17, Android SDK Platform 35, and Gradle 8.9 or compatible.

Run:

```bash
gradle :app:assembleDebug
```

APK output:

```
app/build/outputs/apk/debug/app-debug.apk
```

## App details

| Item | Value |
|---|---|
| Application ID | `com.example.realitylocal` |
| Version | `0.1.0 (1)` |
| Minimum Android | Android 9 (API 28) |
| Compile / Target SDK | 35 / 35 |
| Language / UI | Kotlin + Jetpack Compose |
| Local storage | SQLite |

## Privacy

The app is designed to keep profile data, notes, and local admin settings inside its private app storage. Uninstalling the app or clearing its data removes that local information. Do not use the local PIN as a REALITY account password.

---

*Independent community project. REALITY is a trademark of its respective owner.*
