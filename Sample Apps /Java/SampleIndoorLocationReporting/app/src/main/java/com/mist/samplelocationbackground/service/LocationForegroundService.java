package com.mist.samplelocationbackground.service;

import static com.mist.samplelocationbackground.Constants.ORG_ID;
import static com.mist.samplelocationbackground.Constants.ORG_SECRET;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.mist.samplelocationbackground.MainActivity;
import com.mist.samplelocationbackground.handler.SDKCallbackHandler;
import com.mist.samplelocationbackground.initializer.MistSdkManager;

/**
 * LocationForegroundService This class represents foreground service logic for location SDK.
 * Foreground services perform operations that are noticeable to the user. Foreground services show
 * a status bar notification, so that users are actively aware that your app is performing a task in
 * the foreground and is consuming system resources.
 * https://developer.android.com/guide/components/foreground-services
 */
public class LocationForegroundService extends Service {
    private final int FG_SERVICE_NOTIFICATION_ID = 72;

    /**
     * If someone calls Context.startService() then the system will retrieve the service (creating
     * it and calling its onCreate() method if needed) and then call its onStartCommand(Intent, int,
     * int) method with the arguments supplied by the client. The service will at this point
     * continue running until Context.stopService() or stopSelf() is called. Note that multiple
     * calls to Context.startService() do not nest (though they do result in multiple corresponding
     * calls to onStartCommand()),
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            createNotificationChannel();
            Intent intent1 = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                pendingIntent = PendingIntent.getActivity(this, 0, intent1, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            } else {
                pendingIntent = PendingIntent.getActivity(this, 0, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
            }
            Notification notification = new NotificationCompat.Builder(this, "ChannelId1").setContentTitle("Sample Mist Location App").setContentText("Location App  is running !!").setContentIntent(pendingIntent).build();
            startForeground(FG_SERVICE_NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC);
            startSdk(ORG_SECRET, ORG_ID);
        }
        /**
         * If the service is killed then system will try to recreate the service by calling onStartCommand with null intent
         **/
        return START_STICKY;
    }

    private void startSdk(String orgSecret, String orgId) {
        if (orgSecret != null) {
            MistSdkManager mistSdkManager = MistSdkManager.getInstance(getApplicationContext());
            SDKCallbackHandler sdkCallbackHandler = new SDKCallbackHandler(getApplicationContext());
            mistSdkManager.init(orgSecret, orgId, sdkCallbackHandler);
            if(!orgSecret.isEmpty()){
                mistSdkManager.startMistSDK();
            }
        }
    }

    /** https://developer.android.com/develop/ui/views/notifications/channels */
    private void createNotificationChannel() {
        /** Check if OS is Oreo and above */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel("ChannelId1", "Foreground notification", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(notificationChannel);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
        stopSelf();
        super.onDestroy();
    }

    private void destroy() {
        MistSdkManager mistSdkManager = MistSdkManager.getInstance((Application) getApplication());
        if (mistSdkManager != null) {
            mistSdkManager.destroy();
        }
    }
}
