package com.example.sampleappwakeup.util

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.RemoteException
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.sampleappwakeup.Constants
import com.example.sampleappwakeup.MainActivity
import com.example.sampleappwakeup.R
import org.altbeacon.beacon.BeaconManager
import org.altbeacon.beacon.BeaconParser
import org.altbeacon.beacon.Identifier
import org.altbeacon.beacon.MonitorNotifier
import org.altbeacon.beacon.Region

class AltBeaconUtil {

    private var regions = ArrayList<Region>()
    private val constants = Constants()

    fun startBeaconMonitor(beaconManager: BeaconManager, context: Context){
        beaconManager.beaconParsers.clear()
        beaconManager.beaconParsers.add(BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"))
        beaconManager.setEnableScheduledScanJobs(true)
        val intent1 = Intent(context, MainActivity::class.java)
        val pendingIntent : PendingIntent = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            PendingIntent.getActivity(context, 0, intent1, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        } else{
            PendingIntent.getActivity(context, 0, intent1, PendingIntent.FLAG_UPDATE_CURRENT )
        }
        /** Create foreground service for alt beacon scanning. */
        val notification: Notification = NotificationCompat.Builder(context, "ChannelId2").setContentTitle("Sample App Wakeup")
                .setContentText("Alt beacon scan service running")
                .setSmallIcon(R.drawable.ic_launcher_foreground).setContentIntent(pendingIntent)
                .build()
        beaconManager.addMonitorNotifier((context as MonitorNotifier))
        beaconManager.enableForegroundServiceScanning(notification,71)
        beaconManager.backgroundBetweenScanPeriod = constants.beaconScanIntervalLocationSdkNotRunningMs
        beaconManager.foregroundBetweenScanPeriod = constants.beaconScanIntervalLocationSdkNotRunningMs
        beaconManager.backgroundScanPeriod = constants.beaconPerScanDuration
        beaconManager.foregroundScanPeriod = constants.beaconPerScanDuration
        val subOrgId: String = constants.orgId.substring(0, (constants.orgId.length - 2))
        regions.apply {add(Region("wakeup-beacons1", Identifier.parse(constants.orgId), null, null))
            add(Region("wakeup-beacons2", Identifier.parse(subOrgId + "00"), null, null))
            add(Region("wakeup-beacons3", Identifier.parse(subOrgId + "01"), null, null))
            add(Region("wakeup-beacons4", Identifier.parse(subOrgId + "02"), null, null))
            add(Region("wakeup-beacons5", Identifier.parse(subOrgId + "03"), null, null))
            add(Region("wakeup-beacons6", Identifier.parse(subOrgId + "04"), null, null))
            add(Region("wakeup-beacons7", Identifier.parse(subOrgId + "05"), null, null))
            add(Region("wakeup-beacons8", Identifier.parse(subOrgId + "06"), null, null))
            add(Region("wakeup-beacons9", Identifier.parse(subOrgId + "07"), null, null))
            add(Region("wakeup-beacons10", Identifier.parse(subOrgId + "08"), null, null))
            add(Region("wakeup-beacons11", Identifier.parse(subOrgId + "09"), null, null))
            add(Region("wakeup-beacons12", Identifier.parse(subOrgId + "0a"), null, null))
            add(Region("wakeup-beacons13", Identifier.parse(subOrgId + "0b"), null, null))
            add(Region("wakeup-beacons14", Identifier.parse(subOrgId + "0c"), null, null))
            add(Region("wakeup-beacons15", Identifier.parse(subOrgId + "0d"), null, null))
        }

        Toast.makeText(context,"Monitoring started ",Toast.LENGTH_SHORT).show()
        for (region in regions) {
            beaconManager.startMonitoring(region)
        }
    }

    /**
     * It Increases the time interval between two consecutive beacon scan so that it doesn't scan
     * frequently. We call it once we are starting location sdk.
     */
    fun increaseBeaconScanPeriod(beaconManager: BeaconManager){
        beaconManager.backgroundBetweenScanPeriod = constants.beaconScanIntervalLocationSdkRunningMs
        try {
            beaconManager.updateScanPeriods()
        } catch (e: RemoteException) {
            throw RuntimeException(e)
        }
    }

    /**
     * It decreases the time interval between two consecutive beacon scan so that it starts scanning
     * frequently. We call it once we are stopping location sdk.
     */
    fun decreaseBeaconScanPeriod(beaconManager: BeaconManager){
        beaconManager.backgroundBetweenScanPeriod = constants.beaconScanIntervalLocationSdkNotRunningMs
        try {
            beaconManager.updateScanPeriods()
        } catch (e: RemoteException) {
            throw RuntimeException(e)
        }
    }
}