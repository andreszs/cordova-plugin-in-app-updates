var exec = require('cordova/exec');

var InAppUpdate = {
	getUpdateAvailability: function (successCallback, failureCallback) {
		exec(successCallback, failureCallback, 'InAppUpdate', 'getUpdateAvailability');
	},
	setSnackbarOptions: function (successCallback, failureCallback, snackbarText, snackbarButton, snackbarButtonColor) {
		snackbarText = (typeof (snackbarText) == 'undefined' || snackbarText.length == 0) ? 'An update has just been downloaded.' : snackbarText;
		snackbarButton = (typeof (snackbarButton) == 'undefined' || snackbarButton.length == 0) ? 'RESTART' : snackbarButton;
		snackbarButtonColor = (typeof (snackbarButtonColor) == 'undefined' || snackbarButtonColor.length == 0) ? '#76FF03' : snackbarButtonColor;
		exec(successCallback, failureCallback, 'InAppUpdate', 'setSnackbarOptions', [
			{strText: snackbarText, strButton: snackbarButton, strButtonColor: snackbarButtonColor}
		]);
	},
	updateFlexible: function (successCallback, failureCallback) {
		exec(successCallback, failureCallback, 'InAppUpdate', 'updateFlexible');
	},
	updateImmediate: function (successCallback, failureCallback) {
		exec(successCallback, failureCallback, 'InAppUpdate', 'updateImmediate');
	}

};

module.exports = InAppUpdate;

