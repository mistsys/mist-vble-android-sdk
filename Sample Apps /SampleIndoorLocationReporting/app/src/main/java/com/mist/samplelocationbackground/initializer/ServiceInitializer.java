package com.mist.samplelocationbackground.initializer;

import static androidx.core.content.ContextCompat.startForegroundService;
import static com.mist.samplelocationbackground.Constants.ORG_SECRET;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import com.mist.samplelocationbackground.service.LocationForegroundService;
import com.mist.samplelocationbackground.service.LocationJobService;

/**
 * ServiceInitializer This class provides utility functions to start the android services.
 */
public class ServiceInitializer {

    /* Define your SDK job ID here.*/
    public static final int MIST_SDK_JOB_ID = 789;

    public enum BackgroundServiceType {SCHEDULE_SERVICE, FOREGROUND_SERVICE}

    public static BackgroundServiceType BACKGROUND = BackgroundServiceType.SCHEDULE_SERVICE;

    public static void startLocationService(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            BACKGROUND = BackgroundServiceType.FOREGROUND_SERVICE;
            startMistForegroundService(context);
        } else {
            scheduleJob(context);
        }
    }

    public static void stopLocationService(Context context) {
        if (BACKGROUND == BackgroundServiceType.SCHEDULE_SERVICE) {
            stopScheduledJob(context);
        } else if (BACKGROUND == BackgroundServiceType.FOREGROUND_SERVICE) {
            stopMistForegroundService(context);
        }
    }

    /**
     * This is an API for scheduling various types of jobs against the framework that will be
     * executed in your application's own process.
     * https://developer.android.com/reference/android/app/job/JobService
     * https://developer.android.com/reference/android/app/job/JobScheduler
     */
    public static void scheduleJob(Context context) throws NullPointerException {
        ComponentName serviceComponent = new ComponentName(context, LocationJobService.class);
        JobInfo.Builder builder = new JobInfo.Builder(MIST_SDK_JOB_ID, serviceComponent).setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY).setPersisted(true);
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if (jobScheduler != null) {
            LocationJobService.needJobReschedule(true);
            jobScheduler.schedule(builder.build());
        } else {
            throw new NullPointerException("JobScheduler Service is null");
        }
    }
    /** stop scheduled job */

    public static void stopScheduledJob(Context context) throws NullPointerException {
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if (jobScheduler != null) {
            LocationJobService.needJobReschedule(false);
            jobScheduler.cancel(MIST_SDK_JOB_ID);
        } else {
            throw new NullPointerException("JobScheduler Service is null");
        }
    }

    private static void startMistForegroundService(Context context) {
        Intent intent = new Intent(context, LocationForegroundService.class).putExtra("ORG_SECRET", ORG_SECRET);
        startForegroundService(context, intent);
    }

    public static void stopMistForegroundService(Context context) {
        Intent intent = new Intent(context, LocationForegroundService.class);
        context.stopService(intent);
    }
}
