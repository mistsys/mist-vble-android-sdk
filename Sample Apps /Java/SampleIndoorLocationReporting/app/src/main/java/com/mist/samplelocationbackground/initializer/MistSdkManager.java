package com.mist.samplelocationbackground.initializer;

import android.content.Context;
import android.widget.Toast;

import com.mist.android.BatteryUsage;
import com.mist.android.IndoorLocationCallback;
import com.mist.android.IndoorLocationManager;
import com.mist.android.external.config.LogLevel;
import com.mist.android.external.config.MistConfiguration;

import java.lang.ref.WeakReference;

/**
 * MistSdkInitializer This class provides utility function to start the Mist SDK.
 */
public class MistSdkManager {
    /**
     * Required by Mist SDK for initialization IndoorLocationManager IndoorLocationCallback
     * VirtualBeaconCallback
     */
    private IndoorLocationManager indoorLocationManager;
    private static WeakReference<Context> contextWeakReference;
    private String envType, orgSecret, orgId;
    private static MistSdkManager mistSdkManager;
    private MistConfiguration mistConfiguration;
    private IndoorLocationCallback indoorLocationCallback;

    private MistSdkManager() {
    }

    public static MistSdkManager getInstance(Context context) {
        contextWeakReference = new WeakReference<>(context);
        if (mistSdkManager == null) {
            mistSdkManager = new MistSdkManager();
        }
        return mistSdkManager;
    }

    public void init(String orgSecret, String orgId, IndoorLocationCallback indoorLocationCallback) {
        if (orgSecret != null && !orgSecret.isEmpty()) {
            this.orgSecret = orgSecret;
            this.orgId = orgId;
            this.indoorLocationCallback = indoorLocationCallback;
            this.envType = String.valueOf(orgSecret.charAt(0));
            this.mistConfiguration = new MistConfiguration(
                    contextWeakReference.get(),
                    this.orgSecret,
                    this.orgId,
                    "",
                    LogLevel.INFO,
                    true,
                    BatteryUsage.HIGH_BATTERY_USAGE_HIGH_ACCURACY
            );
        } else {
            Toast.makeText(contextWeakReference.get(), "Org Secret not present", Toast.LENGTH_SHORT).show();
        }
    }

    public synchronized void startMistSDK() {
        if (indoorLocationManager == null) {
            indoorLocationManager = IndoorLocationManager.INSTANCE;
            indoorLocationManager.start(mistConfiguration, indoorLocationCallback);
        } else {
            restartMistSDK();
        }
    }

    void stopMistSDK() {
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
