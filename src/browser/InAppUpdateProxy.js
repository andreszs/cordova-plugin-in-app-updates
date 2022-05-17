"use strict";

var InAppUpdate = {
	getUpdateAvailability: function (successCallback, failureCallback) {
		successCallback('UPDATE_NOT_AVAILABLE');
	},
	setSnackbarOptions: function (successCallback, failureCallback, snackbarText, snackbarButton, snackbarButtonColor) {
		successCallback('SUCCESS');
	},
	updateFlexible: function (successCallback, failureCallback) {
		successCallback('UPDATE_NOT_AVAILABLE');
	},
	updateImmediate: function (successCallback, failureCallback) {
		successCallback('UPDATE_NOT_AVAILABLE');
	}
};

module.exports = InAppUpdate;
require("cordova/exec/proxy").add("InAppUpdate", module.exports);
