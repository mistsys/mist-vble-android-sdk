package com.mist.sample.bluedot.initializer;

import android.content.Context;
import android.util.Log;

import com.mist.android.BatteryUsage;
import com.mist.android.IndoorLocationCallback;
import com.mist.android.IndoorLocationManager;
import com.mist.android.external.config.LogLevel;
import com.mist.android.external.config.MistConfiguration;

import java.lang.ref.WeakReference;

public class MistSdkManager {
    /**
     * Required by Mist SDK for initialization
     * IndoorLocationManager
     * IndoorLocationCallback
     */
    private IndoorLocationManager indoorLocationManager;
    private static WeakReference<Context> contextWeakReference;
    private String envType, orgSecret, orgId;
    private static MistSdkManager sdkInitializer;
    private MistConfiguration mistConfiguration;
    private IndoorLocationCallback indoorLocationCallback;

    private MistSdkManager() {
    }

    public static MistSdkManager getInstance(Context context) {
        contextWeakReference = new WeakReference<>(context);
        if (sdkInitializer == null) {
            sdkInitializer = new MistSdkManager();
        }
        return sdkInitializer;
    }

    public void init(String orgSecret, String orgId, IndoorLocationCallback indoorLocationCallback) {
        if (orgSecret != null && !orgSecret.isEmpty()) {
            Log.d("", "SampleBlueDot init " + orgSecret);
            this.orgSecret = orgSecret;
            this.orgId = orgId;
            this.indoorLocationCallback = indoorLocationCallback;
            this.mistConfiguration = new MistConfiguration(
                    contextWeakReference.get(),
                    this.orgSecret,
                    this.orgId,
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
            indoorLocationManager.saveClientInformation("Anubhav");
            indoorLocationManager.start(mistConfiguration, indoorLocationCallback);
        } else {
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
            indoorLocationManager.start(mistConfiguration, indoorLocationCallback);
        }
    }
}
