package com.mist.sample.wakeup.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.mist.sample.wakeup.service.NearByJobIntentService;

public class NearByBroadCastReceiver extends BroadcastReceiver {

    private static final String TAG = NearByBroadCastReceiver.class.getSimpleName();

    public NearByBroadCastReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Received Broadcast");

        if (intent != null) {
            NearByJobIntentService.enqueueWork(context, intent);
        }
    }
}
