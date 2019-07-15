package com.mist.sample.background.utils;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;

import com.mist.sample.background.service.MSTSDKBackgroundService;

public class BackgroundManager {

    /**
     * Schedule a job with given job ID
     *
     * @param context
     * @param jobID
     * @throws NullPointerException
     */
    public static void scheduleJob(Context context, int jobID) throws NullPointerException {
        JobInfo jobInfo = new JobInfo.Builder(jobID, new ComponentName(context, MSTSDKBackgroundService.class))
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY) // require unmetered network
                .setPersisted(true).build();
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if (jobScheduler != null) {
            MSTSDKBackgroundService.needJobReschedule(true);
            jobScheduler.schedule(jobInfo);
        } else {
            throw new NullPointerException("JobScheduler Service is null");
        }
    }

    /**
     * Stop a schedule job with a given job ID
     *
     * @param context
     * @param jobID
     * @throws NullPointerException
     */
    public static void stopScheduledJob(Context context, int jobID) throws NullPointerException {
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if (jobScheduler != null) {
            MSTSDKBackgroundService.needJobReschedule(false);
            jobScheduler.cancel(jobID);
        } else {
            throw new NullPointerException("JobScheduler Service is null");
        }
    }

}
