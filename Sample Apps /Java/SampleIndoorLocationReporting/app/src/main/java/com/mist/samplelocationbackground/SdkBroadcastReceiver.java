package com.mist.samplelocationbackground;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.mist.samplelocationbackground.initializer.ServiceInitializer;

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
                Log.d("", "Sample Location App: ACTION_BOOT_COMPLETED");
                ServiceInitializer.startLocationService(context);
                break;
            /*https://developer.android.com/reference/android/content/Intent#ACTION_USER_PRESENT*/
            case Intent.ACTION_USER_PRESENT:
                Log.d("", "Sample Location App: ACTION_USER_PRESENT");
                ServiceInitializer.startLocationService(context);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + intent.getAction());
        }
    }
}
