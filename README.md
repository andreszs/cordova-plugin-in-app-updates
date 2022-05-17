
 ![License](https://img.shields.io/badge/license-MIT-orange.svg) ![Platform](https://img.shields.io/badge/platform-android-green.svg) [![Donate](https://img.shields.io/badge/donate-PayPal-green.svg)](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=G33QACCVKYD7U)

# cordova-plugin-in-app-updates

Cordova Android plugin for checking for updates and auto updating app with Google Play Store In-App updates API.

# Platforms

- Android
- Browser

# Features

- AndroidX ready (plugin version 2.0.x)
- Checks for updates
- Install flexible updates
- Install immediate updates
- Customize snackbar message and button for Flexible updates
- Customize snackbar button color for Flexible updates

# Installation

## Version Requirements

| Plugin version | Cordova CLI | Cordova Android | AndroidX support |
| --- | --- | --- | --- |
| 1.0.6 | 9.0.0 | 8.0.0 | No |
| 2.0.5 | 10.0.0 | 9.0.0 | Yes |

## Install latest version via CLI

```bash
  cordova plugin add cordova-plugin-in-app-updates
```

## Install version 1.x

```bash
  cordova plugin add cordova-plugin-in-app-updates@1.0.6
```

# Methods

## getUpdateAvailability

Invokes the AppUpdateManager and return one of the [updateAvailability](https://developer.android.com/reference/com/google/android/play/core/install/model/UpdateAvailability.html) codes as string.

### Return values

- **UPDATE_AVAILABLE**
- **UPDATE_NOT_AVAILABLE**
- **DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS**
- **UNKNOWN**

When this method returns **UPDATE_AVAILABLE**, your app is ready to use the following methods to prompt the user for update.

### Example

 ```javascript
var onSuccess = function (strSuccess) {
    console.log(strSuccess);
};
var onFailure = function (strError) {
    console.warn(strError);
};
cordova.plugins.InAppUpdate.getUpdateAvailability(onSuccess, onFailure);
```

## updateFlexible

Starts a [flexible update](https://developer.android.com/guide/playcore/in-app-updates/kotlin-java#flexible "flexible update") process and prompts the user with a dialog to download the new version now or when Wi-Fi is available.

:warning: The **successCallback** from this method can be triggered repeatedly according to its status.

### Return values

- **UPDATE_NOT_AVAILABLE**: No updates available in Play Store.
- **DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS**: Either flexible or immadiate update is in progress.
- **UPDATE_PROMPT**: The user has been presented with the Play Store dialog to download or ignore the flexible update.
- **RESULT_OK**: User accepted to download flexible update.
- **RESULT_CANCELED**: User declined the flexible update dialog or update was aborted while in progress.
- **RESULT_IN_APP_UPDATE_FAILED**: Something went wrong with the update dialog response.
- **ACTIVITY_RESULT_UNKNOWN**: Unknown result code returned by the dialog.
- **DOWNLOADING**: An update is currently being downloaded in the background.
- **DOWNLOADED**: The update was downloaded and the snackbar with RESTART button has been shown.

### Example

```javascript
var onSuccess = function (strSuccess) {
    console.log(strSuccess);
};
var onFailure = function (strError) {
    console.warn(strError);
};
cordova.plugins.InAppUpdate.updateFlexible(onSuccess, onFailure);
```

:warning: Make sure to call **getUpdateAvailability** as often as needed to ensure there are no **flexible** updates downloaded pending install, as they will consume storage space until installed.

## updateImmediate

Starts an [immediate update](https://developer.android.com/guide/playcore/in-app-updates/kotlin-java#immediate "immediate update") process and prompts the user with a fullscreen dialog to download now or when Wi-Fi is available. The update is downloaded and installed in the foreground, preventing the user from interacting with your app until the installation succeeds and the app is automatically restarted.

:warning: The **successCallback** from this method can be triggered repeatedly according to its status.

### Return values

- **UPDATE_NOT_AVAILABLE**: No updates available in Play Store.
- **DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS**: Either flexible or immadiate update is in progress.
- **RESULT_OK**: User accepted to download immediate update.
- **RESULT_CANCELED**: User declined the immediate update dialog.
- **RESULT_IN_APP_UPDATE_FAILED**: Something went wrong with the update dialog response.
- **ACTIVITY_RESULT_UNKNOWN**: Unknown result code returned by the dialog.
- **DOWNLOADING**: An update is currently being downloaded in the foreground.
- **DOWNLOADED**: The update was downloaded and will be installed immediately.

### Example

```javascript
var onSuccess = function (strSuccess) {
    console.log(strSuccess);
};
var onFailure = function (strError) {
    console.warn(strError);
};
cordova.plugins.InAppUpdate.updateImmediate(onSuccess, onFailure);
```

## setSnackbarOptions

Sets the label and the button text for the snackbar shown after downloading a flexible update. You are free to call this method at any time. You can also call it again to show different snackbar messages after the snackbar was shown.

If not called, default messages in English will be shown.

### Example

```javascript
var onSuccess = function (strSuccess) {
    console.log(strSuccess);
};
var onFailure = function (strError) {
    console.warn(strError);
};
var snackbarText = "An update has just been downloaded.";
var snackbarButton = "RESTART";
var snackbarButtonColor = "#76FF03";
cordova.plugins.InAppUpdate.setSnackbarOptions(onSuccess, onFailure, snackbarText, snackbarButton, snackbarButtonColor);
```

### Default messages when not called

- **snackbarText** = "An update has just been downloaded."
- **snackbarButton** = "RESTART"
- **snackbarText** = "#76FF03"

### Return values

- **onSuccess** = "SUCCESS"
- **onFail** = *Internal error description.*

# Plugin demo app

Get the [In-App Update Plugin Demo app](https://www.andreszsogon.com/cordova-in-app-update-plugin-demo-app/ "In-App Update Plugin Demo app") to test the plugin in all possible scenarios. This app is available in the Store, please follow the linked  instructions to test the update flow.

# Reporting issues

Please report any issue with this plugin in GitHub by providing detailed context and sample code.