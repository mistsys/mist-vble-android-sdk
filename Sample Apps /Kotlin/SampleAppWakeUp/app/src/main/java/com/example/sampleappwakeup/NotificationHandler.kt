package com.example.sampleappwakeup

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class NotificationHandler {

    fun createNotificationChannel(context: Context){
        /* Check if OS is Oreo and above */
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val notificationChannel = NotificationChannel("ChannelId2","Sample App Wakeup", NotificationManager.IMPORTANCE_DEFAULT )
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(notificationChannel)
        }
    }

    fun sendNotification(context: Context, text : String){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            createNotificationChannel(context)
            val intent1 = Intent(context, MainActivity::class.java)
            val pendingIntent : PendingIntent = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                PendingIntent.getActivity(context, 0, intent1, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            } else{
                PendingIntent.getActivity(context,0,intent1, PendingIntent.FLAG_UPDATE_CURRENT)
            }
            val notification : Notification = NotificationCompat.Builder(context,"ChannelId2").setContentTitle("Sample App Wakeup").setContentText(text).setSmallIcon(R.drawable.ic_launcher_foreground).setContentIntent(pendingIntent).build()
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(0,notification)
        }
    }
}