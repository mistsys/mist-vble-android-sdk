package com.example.sampleappwakeup.service

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.sampleappwakeup.Constants
import com.example.sampleappwakeup.MainActivity
import com.example.sampleappwakeup.R

/**
 * LocationForegroundService
 * This class represents foreground service logic for location SDK.
 * Foreground services perform operations that are noticeable to the user.
 * Foreground services show a status bar notification,
 * so that users are actively aware that your app is performing a task in the foreground
 * and is consuming system resources.
 * https://developer.android.com/guide/components/foreground-services
 */
class LocationForegroundService : Service() {

    private val fgServiceNotificationId : Int = 73
    private val constants = Constants()

    /**
     *  If someone calls Context.startService() then the system will retrieve the service (creating it and calling its onCreate() method if needed) and then call its onStartCommand(Intent, int, int) method with the arguments supplied by the client.
     *  The service will at this point continue running until Context.stopService() or stopSelf() is called.
     *  Note that multiple calls to Context.startService() do not nest (though they do result in multiple corresponding calls to onStartCommand()),
     */

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            createNotificationChannel()
            val intent1 = Intent(this, MainActivity::class.java)
            val pendingIntent: PendingIntent = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.getActivity(this, 0, intent1, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            } else {
                PendingIntent.getActivity(this, 0, intent1, PendingIntent.FLAG_UPDATE_CURRENT)
            }
            val notification = NotificationCompat.Builder(this, "ChannelId3")
                .setContentTitle("Sample App Wakeup")
                .setContentText("location sdk running")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .build()
            startForeground(fgServiceNotificationId, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
            startSdk(constants.orgSecret)
        }
        /**
         * If the service is killed then system will try to recreate the service by calling onStartCommand with null intent
         */
        return START_NOT_STICKY
    }

    private fun startSdk(orgSecret: String) {
        val mistSdkManager = MistSdkManager()
        mistSdkManager.getInstance(applicationContext)
        val sdkCallbackHandler = SDKCallbackHandler(applicationContext)
        mistSdkManager.init(orgSecret, sdkCallbackHandler, sdkCallbackHandler,applicationContext)
        mistSdkManager.startMistSDK()
    }

    // https://developer.android.com/develop/ui/views/notifications/channels
    private fun createNotificationChannel() {
        //Check if OS is Oreo and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel("ChannelId3", "Foreground notification", NotificationManager.IMPORTANCE_DEFAULT)
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(notificationChannel)
        }
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        stopForeground(true)
        stopSelf()
        super.onDestroy()
        destroy()
    }

    private fun destroy() {
        val mistSdkManager= MistSdkManager()
        mistSdkManager.getInstance(application as Application)
        mistSdkManager.destroy()
    }

}