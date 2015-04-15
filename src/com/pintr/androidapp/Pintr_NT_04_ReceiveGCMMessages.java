package com.pintr.androidapp;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

public class Pintr_NT_04_ReceiveGCMMessages extends WakefulBroadcastReceiver {
	static final String TAG = "GCMDemo";
	
	@Override
    public void onReceive(Context context, Intent intent) {
		ComponentName comp = new ComponentName(context.getPackageName()
												, Pintr_NT_03_GcmIntentService.class.getName());
		Log.d(TAG, "******RECEIVER FIRED");
        startWakefulService(context, (intent.setComponent(comp)));
        setResultCode(Activity.RESULT_OK);
    }

}
