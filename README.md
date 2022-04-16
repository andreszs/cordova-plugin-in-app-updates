
| License | Platform | Contribute |
| --- | --- | --- |
| ![License](https://img.shields.io/badge/license-MIT-orange.svg) | ![Platform](https://img.shields.io/badge/platform-android-green.svg) | [![Donate](https://img.shields.io/badge/donate-PayPal-green.svg)](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=G33QACCVKYD7U) |

# cordova-plugin-in-app-update

Cordova Android plugin for checking for updates and auto updating app with Google Play Store In-App updates API.

## Features

- Checks for updates
- Flexible updates
- Immediate updates
- Customize snackbar message and button for Flexible updates
- Customize snackbar button color for Flexible updates


## Installation

Add plugin to your Cordova app:

```bash
  cordova plugin add https://github.com/andreszs/cordova-plugin-in-app-update
```
    
## Methods

### Check for update availability

```javascript
cordova.plugins.InAppUpdate.getUpdateAvailability(successCallback, failureCallback);
```

Returns one of the [updateAvailability](https://developer.android.com/reference/com/google/android/play/core/install/model/UpdateAvailability.html) codes as string:
- DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
- UNKNOWN
- UPDATE_AVAILABLE
- UPDATE_NOT_AVAILABLE

### Start flexible update

```javascript
cordova.plugins.InAppUpdate.updateFlexible(successCallback, failureCallback);
```
Checks for update and show full-screen dialog to download and install it now or when WiFi is available.

### Start immediate update

```javascript
cordova.plugins.InAppUpdate.updateImmediate(successCallback, failureCallback);
```
Checks for an update and shows the dialog to download it now or when WiFi is available.

Upon complete download, a snackbar message with a button to install it will be shown.


Once downloaded, the ``getUpdateAvailability`` method will return ``DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS``.
If the user does not click the INSTALL button in the action bar, you can recall this ``updateImmediate``  method and the snackbar will be immediately shown to install the downloaded update.

### Set snackbar options for flexible update downloaded

```javascript
cordova.plugins.InAppUpdate.setSnackbarOptions(successCallback, failureCallback, snackbarText, snackbarButton, snackbarButtonColor);
```

Set snackbar status message, button caption and button color for completed Flexible updates.

If not set, these defaults will be applied:

- snackbarText = ``"An update has just been downloaded.";``
- snackbarButton = ``"RESTART";``
- snackbarText = ``"#76FF03";``

You can invoke this method at any moment and your Flexible update completion will show the snackbar with your settings.
## Usage/Examples

### getUpdateAvailability

```javascript
var onSuccess = function (strSuccess) {
    console.log(strSuccess);
};
var onFailure = function (strError) {
    console.warn(strError);
};
cordova.plugins.InAppUpdate.getUpdateAvailability(onSuccess, onFailure);
```

### updateFlexible

```javascript
var onSuccess = function (strSuccess) {
    console.log(strSuccess);
};
var onFailure = function (strError) {
    console.warn(strError);
};
cordova.plugins.InAppUpdate.updateFlexible(onSuccess, onFailure);
```

### updateImmediate

```javascript
var onSuccess = function (strSuccess) {
    console.log(strSuccess);
};
var onFailure = function (strError) {
    console.warn(strError);
};
cordova.plugins.InAppUpdate.updateImmediate(onSuccess, onFailure);
```

### setSnackbarOptions

Notice this method is not mandatory, default (english) strings will be shown if now called.

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


## Plugin Demo App

Demo app to be released to Open Testing in the Play Store soon, please check back later.

