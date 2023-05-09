package com.mist.sample.samplewakeup;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import com.mist.sample.samplewakeup.application.MainApplication;
import com.mist.sample.samplewakeup.util.AltBeaconUtil;

public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "SampleWakeUpBroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
            case Intent.ACTION_BOOT_COMPLETED:
                Toast.makeText(context, "SampleWakeUpBroadcastReceiver: ACTION_BOOT_COMPLETED", Toast.LENGTH_SHORT).show();
                MainApplication mainApplication = (MainApplication) context.getApplicationContext();
                AltBeaconUtil.startBeaconMonitor(mainApplication.getBeaconManager(), mainApplication);
                break;
        }
    }
}
