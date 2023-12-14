package com.example.sampleappwakeup.application

import android.app.Application
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.sampleappwakeup.NotificationHandler
import com.example.sampleappwakeup.service.LocationForegroundService
import com.example.sampleappwakeup.util.AltBeaconUtil
import org.altbeacon.beacon.BeaconManager
import org.altbeacon.beacon.MonitorNotifier
import org.altbeacon.beacon.Region

/**
 * https://developer.android.com/reference/android/app/Application
 * Base class for maintaining global application state.
 * You can provide your own implementation by creating a subclass and specifying the fully-qualified name of this subclass as the "android:name" attribute in your AndroidManifest.xml's <application> tag.
 * The Application class, or your subclass of the Application class,
 * is instantiated before any other class when the process for your application/package is created.
 */
class MainApplication : Application(), MonitorNotifier {

    private val TAG : String = "SampleAppWakeUp"
    private lateinit var mainApplication : MainApplication
    private lateinit var beaconManager: BeaconManager

    private val notificationHandler = NotificationHandler()
    private val altBeaconUtil = AltBeaconUtil()

    override fun onCreate() {
        super.onCreate()
        mainApplication = this
        beaconManager = BeaconManager.getInstanceForApplication(applicationContext)
        notificationHandler.createNotificationChannel(applicationContext)
    }

    fun getApplication() : MainApplication {
        return mainApplication
    }

    override fun didEnterRegion(region: Region) {
        if(!region.uniqueId.contains("wakeup-beacons")){
            return
        }
        Log.i(TAG,"Found beacon " + region.uniqueId)
        notificationHandler.sendNotification(applicationContext, "Found Beacon " + region.uniqueId)
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            // Increase beacon scan interval for reducing scan frequency.
            altBeaconUtil.increaseBeaconScanPeriod(mainApplication.getBeaconManager())
            //Starting mist location SDK as foreground service
            val intent = Intent(applicationContext, LocationForegroundService::class.java)
            notificationHandler.sendNotification(applicationContext,"start foreground mist beacon service")
            startForegroundService(intent)
        }
    }

    override fun didExitRegion(region: Region) {
        if(!region.uniqueId.contains("wakeup-beacons")){
            return
        }
        Log.i(TAG,"Lost beacon")
        notificationHandler.sendNotification(applicationContext,"Lost beacon " + region.uniqueId)
    }

    override fun didDetermineStateForRegion(state: Int, region: Region?) {
        Log.i(TAG, "Switched from seeing/not seeing beacons:$state")
        notificationHandler.sendNotification(applicationContext, "Switched from seeing/not seeing beacons: $state")
    }

    fun getBeaconManager() : BeaconManager {
        return beaconManager
    }

}