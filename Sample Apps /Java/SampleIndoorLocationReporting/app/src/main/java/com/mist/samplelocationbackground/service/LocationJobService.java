package com.mist.samplelocationbackground.service;

import static com.mist.samplelocationbackground.Constants.ORG_SECRET;

import android.app.Application;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.mist.samplelocationbackground.MainActivity;
import com.mist.samplelocationbackground.handler.SDKCallbackHandler;
import com.mist.samplelocationbackground.initializer.MistSdkManager;

/**
 * LocationJobService This class implements logic for location job service which is used by job
 * scheduler. This class Handles system broadcast event to restart the location service
 */
public class LocationJobService extends JobService {
    private static boolean needReschedule = true;

    @Override
    public boolean onStartJob(JobParameters params) {
        startWorkOnNewThread();
        return true;
    }

    private void startWorkOnNewThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                doWork();
            }
        }, "LocationJobService").start();
    }

    private void doWork() {
        MistSdkManager mistSdkManager = MistSdkManager.getInstance((Application) getApplication());
        SDKCallbackHandler sdkCallbackHandler = new SDKCallbackHandler(getApplicationContext());
        mistSdkManager.init(ORG_SECRET, sdkCallbackHandler);
        mistSdkManager.startMistSDK();
        Log.d("TAG", "SampleLocationApp: doWork() ThreadName: " + Thread.currentThread().getName());
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        jobFinished(params, needReschedule);
        return needReschedule;
    }

    public static void needJobReschedule(boolean needReschedule) {
        LocationJobService.needReschedule = needReschedule;
    }
}
