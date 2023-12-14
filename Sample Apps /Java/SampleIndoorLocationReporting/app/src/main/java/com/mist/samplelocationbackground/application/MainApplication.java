package com.mist.samplelocationbackground.application;

import android.app.Application;

import com.mist.samplelocationbackground.NotificationHandler;

public class MainApplication extends Application {
    /**
     * https://developer.android.com/reference/android/app/Application Base class for maintaining
     * global application state. You can provide your own implementation by creating a subclass and
     * specifying the fully-qualified name of this subclass as the "android:name" attribute in your
     * AndroidManifest.xml's <application> tag. The Application class, or your subclass of the
     * Application class, is instantiated before any other class when the process for your
     * application/package is created.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        NotificationHandler.createNotificationChannel(getApplicationContext());
    }
}
