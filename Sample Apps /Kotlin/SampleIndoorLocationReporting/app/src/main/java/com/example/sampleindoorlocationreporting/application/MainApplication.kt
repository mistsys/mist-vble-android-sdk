package com.example.sampleindoorlocationreporting.application

import android.app.Application
import com.example.sampleindoorlocationreporting.NotificationHandler

class MainApplication : Application() {
    /**
     * https://developer.android.com/reference/android/app/Application Base class for maintaining
     * global application state. You can provide your own implementation by creating a subclass and
     * specifying the fully-qualified name of this subclass as the "android:name" attribute in your
     * AndroidManifest.xml's <application> tag. The Application class, or your subclass of the
     * Application class, is instantiated before any other class when the process for your
     * application/package is created.
     */
    override fun onCreate() {
        super.onCreate()
        NotificationHandler().createNotificationChannel(applicationContext)
    }
}