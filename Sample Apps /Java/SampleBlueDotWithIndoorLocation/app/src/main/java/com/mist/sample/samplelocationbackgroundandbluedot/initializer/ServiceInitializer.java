package com.mist.sample.samplelocationbackgroundandbluedot.initializer;

import static androidx.core.content.ContextCompat.startForegroundService;

import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.mist.sample.samplelocationbackgroundandbluedot.service.LocationForegroundService;

/**
 * ServiceInitializer This class provides utility functions to start the android services.
 */
public class ServiceInitializer {
    /* Define your SDK job ID here.*/
    public static final int MIST_SDK_JOB_ID = 789;

    public static void startLocationService(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startMistForegroundService(context);
        }
    }

    public static void stopLocationService(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            stopMistForegroundService(context);
        }
    }

    private static void startMistForegroundService(Context context) {
        Intent intent = new Intent(context, LocationForegroundService.class);
        startForegroundService(context, intent);
    }

    public static void stopMistForegroundService(Context context) {
        Intent intent = new Intent(context, LocationForegroundService.class);
        context.stopService(intent);
    }
}
