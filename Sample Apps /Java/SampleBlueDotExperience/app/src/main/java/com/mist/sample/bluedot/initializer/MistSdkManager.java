package com.mist.sample.bluedot.initializer;

import android.content.Context;
import android.util.Log;

import com.mist.android.BatteryUsage;
import com.mist.android.IndoorLocationCallback;
import com.mist.android.IndoorLocationManager;
import com.mist.android.VirtualBeaconCallback;
import com.mist.android.external.config.LogLevel;
import com.mist.android.external.config.MistCallbacks;
import com.mist.android.external.config.MistConfiguration;

import java.lang.ref.WeakReference;

public class MistSdkManager {
    /**
     * Required by Mist SDK for initialization
     * IndoorLocationManager
     * IndoorLocationCallback
     * VirtualBeaconCallback
     */
    private IndoorLocationManager indoorLocationManager;
    private static WeakReference<Context> contextWeakReference;
    private String envType, orgSecret;
    private static MistSdkManager sdkInitializer;
    private MistConfiguration mistConfiguration;
    private MistCallbacks mistCallbacks;

    private MistSdkManager() {
    }

    public static MistSdkManager getInstance(Context context) {
        contextWeakReference = new WeakReference<>(context);
        if (sdkInitializer == null) {
            sdkInitializer = new MistSdkManager();
        }
        return sdkInitializer;
    }

    public void init(String orgSecret, IndoorLocationCallback indoorLocationCallback, VirtualBeaconCallback virtualBeaconCallback) {
        if (orgSecret != null && !orgSecret.isEmpty()) {
            Log.d("", "SampleBlueDot init" + orgSecret);
            this.orgSecret = orgSecret;
            this.mistCallbacks = new MistCallbacks(
                    indoorLocationCallback,
                    null,
                    virtualBeaconCallback,
                    null,
                    null
            );
            this.mistConfiguration = new MistConfiguration(
                    contextWeakReference.get(),
                    this.orgSecret,
                    "",
                    LogLevel.INFO,
                    true,
                    BatteryUsage.HIGH_BATTERY_USAGE_HIGH_ACCURACY
            );
        }
    }

    public synchronized void startMistSDK() {
        if (indoorLocationManager == null) {
            indoorLocationManager = IndoorLocationManager.INSTANCE;
            indoorLocationManager.setMistCallbacks(mistCallbacks);
            indoorLocationManager.start(mistConfiguration);
        }
        else {
            restartMistSDK();
        }
    }

    public void stopMistSDK() {
        if (indoorLocationManager != null) {
            indoorLocationManager.stop();
        }
    }

    public void destroy() {
        if (indoorLocationManager != null) {
            indoorLocationManager.stop();
        }
    }

    private synchronized void restartMistSDK() {
        if (indoorLocationManager != null) {
            stopMistSDK();
            indoorLocationManager.start(mistConfiguration);
        }
    }
}
