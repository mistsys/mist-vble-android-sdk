package com.mist.sample.samplewakeup.application;

import android.app.Application;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import com.mist.sample.samplewakeup.NotificationHandler;
import com.mist.sample.samplewakeup.service.LocationForegroundService;
import com.mist.sample.samplewakeup.util.AltBeaconUtil;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.Region;

/**
 * https://developer.android.com/reference/android/app/Application
 * Base class for maintaining global application state.
 * You can provide your own implementation by creating a subclass and specifying the fully-qualified name of this subclass as the "android:name" attribute in your AndroidManifest.xml's <application> tag.
 * The Application class, or your subclass of the Application class,
 * is instantiated before any other class when the process for your application/package is created.
 */
public class MainApplication extends Application implements  MonitorNotifier  {

    private static final String TAG = "SampleAppWakeUp";
    private  static MainApplication mainApplication;
    private BeaconManager beaconManager;

    @Override
    public void onCreate() {
        super.onCreate();
         mainApplication = this;
         beaconManager = BeaconManager.getInstanceForApplication(getApplicationContext());
        NotificationHandler.createNotificationChannel(getApplicationContext());
    }

    public static MainApplication getApplication(){
        return mainApplication;
    }

    @Override
    public void didEnterRegion(Region region) {

        if(!region.getUniqueId().contains("wakeup-beacons"))
            return;
        Log.i(TAG, "Found beacon" + region.getUniqueId());
        NotificationHandler.sendNotification(getApplicationContext(), "Found Beacon " + region.getUniqueId());
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            // Increase beacon scan interval for reducing scan frequency.
            AltBeaconUtil.increaseBeaconScanPeriod(mainApplication.getBeaconManager());

            //Starting mist location SDK as foreground service
            Intent intent = new Intent(getApplicationContext(), LocationForegroundService.class);
            NotificationHandler.sendNotification(getApplicationContext(), "start foreground mist beacon service" );
            startForegroundService( intent);
        }
    }

    @Override
    public void didExitRegion(Region region) {
        if(!region.getUniqueId().contains("wakeup-beacons"))
            return;

        Log.i(TAG, "Lost beacon");
        NotificationHandler.sendNotification(getApplicationContext(), "Lost beacon "+region.getUniqueId());
    }

    @Override
    public void didDetermineStateForRegion(int state, Region region) {
        Log.i(TAG, "Switched from seeing/not seeing beacons: "+state);
        NotificationHandler.sendNotification(getApplicationContext(), "Switched from seeing/not seeing beacons: "+state);
    }

    public BeaconManager getBeaconManager() {
        return beaconManager;
    }
}