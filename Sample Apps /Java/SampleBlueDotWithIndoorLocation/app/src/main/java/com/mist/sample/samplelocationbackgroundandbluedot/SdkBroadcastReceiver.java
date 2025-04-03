package com.mist.sample.samplelocationbackgroundandbluedot;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.mist.sample.samplelocationbackgroundandbluedot.initializer.ServiceInitializer;

/**
 * SdkBroadcastReceiver This class Handles system broadcast event to restart the location service
 */
public class SdkBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
            /*https://developer.android.com/reference/android/content/Intent#ACTION_BOOT_COMPLETED*/
            case Intent.ACTION_BOOT_COMPLETED:
                Toast.makeText(context, "ACTION_BOOT_COMPLETED", Toast.LENGTH_SHORT).show();
                ServiceInitializer.startLocationService(context);
                break;
            /*https://developer.android.com/reference/android/content/Intent#ACTION_USER_PRESENT*/
            case Intent.ACTION_USER_PRESENT:
                ServiceInitializer.startLocationService(context);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + intent.getAction());
        }
    }
}
