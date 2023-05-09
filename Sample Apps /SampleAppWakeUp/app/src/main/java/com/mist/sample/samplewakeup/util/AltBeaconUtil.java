package com.mist.sample.samplewakeup.util;

import static com.mist.sample.samplewakeup.Constants.BEACON_PER_SCAN_DURATION;
import static com.mist.sample.samplewakeup.Constants.BEACON_SCAN_INTERVAL_LOCATION_SDK_NOT_RUNNING_MS;
import static com.mist.sample.samplewakeup.Constants.BEACON_SCAN_INTERVAL_LOCATION_SDK_RUNNING_MS;
import static com.mist.sample.samplewakeup.Constants.ORG_ID;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.RemoteException;
import android.widget.Toast;
import androidx.core.app.NotificationCompat;
import com.mist.sample.samplewakeup.MainActivity;
import com.mist.sample.samplewakeup.R;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.Region;
import java.util.ArrayList;

public class AltBeaconUtil {
    static ArrayList<Region> regions = new ArrayList<>();

    public static void startBeaconMonitor(BeaconManager beaconManager, Context context) {
        beaconManager.getBeaconParsers().clear(); /* The example shows how to find iBeacon.*/
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
        beaconManager.setEnableScheduledScanJobs(true);
        Intent intent1 = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pendingIntent = PendingIntent.getActivity(context, 0, intent1, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        } else {
            pendingIntent = PendingIntent.getActivity(context, 0, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        /** Create foreground service for alt beacon scanning. */
        Notification notification = new NotificationCompat.Builder(context, "ChannelId2").setContentTitle("Sample App Wakeup").setContentText("Alt beacon scan service running").setSmallIcon(R.drawable.ic_launcher_foreground).setContentIntent(pendingIntent).build();
        beaconManager.addMonitorNotifier((MonitorNotifier) context);
        beaconManager.enableForegroundServiceScanning(notification, 71);
        beaconManager.setBackgroundBetweenScanPeriod(BEACON_SCAN_INTERVAL_LOCATION_SDK_NOT_RUNNING_MS);
        beaconManager.setForegroundBetweenScanPeriod(BEACON_SCAN_INTERVAL_LOCATION_SDK_NOT_RUNNING_MS);
        beaconManager.setBackgroundScanPeriod(BEACON_PER_SCAN_DURATION);
        beaconManager.setForegroundScanPeriod(BEACON_PER_SCAN_DURATION);
        String subOrgId = ORG_ID.substring(0, ORG_ID.length() - 2);
        regions.add(new Region("wakeup-beacons1", Identifier.parse(ORG_ID), null, null));
        regions.add(new Region("wakeup-beacons2", Identifier.parse(subOrgId + "00"), null, null));
        regions.add(new Region("wakeup-beacons3", Identifier.parse(subOrgId + "01"), null, null));
        regions.add(new Region("wakeup-beacons4", Identifier.parse(subOrgId + "02"), null, null));
        regions.add(new Region("wakeup-beacons5", Identifier.parse(subOrgId + "03"), null, null));
        regions.add(new Region("wakeup-beacons6", Identifier.parse(subOrgId + "04"), null, null));
        regions.add(new Region("wakeup-beacons7", Identifier.parse(subOrgId + "05"), null, null));
        regions.add(new Region("wakeup-beacons8", Identifier.parse(subOrgId + "06"), null, null));
        regions.add(new Region("wakeup-beacons9", Identifier.parse(subOrgId + "07"), null, null));
        regions.add(new Region("wakeup-beacons10", Identifier.parse(subOrgId + "08"), null, null));
        regions.add(new Region("wakeup-beacons11", Identifier.parse(subOrgId + "09"), null, null));
        regions.add(new Region("wakeup-beacons12", Identifier.parse(subOrgId + "0a"), null, null));
        regions.add(new Region("wakeup-beacons13", Identifier.parse(subOrgId + "0b"), null, null));
        regions.add(new Region("wakeup-beacons14", Identifier.parse(subOrgId + "0c"), null, null));
        regions.add(new Region("wakeup-beacons15", Identifier.parse(subOrgId + "0d"), null, null));
        Toast.makeText(context, "Monitoring started ", Toast.LENGTH_LONG).show();
        for (Region region : regions) {
            beaconManager.startMonitoring(region);
        }
    }

    /**
     * It Increases the time interval between two consecutive beacon scan so that it doesn't scan
     * frequently. We call it once we are starting location sdk.
     */
    public static void increaseBeaconScanPeriod(BeaconManager beaconManager) {
        beaconManager.setBackgroundBetweenScanPeriod(BEACON_SCAN_INTERVAL_LOCATION_SDK_RUNNING_MS);
        try {
            beaconManager.updateScanPeriods();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * It decreases the time interval between two consecutive beacon scan so that it starts scanning
     * frequently. We call it once we are stopping location sdk.
     */
    public static void decreaseBeaconScanPeriod(BeaconManager beaconManager) {
        beaconManager.setBackgroundBetweenScanPeriod(BEACON_SCAN_INTERVAL_LOCATION_SDK_NOT_RUNNING_MS);
        try {
            beaconManager.updateScanPeriods();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }
}
