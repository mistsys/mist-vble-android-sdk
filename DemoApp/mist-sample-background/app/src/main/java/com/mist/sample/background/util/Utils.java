package com.mist.sample.background.util;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;

import com.mist.sample.background.service.MISTSDKBackgroundService;

/**
 * Created by anubhava on 02/04/18.
 */

public class Utils {

    private static final int MIST_SDK_JOB_ID = 100;
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
        } else if (envType.equalsIgnoreCase("S")) {
            env = "Staging";
        } else if (envType.equalsIgnoreCase("K")) {
            env = "Kalam";
        } else {
            env = "Dev";
        }
        // return the environment string
        return env;
    }

    public static boolean isEmptyString(String value) {
        return TextUtils.isEmpty(value) || value.equalsIgnoreCase("null");
    }

    // schedule the start of the service
    public static void scheduleJob(Context context) throws NullPointerException {
        ComponentName serviceComponent = new ComponentName(context, MISTSDKBackgroundService.class);
        JobInfo.Builder builder = new JobInfo.Builder(MIST_SDK_JOB_ID, serviceComponent);
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);// require unmetered network
        builder.setPersisted(true);

        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if (jobScheduler != null) {
            MISTSDKBackgroundService.needJobReschedule(true);
            jobScheduler.schedule(builder.build());
        } else {
            throw new NullPointerException("JobScheduler Service is null");
        }
    }

    // stop scheduled job
    public static void stopScheduledJob(Context context) throws NullPointerException {
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if (jobScheduler != null) {
            MISTSDKBackgroundService.needJobReschedule(false);
            jobScheduler.cancel(MIST_SDK_JOB_ID);
        } else {
            throw new NullPointerException("JobScheduler Service is null");
        }
    }
}
