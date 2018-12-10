package com.mist.sample.wakeup.utils;

import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import com.mist.sample.wakeup.R;
import com.mist.sample.wakeup.service.MistSdkBackgroundService;

/**
 * Created by anubhava on 26/03/18.
 */

public class Utils {

    public static final int MIST_SDK_JOB_ID = 100;
    public static final String NOTIFICATION_CHANNEL_ID = "mist_notification";
    public static final String NOTIFICATION_CHANNEL_NAME = "Proximity";
    public static final String NOTIFICATION_CHANNEL_DESC = "You will get the notification when you are in proximity of you org beacons";
    public static final String WELCOME_MESSAGE = "Welcome";
    public static final int WELCOME_NOTIFICATION_ID = 99;
    public static final String TOKEN_PREF_KEY_NAME = "sdkToken";

    /**
     * Check Internet is on or off
     */

    public static boolean isNetworkAvailable(Context context) {
        boolean isNet = false;
        if (context != null) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager != null) {
                NetworkInfo result = connectivityManager.getActiveNetworkInfo();
                if (result != null && result.isConnectedOrConnecting()) {
                    isNet = true;
                }
            }
        }
        return isNet;
    }

    /*checking if location service
      is enabled */

    public static boolean isLocationServiceEnabled(Context context) {
        boolean gps_enabled = false, network_enabled = false;

        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            try {
                gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            } catch (Exception ex) {
                //do nothing...
            }

            try {
                network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            } catch (Exception ex) {
                //do nothing...
            }
        }
        return gps_enabled || network_enabled;
    }

    public static String getEnvironment(String envType) {
        String env;
        // set the environment string to return
        if (envType.equalsIgnoreCase("P")) {
            env = "Production";
        } else if (envType.equalsIgnoreCase("E")) {
            env = "EU";
        } else if (envType.equalsIgnoreCase("K")) {
            env = "Kalam";
        } else {
            env = "Production";
        }
        // return the environment string
        return env;
    }

    public static boolean isEmptyString(String value) {
        return TextUtils.isEmpty(value) || value.equalsIgnoreCase("null");
    }

    // schedule the start of the service
    public static void scheduleJob(Context context) throws NullPointerException {
        ComponentName serviceComponent = new ComponentName(context, MistSdkBackgroundService.class);
        JobInfo.Builder builder = new JobInfo.Builder(MIST_SDK_JOB_ID, serviceComponent);
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);// require unmetered network
        builder.setPersisted(true);

        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if (jobScheduler != null) {
            MistSdkBackgroundService.needJobReschedule(true);
            jobScheduler.schedule(builder.build());
        } else {
            throw new NullPointerException("JobScheduler Service is null");
        }
    }

    // stop scheduled job
    public static void stopScheduledJob(Context context) throws NullPointerException {
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if (jobScheduler != null) {
            MistSdkBackgroundService.needJobReschedule(false);
            jobScheduler.cancel(MIST_SDK_JOB_ID);
        } else {
            throw new NullPointerException("JobScheduler Service is null");
        }
    }

    public static void sendNotification(Context context, PendingIntent contentIntent) {
        NotificationManager mNotificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        //Define sound URI
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        if (Build.VERSION.SDK_INT > 25) {
            registerNotificationChannel(context, NOTIFICATION_CHANNEL_NAME, NOTIFICATION_CHANNEL_DESC);
        }
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                        .setSmallIcon(R.drawable.small_notification)
                        .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
                        .setContentText(WELCOME_MESSAGE)
                        .setLights(Color.RED, 3000, 3000)
                        .setSound(soundUri);
        mBuilder.setContentIntent(contentIntent);
        mBuilder.setAutoCancel(true);
        mBuilder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        if (mNotificationManager != null) {
            mNotificationManager.notify(WELCOME_NOTIFICATION_ID, mBuilder.build());
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    public static void registerNotificationChannel(Context context, String channelName, String channelDescription) {
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel mChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, importance);
        // Configure the notification channel.
        mChannel.setDescription(channelDescription);
        mChannel.enableLights(true);
        // Sets the notification light color for notifications posted to this
        // channel, if the device supports this feature.
        mChannel.setLightColor(Color.RED);
        mChannel.enableVibration(true);
        mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
        if (mNotificationManager != null) {
            mNotificationManager.createNotificationChannel(mChannel);
        }
    }
}
