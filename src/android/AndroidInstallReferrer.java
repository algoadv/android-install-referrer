package com.algoadtech.installreferrer;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class AndroidInstallReferrer extends CordovaPlugin {
    private static final String KEY_REFERRER = "referrer";
    private static String TAG = "ANDROID_INSTALL_RECEIVER";

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) {
        PluginResult.Status status = PluginResult.Status.OK;

        if(!action.equals("get")) {
            Log.e(TAG, "Unkown action: " + action );
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, "Unkown action"));
            return false;
        }
        
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this.cordova.getActivity());
        String result = sharedPrefs.getString(KEY_REFERRER, "");
        Log.v(TAG, "Referer value: " + result );

        callbackContext.sendPluginResult(new PluginResult(status, result));
        return true;
    }
}