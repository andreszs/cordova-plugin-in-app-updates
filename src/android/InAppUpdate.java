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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.atomic.AtomicReference;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static com.google.android.play.core.install.model.ActivityResult.RESULT_IN_APP_UPDATE_FAILED;

public class InAppUpdate extends CordovaPlugin {

	protected CordovaPlugin activityResultCallback;
	protected int activityResultRequestCode = 1714;
	private final AtomicReference<CallbackContext> currentCallbackContext = new AtomicReference<CallbackContext>();
	private FrameLayout layout;
   private Snackbar snackbar;
	private CallbackContext myCallbackContext;

	private static int currentUpdateType;
	private static AppUpdateManager appUpdateManager;

	private String snackbarText = "An update has just been downloaded.";
	private String snackbarButton = "RESTART";
	private String snackbarDuration = "LENGTH_INDEFINITE";
	private String snackbarButtonColor = "#76FF03";

	public InAppUpdate() {
		Log.i(InAppUpdate.class.getSimpleName(), "Constructed");
	}

	@Override
	public void initialize(CordovaInterface cordova, CordovaWebView webView) {
		super.initialize(cordova, webView);
		layout = (FrameLayout) webView.getView().getParent();
	}

	@Override
	public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
		Log.i(InAppUpdate.class.getSimpleName(), String.format("Executing the %s action started", action));
		if (action.equals("getUpdateAvailability")) {
			getUpdateAvailability(callbackContext);
			return true;
		} else if (action.equals("updateFlexible")) {
			updateFlexible(callbackContext);
			return true;
		} else if (action.equals("updateImmediate")) {
			updateImmediate(callbackContext);
			return true;
		} else if (action.equals("setSnackbarOptions")){
			setSnackbarOptions(callbackContext, args);
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
					Log.i(InAppUpdate.class.getSimpleName(), "appUpdateInfoTask success listener called");

					switch (appUpdateInfo.updateAvailability()) {
						case UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS:
							callbackContext.success("DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS");
							break;

						case UpdateAvailability.UPDATE_AVAILABLE:
							callbackContext.success("UPDATE_AVAILABLE");
							break;

						case UpdateAvailability.UPDATE_NOT_AVAILABLE:
							callbackContext.success("UPDATE_NOT_AVAILABLE");
							break;

						default:
							callbackContext.success("UNKNOWN");
					}

				}).addOnFailureListener(e -> {
					// Returns "com.google.android.play.core.internal.al: Failed to bind to the service." whenever the Play Store is not available.
					Log.e(InAppUpdate.class.getSimpleName(), e.toString());

					e.printStackTrace();
					callbackContext.error(e.toString());

				});

			}
		});

	}

	//@Override
	public void setActivityResultCallback(CordovaPlugin plugin) {
		Log.i(InAppUpdate.class.getSimpleName(), "setActivityResultCallback");
		// Cancel any previously pending activity.
//		if (activityResultCallback != null) {
//			activityResultCallback.onActivityResult(activityResultRequestCode, Activity.RESULT_CANCELED, null);
//		}
		this.activityResultCallback = plugin;
	}

	private void popupSnackbarForCompleteUpdate() {
		Log.i(InAppUpdate.class.getSimpleName(), "Requesting snackbar for installing update");

		//final CallbackContext callbackContext = currentCallbackContext.getAndSet(null);

		try {

			if (this.snackbar != null) {
				this.snackbar.dismiss();
			}
			cordova.getActivity().runOnUiThread(new Runnable() {
				public void run() {
					Log.i(InAppUpdate.class.getSimpleName(), "Snackbar.make");

					snackbar = Snackbar.make(layout, snackbarText, Snackbar.LENGTH_INDEFINITE);
					snackbar.setActionTextColor(Color.parseColor(snackbarButtonColor));

					if(snackbarDuration.equals("LENGTH_LONG")){
						snackbar.setDuration(Snackbar.LENGTH_LONG);
					}else if(snackbarDuration.equals("LENGTH_SHORT")){
						snackbar.setDuration(Snackbar.LENGTH_LONG);
					}else{
						snackbar.setDuration(Snackbar.LENGTH_INDEFINITE);
					}

					//snackbar.setAction(snackbarButton, view -> appUpdateManager.completeUpdate());
					snackbar.setAction(snackbarButton, new View.OnClickListener() {
						//@Override
						public void onClick(View view) {
							appUpdateManager.completeUpdate();
						}
					});
					snackbar.show();
				}
			});
			myCallbackContext.success("READY_TO_INSTALL");

		} catch (Exception e) {

			Log.e(InAppUpdate.class.getSimpleName(), e.getMessage());
			//callbackContext.error(e.getMessage());

		}

	}

	private void setSnackbarOptions(CallbackContext callbackContext, JSONArray args) {
		Log.i(InAppUpdate.class.getSimpleName(), "setSnackbarOptions");

		try {

			JSONObject arg_object = args.getJSONObject(0);
			this.snackbarText = arg_object.getString("strText");
			this.snackbarButton = arg_object.getString("strButton");
			String colorHex = arg_object.getString("strButtonColor");
			if (colorHex != null && colorHex.length() == 7) {
				this.snackbarButtonColor = colorHex;
			}
			callbackContext.success("Success");

		} catch (Exception e) {

			Log.e(InAppUpdate.class.getSimpleName(), e.getMessage());
			callbackContext.error(e.getMessage());

		}

	}

	private void updateFlexible(CallbackContext callbackContext) {
		currentCallbackContext.set(callbackContext);
		myCallbackContext = callbackContext;

		currentUpdateType = AppUpdateType.FLEXIBLE;
		cordova.setActivityResultCallback(this);
		cordova.getThreadPool().execute(new Runnable() {
			@Override
			public void run() {

				// Creates instance of the manager.
				appUpdateManager = AppUpdateManagerFactory.create(cordova.getContext());

				// Returns an intent object that you use to check for an update.
				Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();

				// Create a listener to track request state updates.
				InstallStateUpdatedListener listener = state -> {
				  if (state.installStatus() == InstallStatus.DOWNLOADED) {
					  // After the update is downloaded, show a notification and request user confirmation to restart the app.
					  Log.i(InAppUpdate.class.getSimpleName(), ">>> InstallStateUpdatedListener : state.installStatus() == InstallStatus.DOWNLOADED");

					  popupSnackbarForCompleteUpdate();
					}
				};

				// Before starting an update, register a listener for updates.
				appUpdateManager.registerListener(listener);

				// Start an update.
				appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
					Log.i(InAppUpdate.class.getSimpleName(), "Updated success listener called");

					if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
						// If the update is downloaded but not installed, notify the user to complete the update.
						Log.i(InAppUpdate.class.getSimpleName(), "appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED ...");
						popupSnackbarForCompleteUpdate();

						// When status updates are no longer needed, unregister the listener.
						appUpdateManager.unregisterListener(listener);

						callbackContext.success("DOWNLOADED");

					} else {
						// Check for update availability.
						Log.i(InAppUpdate.class.getSimpleName(), "appUpdateInfo.installStatus() != InstallStatus.DOWNLOADED");

						switch (appUpdateInfo.updateAvailability()) {
							case UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS:
								// An update has been triggered by the developer and is in progress.
								Log.i(InAppUpdate.class.getSimpleName(), "DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS");
								callbackContext.success("DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS");
								break;

							case UpdateAvailability.UPDATE_AVAILABLE:
								// Update is available.
								Log.i(InAppUpdate.class.getSimpleName(), "UPDATE_AVAILABLE");
								if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
									// Request the update.
									startUpdate(AppUpdateType.FLEXIBLE, appUpdateInfo);
								} else {
									// Update type not allowed.
									Log.e(InAppUpdate.class.getSimpleName(), "Flexible update type not allowed");
									callbackContext.error("Flexible update type not allowed");
								}
								break;

							case UpdateAvailability.UPDATE_NOT_AVAILABLE:
								// Update not available.
								Log.i(InAppUpdate.class.getSimpleName(), "UPDATE_NOT_AVAILABLE");
								callbackContext.success("UPDATE_NOT_AVAILABLE");
								break;

							default:
								// Unknown update status.
								Log.i(InAppUpdate.class.getSimpleName(), "UNKNOWN");
								callbackContext.success("UNKNOWN");
						}
					}

				}).addOnFailureListener(e -> {
					// Returns "com.google.android.play.core.internal.al: Failed to bind to the service." whenever the Play Store is not available.
					Log.e(InAppUpdate.class.getSimpleName(), e.toString());

					e.printStackTrace();
					callbackContext.error(e.toString());

				});

			}
		});
	}

	private void updateImmediate(CallbackContext callbackContext) {
		currentCallbackContext.set(callbackContext);

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
					Log.i(InAppUpdate.class.getSimpleName(), "Updated success listener called");

					if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
						// If the update is downloaded but not installed, complete the update now.
						startUpdate(AppUpdateType.IMMEDIATE, appUpdateInfo);

					} else {
						// Check for update availability.
						switch (appUpdateInfo.updateAvailability()) {
							case UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS:
								// An update has been triggered by the developer and is in progress.
								Log.i(InAppUpdate.class.getSimpleName(), "DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS");
								callbackContext.success("DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS");
								break;

							case UpdateAvailability.UPDATE_AVAILABLE:
								// Update is available.
								Log.i(InAppUpdate.class.getSimpleName(), "UPDATE_AVAILABLE");
								if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
									// Request the update.
									startUpdate(AppUpdateType.IMMEDIATE, appUpdateInfo);
								} else {
									// Update type not allowed.
									Log.e(InAppUpdate.class.getSimpleName(), "Flexible update type not allowed");
									callbackContext.error("Flexible update type not allowed");
								}
								break;

							case UpdateAvailability.UPDATE_NOT_AVAILABLE:
								// Update not available.
								Log.i(InAppUpdate.class.getSimpleName(), "UPDATE_NOT_AVAILABLE");
								callbackContext.success("UPDATE_NOT_AVAILABLE");
								break;

							default:
								// Unknown update status.
								Log.i(InAppUpdate.class.getSimpleName(), "UNKNOWN");
								callbackContext.success("UNKNOWN");
						}
					}

				}).addOnFailureListener(e -> {
					// Returns "com.google.android.play.core.internal.al: Failed to bind to the service." whenever the Play Store is not available.
					Log.e(InAppUpdate.class.getSimpleName(), e.toString());

					e.printStackTrace();
					callbackContext.error(e.toString());

				});

			}
		});

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {

		Log.i(InAppUpdate.class.getSimpleName(), "onActivityResult");

		if (requestCode == activityResultRequestCode) {
			final CallbackContext callbackContext = currentCallbackContext.getAndSet(null);
			if (resultCode != RESULT_OK) {
				if (callbackContext != null) {
					switch (resultCode) {
						case RESULT_CANCELED:
							callbackContext.error("canceled");
							break;
						case RESULT_IN_APP_UPDATE_FAILED:
							callbackContext.error("failed");
							break;
						default:
							callbackContext.error("error");
					}
				}
			}
		}
		super.onActivityResult(requestCode, resultCode, intent);

	}

	@Override
	public void onResume(final boolean multitasking) {

		Log.i(InAppUpdate.class.getSimpleName(), String.format("onResume: currentUpdateType = %s", String.valueOf(currentUpdateType)));

		super.onResume(multitasking);

		appUpdateManager.getAppUpdateInfo().addOnSuccessListener(appUpdateInfo -> {
			if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
				// If the update is downloaded but not installed, notify the user to complete the update.
				Log.i(InAppUpdate.class.getSimpleName(), String.format("appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED"));

				if (currentUpdateType == AppUpdateType.FLEXIBLE) {

					Log.i(InAppUpdate.class.getSimpleName(), ">>> onResume: currentUpdateType == AppUpdateType.FLEXIBLE");

					popupSnackbarForCompleteUpdate();
				} else {
					Log.i(InAppUpdate.class.getSimpleName(), ">>> onResume: startUpdate(AppUpdateType.IMMEDIATE, appUpdateInfo)");
					try {
						startUpdate(AppUpdateType.IMMEDIATE, appUpdateInfo);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});

	}

	public void startUpdate(final int updateType, final AppUpdateInfo appUpdateInfo) {

		Log.i(InAppUpdate.class.getSimpleName(), "startUpdate");

		final CallbackContext callbackContext = currentCallbackContext.getAndSet(null);

		try {
			appUpdateManager.startUpdateFlowForResult(appUpdateInfo, updateType, cordova.getActivity(), activityResultRequestCode);
			//} catch (IntentSender.SendIntentException e) {
		} catch (Exception e) {
			e.printStackTrace();
			//Log.e(InAppUpdate.class.getSimpleName(), e.getMessage());
			Log.e(InAppUpdate.class.getSimpleName(), "startUpdate ERROR");
			//callbackContext.error(e.getMessage());
			callbackContext.error("startUpdate ERROR");
		}

	}

}
