package com.mist.sample.wakeup.service;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.util.Log;

import com.mist.android.AppMode;
import com.mist.android.BatteryUsage;
import com.mist.android.model.AppModeParams;
import com.mist.sample.wakeup.app.MainApplication;
import com.mist.sample.wakeup.utils.MistManager;
import com.mist.sample.wakeup.utils.SharedPrefUtils;
import com.mist.sample.wakeup.utils.Utils;

public class MistSdkBackgroundService extends JobService {

    private static final String TAG = MistSdkBackgroundService.class.getSimpleName();
    private static boolean needReschedule = true;

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d(TAG, "Job Started !");
        startWorkOnNewThread();
        return true;
    }

    private void startWorkOnNewThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                doWork();
            }
        }, TAG).start();
    }

    private void doWork() {
        String sdkToken = SharedPrefUtils.readSdkToken(getApplication(), Utils.TOKEN_PREF_KEY_NAME);

        if (sdkToken != null) {
            MistManager.newInstance((MainApplication) getApplication()).
                    init(sdkToken, null, AppMode.BACKGROUND);

            MistManager.newInstance((MainApplication) getApplication()).
                    setAppMode(new AppModeParams(AppMode.BACKGROUND,
                            BatteryUsage.LOW_BATTERY_USAGE_LOW_ACCURACY,
                            true, 0.5, 1));
        }
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(TAG, "Job stopped before being completed !!");
        jobFinished(params, needReschedule);
        return needReschedule;
    }

    public static void needJobReschedule(boolean needReschedule) {
        MistSdkBackgroundService.needReschedule = needReschedule;
    }

}
