package com.mist.sample.samplewakeup.service;

import static androidx.core.content.ContextCompat.startForegroundService;
import static com.mist.sample.samplewakeup.Constants.NO_VBLE_FAIL_COUNT_LIMIT;
import static com.mist.sample.samplewakeup.Constants.NO_VBLE_TIMEOUT_MS;

import android.app.AlarmManager;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.mist.android.ErrorType;
import com.mist.android.IndoorLocationCallback;
import com.mist.android.MistMap;
import com.mist.android.MistPoint;
import com.mist.android.MistVirtualBeacon;
import com.mist.android.VirtualBeaconCallback;
import com.mist.sample.samplewakeup.MainActivity;
import com.mist.sample.samplewakeup.NotificationHandler;
import com.mist.sample.samplewakeup.R;
import com.mist.sample.samplewakeup.application.MainApplication;
import com.mist.sample.samplewakeup.util.AltBeaconUtil;

/**
 * SDKCallbackHandler
 * We provide business logic from application perspective for different callback
 * from Mist SDK
 */
public class SDKCallbackHandler implements VirtualBeaconCallback, IndoorLocationCallback {
    Context context;
    private long lastFailedTime = 0;
    private long failedCount = 0;

    public SDKCallbackHandler(Context context) {
        this.context = context;
    }

    private static final String TAG = "SampleWakeUpApp";

    /**
     * We need to implement this method as per our business logic.
     * These methods will be called for IndoorLocationCallback
     * @param relativeLocation
     */
    @Override
    public void onRelativeLocationUpdated(MistPoint relativeLocation) {
        Log.v(TAG, "onRelativeLocationUpdated called");
        NotificationHandler.sendNotification(context, relativeLocation.toString());
        // Returns updated location of the mobile client (as a point (X, Y) measured in meters from the map origin, i.e., relative X, Y)
        }

    @Override
    public void onMapUpdated(MistMap map) {
        Log.v(TAG, "onMapUpdated called");
        /** Returns update map for the mobile client as a {@link}MSTMap object */
    }

    @Override
    public void onError(ErrorType errorType, String errorMessage) {
        Log.v(TAG, "onError called" + errorMessage);
        if (errorType != ErrorType.NO_BEACONS_DETECTED) {
            /**
             * Stopping location service only for vble related errors
             */
            NotificationHandler.sendNotification(context, errorMessage);
            return;
        }
        if (failedCount % 10 < 1) {
            NotificationHandler.sendNotification(context, errorMessage + " " + failedCount);
        }
        if (lastFailedTime == 0) {
            lastFailedTime = System.currentTimeMillis();
        }
        long timeSinceInitialFailedCall = System.currentTimeMillis() - lastFailedTime;
        failedCount++;
        if (isDeviceLocked(context)) {
            return;
        }
        /**
         * We are stopping the location sdk, if we get more then NO_VBLE_FAIL_COUNT_LIMIT vble error after 5 min.
         */
        if (timeSinceInitialFailedCall >= NO_VBLE_TIMEOUT_MS && failedCount > NO_VBLE_FAIL_COUNT_LIMIT) {
            /** Stopping the location SDK */
            MainApplication mainApplication = MainApplication.getApplication();
            MistSdkManager.getInstance(mainApplication).stopMistSDK();
            /** Here we reduce the scan time for alt beacon so that it starts scanning for beacon frequently. */
            AltBeaconUtil.decreaseBeaconScanPeriod(mainApplication.getBeaconManager());
            /** Stopping the location foreground service*/
            Intent intent = new Intent(context, LocationForegroundService.class);
            context.stopService(intent);
        } else if (timeSinceInitialFailedCall >= NO_VBLE_TIMEOUT_MS && failedCount < NO_VBLE_FAIL_COUNT_LIMIT) {
            failedCount = 1;
            lastFailedTime = System.currentTimeMillis();
        }}

    private boolean isDeviceLocked(Context context) {
        KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        return keyguardManager.isDeviceLocked();
    }

    /**
     * We need to implement this method as per our business logic.
     * These methods will be called for VirtualBeaconCallback
     * @param mistVirtualBeacon
     */
    @Override
    public void didRangeVirtualBeacon(MistVirtualBeacon mistVirtualBeacon) {
        Log.v(TAG, "didRangeVirtualBeacon called");
        NotificationHandler.sendNotification(context, mistVirtualBeacon.message);
    }

    @Override
    public void onVirtualBeaconListUpdated(MistVirtualBeacon[] virtualBeacons) {
        Log.v(TAG, "onVirtualBeaconListUpdated called");
        NotificationHandler.sendNotification(context, "virtual beacon list updated");
    }
}
