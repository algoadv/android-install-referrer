package com.algoadtech.installreferrer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class ReferrerReceiver extends BroadcastReceiver {

    private static final String ACTION_INSTALL_REFERRER = "com.android.vending.INSTALL_REFERRER";
    private static final String KEY_REFERRER = "referrer";
    private static String TAG = "ANDROID_REFERRER_RECEIVER";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) {
            Log.e(TAG, "Intent is null");
            return;
        }
        
        if (!ACTION_INSTALL_REFERRER.equals(intent.getAction())) {
            Log.e(TAG, "Wrong action! Expected: " + ACTION_INSTALL_REFERRER + " but was: " + intent.getAction());
            return;
        }

        Bundle extras = intent.getExtras();
        if (intent.getExtras() == null) {
            Log.e(TAG, "No data in intent");
            return;
        }

        String referrer = extras.getString("referrer", "");
        Log.v(TAG, "Referrer received from installation: " + referrer);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        Editor edit = sharedPreferences.edit();
        edit.putString(KEY_REFERRER, referrer);
        edit.commit();
    }
}