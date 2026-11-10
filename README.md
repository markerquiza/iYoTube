# IyoTube for Rokid AI Glasses

IyoTube plays YouTube links and direct media URLs on the green-display Rokid AI Glasses. Use **Wi-Fi settings** to connect the glasses, then choose **Search or open link** to enter a title or URL. Playback fills the screen and preserves the video proportions. Press **Back** to return to the menu.

See the [installation guide](docs/INSTALL_IPHONE.md) for steps to install the APK using Hi Rokid on an iPhone.

Voice search uses the Android speech recognizer when the glasses provide one.

## Build
Open this directory in Android Studio, install Android SDK 35 and Build Tools 35.0.0, and select JDK 17 and Gradle 8.11.1. The Gradle wrapper is bundled.

```sh
./gradlew testDebugUnitTest lintDebug assembleDebug
```

Output: `app/build/outputs/apk/debug/app-debug.apk`.

The GitHub Actions workflow builds, tests and uploads a debug APK on push or manual dispatch. Production signing is not configured.
