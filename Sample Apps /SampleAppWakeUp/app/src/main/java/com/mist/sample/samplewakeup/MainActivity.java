package com.mist.sample.samplewakeup;

import android.os.Bundle;
import com.mist.sample.samplewakeup.application.MainApplication;
import com.mist.sample.samplewakeup.util.AltBeaconUtil;
import org.altbeacon.beacon.BeaconManager;

public class MainActivity extends PermissionHandlerActivity {
    private static final String TAG = "SampleWakeUp";
    private static MainApplication mainApplication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainApplication = MainApplication.getApplication();
        checkPermissions();
        BeaconManager.setDebug(true);
    }

    @Override
    public void startBeaconMonitor() {
        AltBeaconUtil.startBeaconMonitor(mainApplication.getBeaconManager(), mainApplication);
    }
}