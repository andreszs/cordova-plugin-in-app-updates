# Changelog

## Unreleased

## 2.3.0 - 2026-08-27

- Replaced the Material Snackbar used after a flexible update download with a native Android `AlertDialog`.
- Removed the explicit Material Components and AppCompat dependencies.
- Preserved the existing JavaScript `setSnackbarOptions()` API; its values now configure the completion dialog.
- Restored compatibility with cordova-android 11 or later.

## 2.2.0 - 2023-09-01

- Optimized for android-compileSdkVersion 33.
- Set the minimum cordova-android version to 12.
- Updated androidx.appcompat:appcompat to 1.6.1.
- Updated com.google.android.material:material to 1.8.0.

## 2.1.0 - 2023-08-31

- Optimized for android-compileSdkVersion 32.
- Set the minimum cordova-android version to 11.
- Replaced com.google.android.play:core with the standalone [Play In-App Updates Library](https://developer.android.com/reference/com/google/android/play/core/release-notes-in_app_updates) 2.1.0.
- Updated androidx.appcompat:appcompat to 1.5.1.
- Updated com.google.android.material:material to 1.3.0.
- Removed the `edit-config` rule that set `android:theme="@style/Theme.AppCompat.NoActionBar"`.
- Tested on Android 5.1, 8.1, and 10.

## 2.0.5 - 2022-05-17

- Optimized for android-compileSdkVersion 30.
- Updated androidx.appcompat:appcompat to 1.3.0.
- Updated com.google.android.play:core to 1.8.0.
- Updated com.google.android.material:material to 1.2.0.
