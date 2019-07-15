package com.mist.sample.background.service;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.util.Log;

import com.mist.android.AppMode;
import com.mist.android.BatteryUsage;
import com.mist.android.model.AppModeParams;
import com.mist.sample.background.app.MainApplication;
import com.mist.sample.background.utils.MistManager;
import com.mist.sample.background.utils.SharedPrefUtils;
import com.mist.sample.background.utils.Utils;

public class MSTSDKBackgroundService extends JobService {

    private static final String TAG = MSTSDKBackgroundService.class.getSimpleName();
    private static boolean needReschedule = true;

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d(TAG, "Job started.");
        startWorkOnNewThread(params);
        return true;
    }

    private void startWorkOnNewThread(final JobParameters params) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                doWork(params);
            }
        }, TAG).start();
    }

    private void doWork(JobParameters params) {
        String sdkToken = SharedPrefUtils.readSDKToken(getApplication(), Utils.TOKEN_PREF_KEY_NAME);
        if (!Utils.isValidToken(sdkToken)) {
            Log.e(TAG, "SDK token is invalid. Please check the token and try again. " + sdkToken);
            jobFinished(params, false);
            return;
        }
        MistManager.newInstance((MainApplication) getApplication()).
                setAppMode(new AppModeParams(AppMode.BACKGROUND,
                        BatteryUsage.LOW_BATTERY_USAGE_LOW_ACCURACY,
                        true, 0.5, 1));
        MistManager.newInstance((MainApplication) getApplication()).
                init(sdkToken, null, AppMode.BACKGROUND);
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(TAG, "Job cancelled before being completed.");
        jobFinished(params, needReschedule);
        return needReschedule;
    }

    public static void needJobReschedule(boolean needReschedule) {
        MSTSDKBackgroundService.needReschedule = needReschedule;
    }
}
