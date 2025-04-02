package com.example.sampleindoorlocationreporting.application

import android.app.Application
import com.example.sampleindoorlocationreporting.NotificationHandler

class MainApplication : Application() {

    /**
     * Initializes the application, setting up any necessary global state.
     * For example, creating a notification channel.
     */
    override fun onCreate() {
        super.onCreate()
        NotificationHandler().createNotificationChannel(applicationContext)
    }

}
