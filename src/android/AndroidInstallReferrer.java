package com.algoadtech.installreferrer;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.SharedPreferences;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;

import com.android.installreferrer.api.InstallReferrerStateListener;
import com.android.installreferrer.api.ReferrerDetails;
import com.android.installreferrer.api.InstallReferrerClient;
import com.android.installreferrer.api.InstallReferrerClient.InstallReferrerResponse;
import android.content.Intent;

import java.util.HashMap;
import java.util.Map;

public class AndroidInstallReferrer extends CordovaPlugin {
    private static final String KEY_REFERRER = "referrer"; // no app prefix - for backward compatibility
    private static final String KEY_REFERRER_DATA = "app_referrer_data";
    private static final String KEY_REFERRER_CHECKED = "app_referrer_checked";
    private static final String KEY_GOOGLE_PLAY_CONNECTION_STATUS = "app_play_con_status";
    private static final String GENERIC_ERROR = "Google Play Services Error";

    private static String TAG = "ANDROID_INSTALL_RECEIVER";
    private InstallReferrerClient ReferrerClient = null;

    @Override
    public void onNewIntent(Intent intent) {
        cordova.getActivity().setIntent(intent);
        Log.e(TAG, "Intent Received: " + intent.getAction());
    }

    /**
     * Called after plugin construction and fields have been initialized.
     */
    protected void pluginInitialize() {
        Log.v(TAG, "Plugin Initializing");
        this.ReferrerClient = InstallReferrerClient.newBuilder(this.cordova.getActivity().getApplicationContext()).build();
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) {
        PluginResult.Status status = PluginResult.Status.OK;

        if(!action.equals("get") && !action.equals("getData")) {
            Log.e(TAG, "Unkown action: " + action );
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, "Unkown action"));
            return false;
        }

        // pull the data from the shared preferences
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this.cordova.getActivity());
        boolean appReferrerChecked = sharedPrefs.getBoolean(KEY_REFERRER_CHECKED, false);
        
        if(!appReferrerChecked) { // we haven't checked the referrer on Google Play for the app yet
            Log.v(TAG, "Retrieving referrer from Google Play");
            this.retrieveInstallReferrerFromPlayStore(sharedPrefs, callbackContext, this.ReferrerClient, action);
        }
        else { // this referrer has been checked on Google Play for this app
            Log.v(TAG, "Retrieving referrer from cache");
            String preferencesKey = action == "get" ? KEY_REFERRER : KEY_REFERRER_DATA;
            String result = sharedPrefs.getString(preferencesKey, "");
            Log.v(TAG, "Referer value: " + result );
            callbackContext.sendPluginResult(new PluginResult(status, result));
        }

        return true;
    }

    private void retrieveInstallReferrerFromPlayStore(
            SharedPreferences sharedPreferences,
            CallbackContext callbackContext,
            InstallReferrerClient referrerClient,
            String action) {
            //{ anadna = asjdajs}
        this.ReferrerClient.startConnection(new InstallReferrerStateListener() {
            @Override
            public void onInstallReferrerSetupFinished(int responseCode) {
                SharedPreferences.Editor edit = sharedPreferences.edit();
                edit.putBoolean(KEY_REFERRER_CHECKED, true);
                edit.putInt(KEY_GOOGLE_PLAY_CONNECTION_STATUS, responseCode);

                switch (responseCode) {
                    case InstallReferrerResponse.OK:
                        // Connection established
                        Log.v(TAG, "Google Play Client Connected");
                        ReferrerDetails response;

                        try {
                            response = referrerClient.getInstallReferrer();

                            String referrerParam = response.getInstallReferrer();

                            Map<String, Object> referrerData = new HashMap<String, Object>();
                            referrerData.put("referrer", referrerParam);
                            referrerData.put("clickTimestamp", response.getReferrerClickTimestampSeconds());
                            referrerData.put("installTimestamp", response.getInstallBeginTimestampSeconds());
                            JSONObject jsonObj = new JSONObject(referrerData);
                            String jsonObjString = jsonObj.toString();

                            edit.putString(KEY_REFERRER_DATA, jsonObjString);
                            edit.putString(KEY_REFERRER, referrerParam);

                            Log.v(TAG, "Retrieved Referrer From Google Play: " + jsonObjString);

                            if(action.equals("get"))
                                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, referrerParam));
                            else
                                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, jsonObjString));

                        } catch (RemoteException e) {
                            Log.e(TAG, "Connection failed during Google Play details retrieval: \n" + e.toString());
                            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, GENERIC_ERROR));
                        }
                        break;
                    case InstallReferrerResponse.FEATURE_NOT_SUPPORTED:
                        // API not available on the current Play Store app
                        Log.e(TAG, "Google Play Client Not Supported - Update is needed");
                        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, GENERIC_ERROR));
                        break;
                    case InstallReferrerResponse.SERVICE_UNAVAILABLE:
                        // Connection could not be established
                        Log.e(TAG, "Google Play Client Service Unavailable");
                        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, GENERIC_ERROR));
                        break;
                }
                // disconnect the client
                referrerClient.endConnection();
                edit.commit();
            }

            @Override
            public void onInstallReferrerServiceDisconnected() {

            }
        });
    }
}