package com.andreszs.inappupdate;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;

import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.InstallState;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.google.android.play.core.tasks.Task;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static com.google.android.play.core.install.model.ActivityResult.RESULT_IN_APP_UPDATE_FAILED;

/**
 * @author https://github.com/andreszs/cordova-plugin-in-app-update
 */
public class InAppUpdate extends CordovaPlugin {

	private static String LOG_TAG;
	private static boolean PROMPTED = false;
	private static int currentUpdateType;
	private static AppUpdateManager appUpdateManager;

	private final int activityResultRequestCode = 777;
	private CordovaPlugin activityResultCallback;
	private FrameLayout layout;
	private Snackbar snackbar;
	private CallbackContext callbackContext;

	private String snackbarText = "An update has just been downloaded.";
	private String snackbarButton = "RESTART";
	private String snackbarButtonColor = "#76FF03";

	public InAppUpdate() {
		this.LOG_TAG = InAppUpdate.class.getSimpleName();
		Log.i(LOG_TAG, "Constructed");
	}

	@Override
	public void initialize(CordovaInterface cordova, CordovaWebView webView) {
		super.initialize(cordova, webView);
		layout = (FrameLayout) webView.getView().getParent();
	}

	@Override
	public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
		this.callbackContext = callbackContext;
		this.PROMPTED = false;

		Log.i(LOG_TAG, String.format("Executing the %s action started", action));
		if (action.equals("getUpdateAvailability")) {
			getUpdateAvailability(callbackContext);
			return true;
		} else if (action.equals("updateFlexible")) {
			updateFlexible();
			return true;
		} else if (action.equals("updateImmediate")) {
			updateImmediate();
			return true;
		} else if (action.equals("setSnackbarOptions")) {
			setSnackbarOptions(args);
			return true;
		}

		return false;
	}

	private void getUpdateAvailability(CallbackContext callbackContext) {

		cordova.getThreadPool().execute(new Runnable() {
			public void run() {

				// Creates instance of the manager.
				appUpdateManager = AppUpdateManagerFactory.create(cordova.getContext());

				// Returns an intent object that you use to check for an update.
				Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();

				appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
					Log.i(LOG_TAG, String.format("appUpdateInfo.updateAvailability = %s", String.valueOf(appUpdateInfo.updateAvailability())));
					PluginResult result;
					switch (appUpdateInfo.updateAvailability()) {
						case UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS:
							result = new PluginResult(PluginResult.Status.OK, "DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS");
							callbackContext.sendPluginResult(result);
							break;

						case UpdateAvailability.UPDATE_AVAILABLE:
							result = new PluginResult(PluginResult.Status.OK, "UPDATE_AVAILABLE");
							callbackContext.sendPluginResult(result);
							break;

						case UpdateAvailability.UPDATE_NOT_AVAILABLE:
							result = new PluginResult(PluginResult.Status.OK, "UPDATE_NOT_AVAILABLE");
							callbackContext.sendPluginResult(result);
							break;

						default:
							result = new PluginResult(PluginResult.Status.OK, "UNKNOWN");
							callbackContext.sendPluginResult(result);
					}

				}).addOnFailureListener(e -> {
					// Returns "com.google.android.play.core.internal.al: Failed to bind to the service." whenever the Play Store is not available.
					e.printStackTrace();
					Log.e(LOG_TAG, e.toString());
					callbackContext.error(e.toString());

				});

			}
		});

	}

	public void setActivityResultCallback(CordovaPlugin plugin) {
		Log.i(LOG_TAG, "setActivityResultCallback");

		// Cancel any previously pending activity.
		if (this.activityResultCallback != null) {
			this.activityResultCallback.onActivityResult(this.activityResultRequestCode, Activity.RESULT_CANCELED, null);
		}
		this.activityResultCallback = plugin;
	}

	private void popupSnackbarForCompleteUpdate() {
		Log.i(LOG_TAG, "popupSnackbarForCompleteUpdate");
		this.PROMPTED = true;

		cordova.getActivity().runOnUiThread(new Runnable() {
			public void run() {
				try {
					snackbar = Snackbar.make(layout, snackbarText, Snackbar.LENGTH_INDEFINITE);
					snackbar.setAction(snackbarButton, new View.OnClickListener() {
						public void onClick(View view) {
							appUpdateManager.completeUpdate();
						}
					});
					snackbar.setActionTextColor(Color.parseColor(snackbarButtonColor));
					snackbar.show();
				} catch (Exception e) {
					e.printStackTrace();
					Log.e(LOG_TAG, e.getMessage());
				}
			}
		});

	}

	private void setSnackbarOptions(JSONArray args) {

		PluginResult result;
		Log.i(LOG_TAG, "setSnackbarOptions");

		try {

			JSONObject arg_object = args.getJSONObject(0);
			this.snackbarText = arg_object.getString("strText");
			this.snackbarButton = arg_object.getString("strButton");
			String colorHex = arg_object.getString("strButtonColor");
			if (colorHex != null && colorHex.length() == 7) {
				this.snackbarButtonColor = colorHex;
				result = new PluginResult(PluginResult.Status.OK, "SUCCESS");
			} else {
				result = new PluginResult(PluginResult.Status.OK, "SUCCESS (Invalid HEX color specified; ignored)");
			}
			this.callbackContext.sendPluginResult(result);

		} catch (Exception e) {

			Log.e(LOG_TAG, e.getMessage());
			result = new PluginResult(PluginResult.Status.ERROR, e.getMessage());
			this.callbackContext.sendPluginResult(result);

		}

	}

	private void updateFlexible() {

		currentUpdateType = AppUpdateType.FLEXIBLE;
		cordova.setActivityResultCallback(this);

		cordova.getThreadPool().execute(new Runnable() {
			//@Override
			InstallStateUpdatedListener installStateUpdatedListener;

			public void run() {

				// Creates instance of the manager.
				appUpdateManager = AppUpdateManagerFactory.create(cordova.getContext());

				// Returns an intent object that you use to check for an update.
				Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();

				// Create a listener to track request state updates.
				installStateUpdatedListener = new InstallStateUpdatedListener() {
					@Override
					public void onStateUpdate(InstallState state) {
						if (state.installStatus() == InstallStatus.DOWNLOADED) {
							// Update downloaded.
							Log.i(LOG_TAG, "InstallStateUpdatedListener: InstallStatus.DOWNLOADED");

							// Unregister listener
							appUpdateManager.unregisterListener(installStateUpdatedListener);

							// Prevent multiple consecutive prompts of this event
							if (!PROMPTED) {
								popupSnackbarForCompleteUpdate();
								PluginResult result = new PluginResult(PluginResult.Status.OK, "DOWNLOADED");
								callbackContext.sendPluginResult(result);
							}
						} else if (state.installStatus() == InstallStatus.INSTALLED) {
							// Update installed.
							Log.i(LOG_TAG, "InstallStateUpdatedListener: InstallStatus.INSTALLED");
							if (appUpdateManager != null) {
								// Unregister listener.
								appUpdateManager.unregisterListener(installStateUpdatedListener);
							}
						} else if (state.installStatus() == InstallStatus.DOWNLOADING) {
							// Now downloading...
							Log.i(LOG_TAG, "InstallStateUpdatedListener: InstallStatus.DOWNLOADING");
							PluginResult result = new PluginResult(PluginResult.Status.OK, "DOWNLOADING");
							result.setKeepCallback(true);
							callbackContext.sendPluginResult(result);
						} else {
							// Other status.
							Log.i(LOG_TAG, "InstallStateUpdatedListener: state: " + state.installStatus());
						}
					}
				};

				// Before starting an update, register a listener for updates.
				appUpdateManager.registerListener(installStateUpdatedListener);

				// Start an update.
				appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
					Log.i(LOG_TAG, "Updated success listener called");

					if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
						// If the update is downloaded but not installed, notify the user to complete the update.
						Log.i(LOG_TAG, "InstallStatus.DOWNLOADED");

						// When status updates are no longer needed, unregister the listener.
						appUpdateManager.unregisterListener(installStateUpdatedListener);

						popupSnackbarForCompleteUpdate();
						PluginResult result = new PluginResult(PluginResult.Status.OK, "DOWNLOADED");
						callbackContext.sendPluginResult(result);

					} else {
						// Check for update availability.
						switch (appUpdateInfo.updateAvailability()) {
							case UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS:
								// An update has been triggered by the developer and is in progress.
								Log.i(LOG_TAG, "DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS");
								callbackContext.success("DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS");
								break;

							case UpdateAvailability.UPDATE_AVAILABLE:
								// Update is available.
								Log.i(LOG_TAG, "UPDATE_AVAILABLE");
								if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
									// Request the update.
									startUpdate(AppUpdateType.FLEXIBLE, appUpdateInfo);

								} else {
									// Update type not allowed.
									Log.e(LOG_TAG, "Flexible update type not allowed");
									callbackContext.error("UPDATE_TYPE_NOT_ALLOWED");
								}
								break;

							case UpdateAvailability.UPDATE_NOT_AVAILABLE:
								// Update not available.
								Log.i(LOG_TAG, "UPDATE_NOT_AVAILABLE");
								callbackContext.success("UPDATE_NOT_AVAILABLE");
								break;

							default:
								// Unknown update status.
								Log.i(LOG_TAG, "UNKNOWN");
								callbackContext.success("UNKNOWN");
						}
					}

				}).addOnFailureListener(e -> {
					// Returns "com.google.android.play.core.internal.al: Failed to bind to the service." whenever the Play Store is not available.
					e.printStackTrace();
					Log.e(LOG_TAG, e.toString());
					callbackContext.error(e.toString());

				});

			}
		});
	}

	private void updateImmediate() {

		currentUpdateType = AppUpdateType.IMMEDIATE;
		cordova.setActivityResultCallback(this);
		cordova.getThreadPool().execute(new Runnable() {
			@Override
			public void run() {

				// Creates instance of the manager.
				appUpdateManager = AppUpdateManagerFactory.create(cordova.getContext());

				// Returns an intent object that you use to check for an update.
				Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();

				appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
					Log.i(LOG_TAG, "Updated success listener called");

					if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
						// If the update is downloaded but not installed, complete the update now.
						startUpdate(AppUpdateType.IMMEDIATE, appUpdateInfo);

					} else {
						// Check for update availability.
						switch (appUpdateInfo.updateAvailability()) {
							case UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS:
								// An update has been triggered by the developer and is in progress.
								Log.i(LOG_TAG, "DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS");
								callbackContext.success("DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS");
								break;

							case UpdateAvailability.UPDATE_AVAILABLE:
								// Update is available.
								Log.i(LOG_TAG, "UPDATE_AVAILABLE");
								if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
									// Request the update.
									startUpdate(AppUpdateType.IMMEDIATE, appUpdateInfo);
								} else {
									// Update type not allowed.
									Log.e(LOG_TAG, "Flexible update type not allowed");
									callbackContext.error("Flexible update type not allowed");
								}
								break;

							case UpdateAvailability.UPDATE_NOT_AVAILABLE:
								// Update not available.
								Log.i(LOG_TAG, "UPDATE_NOT_AVAILABLE");
								callbackContext.success("UPDATE_NOT_AVAILABLE");
								break;

							default:
								// Unknown update status.
								Log.i(LOG_TAG, "UNKNOWN");
								callbackContext.success("UNKNOWN");
						}
					}

				}).addOnFailureListener(e -> {
					// Returns "com.google.android.play.core.internal.al: Failed to bind to the service." whenever the Play Store is not available.
					Log.e(LOG_TAG, e.toString());
					e.printStackTrace();
					callbackContext.error(e.toString());

				});

			}
		});

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {

		PluginResult result;
		Log.i(LOG_TAG, "onActivityResult");

		if (requestCode == this.activityResultRequestCode && this.callbackContext != null) {
			switch (resultCode) {
				case RESULT_OK:
					// User accepted the update.
					Log.i(LOG_TAG, "RESULT_OK");
					result = new PluginResult(PluginResult.Status.OK, "RESULT_OK");
					result.setKeepCallback(true);
					this.callbackContext.sendPluginResult(result);
					break;

				case RESULT_CANCELED:
					// User cancelled the update.
					Log.i(LOG_TAG, "RESULT_CANCELED");
					result = new PluginResult(PluginResult.Status.OK, "RESULT_CANCELED");
					this.callbackContext.sendPluginResult(result);
					break;

				case RESULT_IN_APP_UPDATE_FAILED:
					// Update failured.
					Log.e(LOG_TAG, "RESULT_IN_APP_UPDATE_FAILED");
					result = new PluginResult(PluginResult.Status.ERROR, "RESULT_IN_APP_UPDATE_FAILED");
					this.callbackContext.sendPluginResult(result);
					break;

				default:
					// Unknown activity result.
					Log.e(LOG_TAG, "ACTIVITY_RESULT_UNKNOWN");
					result = new PluginResult(PluginResult.Status.ERROR, "ACTIVITY_RESULT_UNKNOWN");
					this.callbackContext.sendPluginResult(result);
			}
		}
		super.onActivityResult(requestCode, resultCode, intent);

	}

	@Override
	public void onResume(final boolean multitasking) {

		//Log.i(LOG_TAG, String.format("onResume: currentUpdateType = %s", String.valueOf(currentUpdateType)));
		appUpdateManager.getAppUpdateInfo().addOnSuccessListener(appUpdateInfo -> {
			if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
				// If the update is downloaded but not installed, notify the user to complete the update.
				if (currentUpdateType == AppUpdateType.FLEXIBLE) {
					Log.i(LOG_TAG, "onResume: popupSnackbarForCompleteUpdate");
					popupSnackbarForCompleteUpdate();
				} else {
					Log.i(LOG_TAG, "onResume: startUpdate");
					try {
						startUpdate(AppUpdateType.IMMEDIATE, appUpdateInfo);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});

		super.onResume(multitasking);

	}

	public void startUpdate(final int updateType, final AppUpdateInfo appUpdateInfo) {

		PluginResult result;
		Log.i(LOG_TAG, "startUpdate");

		try {
			appUpdateManager.startUpdateFlowForResult(appUpdateInfo, updateType, cordova.getActivity(), this.activityResultRequestCode);
			result = new PluginResult(PluginResult.Status.OK, "UPDATE_PROMPT");
			result.setKeepCallback(true);
			this.callbackContext.sendPluginResult(result);

		} catch (Exception e) {
			Log.e(LOG_TAG, e.getMessage());
			result = new PluginResult(PluginResult.Status.ERROR, e.getMessage());
			this.callbackContext.sendPluginResult(result);

		}

	}

}
