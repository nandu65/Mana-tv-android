# M3U Player - Android TV (V2 Native Application)

**M3U Player TV** is a native Android TV application built with **Kotlin**, **Jetpack Compose for TV**, and **AndroidX Media3 (ExoPlayer)**. It is designed specifically for 10-foot TV viewing and Android TV remote / D-pad navigation.

---

## Features

- 📺 **Native AndroidX Media3 / ExoPlayer**: Plays `.m3u8` live HLS streams, direct MP4, WebM, and other supported media streams directly on TV without browser CORS restrictions.
- 🎮 **100% Remote / D-Pad Friendly**: Fully navigable using standard Android TV remote controls (Up/Down for instant channel switching, Center/Select for on-screen controls, Back to return to browsing).
- 📁 **Multiple Playlists Support**: Easily add remote M3U URLs, local files, or test sample demo streams.
- 🗂️ **Dynamic Categories**: Automatically groups channels based on `#EXTINF` metadata and displays channel counts.
- ⭐ **Favorites & Continue Watching**: Mark favorite channels and access recently watched streams directly from the home screen.
- 🔍 **On-Screen Search**: Instant filtering by channel name, category, TVG ID, or country.
- 🔒 **Local-First & Private**: Powered by Room Database. No cloud accounts, no backend, no analytics.

---

## Tech Stack

- **Language**: Kotlin 2.0
- **UI Toolkit**: Jetpack Compose + Compose for TV
- **Media Engine**: AndroidX Media3 (ExoPlayer 1.5.1 + HLS module)
- **Local Persistence**: AndroidX Room 2.6.1 + KSP
- **Networking**: OkHttp 4.12
- **Image Loading**: Coil 2.7

---

## Building the APK

### Prerequisites
- JDK 17+ (Microsoft OpenJDK 17 or Eclipse Temurin)
- Android SDK (API 35, Build-Tools 35.0.0)

### Build Command
From the `android-tv` directory:

```bash
# On Windows:
.\gradlew.bat assembleDebug

# On Linux / macOS:
./gradlew assembleDebug
```

The compiled APK will be generated at:
```
android-tv/app/build/outputs/apk/debug/app-debug.apk
```

---

## Installing on Android TV via ADB

1. **Enable Developer Options & USB / Network Debugging on your Android TV**:
   - Go to **Settings > Device Preferences > About > Build** and click **Build** 7 times.
   - Go to **Settings > Device Preferences > Developer options** and turn on **Network debugging** (or USB debugging).
   - Note your TV's IP address (e.g., `192.168.1.100`).

2. **Connect via ADB from your PC**:
   ```bash
   adb connect 192.168.1.100:5555
   ```

3. **Install the APK**:
   ```bash
   adb install -r android-tv/app/build/outputs/apk/debug/app-debug.apk
   ```

4. **Launch on Android TV**:
   The application will appear with its TV banner and icon directly in your Android TV Home launcher.

---

## TV Remote Navigation Guide

| Button / Key | In Channel List | In Fullscreen Video Player |
| :--- | :--- | :--- |
| **D-Pad Up** | Move focus up | Switch to **Previous Channel** |
| **D-Pad Down** | Move focus down | Switch to **Next Channel** |
| **D-Pad Left / Right** | Navigate sidebar / channel cards | - |
| **Center / OK / Select** | Play selected channel | Toggle on-screen controls & metadata overlay |
| **Play / Pause** | - | Toggle stream playback |
| **Back** | Exit / Previous section | Return to Channel Browser |
