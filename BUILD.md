# Building and Packaging TAJ EGY

## 1. Prerequisites
- **Android Studio**: Ladybug (2024.2+) or Meerkat (2024.3+)
- **Android SDK**: Platform 36 (Android 16), Platform Tools, Build Tools 36.0.0+
- **JDK**: Java 17 or Java 21 (configured in Android Studio > Settings > Build Tools > Gradle > Gradle JDK)
- **Minimum Device SDK**: Android 7.0 (API Level 24)

## 2. Opening & Building in Android Studio
1. Clone or extract this repository to your local computer.
2. Launch Android Studio and click **File > Open...**.
3. Select the repository root directory.
4. Allow Gradle to sync dependencies automatically.
5. Select the `app` target from the run configurations dropdown.
6. Click **Run** (`Shift + F10`) to deploy the app to an emulator or physical Android device.

## 3. Command-Line Builds
Run from the root of the project:

- **Build Debug APK**:
  ```bash
  gradle :app:assembleDebug
  ```
  Output APK location:
  `app/build/outputs/apk/debug/app-debug.apk`

- **Run Unit Tests**:
  ```bash
  gradle :app:testDebugUnitTest
  ```

- **Build Release APK**:
  ```bash
  gradle :app:assembleRelease
  ```
  Output APK location:
  `app/build/outputs/apk/release/app-release.apk`

- **Build Google Play Release Bundle (AAB)**:
  ```bash
  gradle :app:bundleRelease
  ```
  Output AAB location:
  `app/build/outputs/bundle/release/app-release.aab`

## 4. Configuring Your Private Release Keystore
In compliance with open-source security standards, private production signing keys and passwords are not included in the repository.

### Option A: Using Environment Variables (Recommended for CI/CD)
Set the following environment variables before invoking Gradle:
```bash
export KEYSTORE_PATH="/path/to/your/upload-keystore.jks"
export STORE_PASSWORD="your-keystore-password"
export KEY_ALIAS="your-key-alias"
export KEY_PASSWORD="your-key-password"

gradle :app:assembleRelease
```

### Option B: Generating and Signing via Android Studio GUI
1. In Android Studio, navigate to **Build > Generate Signed Bundle / APK...**.
2. Select **APK** or **Android App Bundle**.
3. Click **Create new...** or select your existing `.jks` or `.keystore` file.
4. Enter your key alias and password.
5. Choose `release` build variant and select V1 and V2 signature options.
6. Click **Finish**.

### Development Fallback
If `KEYSTORE_PATH` is not set or the file is absent, Gradle automatically signs the release output with the local debug key so developers can test release compilation immediately without setup blockers.
