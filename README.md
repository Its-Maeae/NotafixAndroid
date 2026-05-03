# Notafix Android

Android wrapper app for [Notafix](https://notafix.mooo.com) — a grade calculator for German schools. Built with [Capacitor](https://capacitorjs.com/) as a native WebView app.

## Features

- 📊 Grade tracking for 1st and 2nd semester + final grade
- 📈 Grade statistics with bar chart
- 🔒 Biometric / PIN lock screen
- 🔄 Offline sync with automatic background synchronization
- 🌙 Dark mode support

## Requirements

- [Node.js](https://nodejs.org/) v18+
- [Android Studio](https://developer.android.com/studio) (latest stable)
- Java 17 or 21
- A running Notafix server (see [notafix-server](https://github.com/yourusername/notafix-server))

## Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/yourusername/notafix-android.git
cd notafix-android
```

### 2. Install dependencies

```bash
npm install
```

### 3. Configure server URL

Edit `capacitor.config.json` and set your server URL:

```json
{
  "server": {
    "url": "https://your-server-url.com"
  }
}
```

### 4. Sync Capacitor

```bash
npx cap sync
```

### 5. Open in Android Studio

Open the `android/` folder in Android Studio:

**File → Open → select the `android/` folder → OK**

Wait for Gradle sync to complete.

### 6. Build APK

**Build → Build Bundle(s) / APK(s) → Build APK(s)**

The APK will be located at:
```
android/app/build/outputs/apk/debug/app-debug.apk
```

## Project Structure

```
notafix-android/
├── android/                  # Native Android project
│   └── app/
│       └── src/main/
│           ├── java/com/notafix/app/
│           │   ├── MainActivity.java      # Main activity + cookie persistence
│           │   └── NotafixPlugin.java     # Native plugin (biometrics, reload)
│           ├── AndroidManifest.xml
│           └── res/                       # App icons and resources
├── android-overrides/        # Capacitor config overrides
├── www/                      # Web assets (generated, not committed)
├── capacitor.config.json     # Capacitor configuration
└── package.json
```

## Native Features

### Biometric Lock
The app supports fingerprint and PIN/password locking via the native `NotafixPlugin`. Configure it under **Settings → Security** inside the app.

- Lock activates on cold start if enabled
- Automatically re-locks after **2 minutes** in the background

### Cookie Persistence
Login sessions are persisted across app restarts via `CookieManager.flush()`. You only need to log in once.

### Manual Refresh
Tap **Settings → Refresh** to sync pending offline changes and reload the app.

## Permissions

| Permission | Reason |
|---|---|
| `INTERNET` | Load the web app |
| `ACCESS_NETWORK_STATE` | Detect online/offline status |
| `USE_BIOMETRIC` | Fingerprint authentication |
| `USE_FINGERPRINT` | Legacy fingerprint support |

## Development Notes

After making changes to `capacitor.config.json` or adding Capacitor plugins, always run:

```bash
npx cap sync
```

Then do a Gradle sync in Android Studio before rebuilding.

## Troubleshooting

**Gradle sync fails** — Make sure you are using Java 17 or 21. Go to **File → Project Structure → SDK Location** and verify the JDK path.

**App shows blank screen** — Check that your server URL in `capacitor.config.json` is reachable and uses HTTPS.

**Biometrics not working** — The device must have a registered fingerprint or PIN. Check under Android Settings → Security.

**Old content showing** — Tap **Settings → Refresh** inside the app to clear the service worker cache.

## License

MIT
