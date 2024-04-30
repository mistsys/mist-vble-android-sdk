package com.example.sampleindoorlocationreporting.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.sampleindoorlocationreporting.Constants
import com.example.sampleindoorlocationreporting.MainActivity
import com.example.sampleindoorlocationreporting.handler.SDKCallbackHandler
import com.example.sampleindoorlocationreporting.initializer.MistSdkManager

class LocationForegroundService : Service() {
    private val constants = Constants()
    private val fgServiceNotificationId = 72

    /**
     * If someone calls Context.startService() then the system will retrieve the service (creating
     * it and calling its onCreate() method if needed) and then call its onStartCommand(Intent, int,
     * int) method with the arguments supplied by the client. The service will at this point
     * continue running until Context.stopService() or stopSelf() is called. Note that multiple
     * calls to Context.startService() do not nest (though they do result in multiple corresponding
     * calls to onStartCommand()),
     */

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            createNotificationChannel()
            val intent1 = Intent(this, MainActivity::class.java)
            val pendingIntent: PendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.getActivity(this, 0, intent1, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            } else {
                PendingIntent.getActivity(this, 0, intent1, PendingIntent.FLAG_UPDATE_CURRENT)
            }
            val notification = NotificationCompat.Builder(this, "ChannelId1")
                .setContentTitle("Sample Location and Blue dot  App")
                .setContentText("Location App  is running !!").setContentIntent(pendingIntent)
                .build()
            startForeground(fgServiceNotificationId, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
            startSdk(constants.orgSecret)
        }
        /**
         * If the service is killed then system will try to recreate the service by calling onStartCommand with null intent
         */
        return START_STICKY
    }

    private fun startSdk(orgSecret: String?) {
        if (orgSecret != null) {
            val mistSdkManager= MistSdkManager()
            mistSdkManager.getInstance(applicationContext)
            val sdkCallbackHandler = SDKCallbackHandler(applicationContext)
            mistSdkManager.init(orgSecret, sdkCallbackHandler)
            if (!orgSecret.isEmpty()) {
                mistSdkManager.startMistSDK()
            }
        }
    }

    /** https://developer.android.com/develop/ui/views/notifications/channels */
    private fun createNotificationChannel() {
        /** Check if OS is Oreo and above */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel("ChannelId1", "Foreground notification", NotificationManager.IMPORTANCE_DEFAULT)
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(notificationChannel)
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onDestroy() {
        stopForeground(true)
        stopSelf()
        super.onDestroy()
        destroy()
    }

    private fun destroy() {
        val mistSdkManager = MistSdkManager()
        mistSdkManager.getInstance(applicationContext)
        mistSdkManager.destroy()
    }
}