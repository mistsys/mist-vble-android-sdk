package com.mist.samplelocationbackground.initializer;

import android.content.Context;
import android.widget.Toast;

import com.mist.android.IndoorLocationCallback;
import com.mist.android.IndoorLocationManager;
import com.mist.android.VirtualBeaconCallback;

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
    private IndoorLocationCallback indoorLocationCallback;
    private VirtualBeaconCallback virtualBeaconCallback;
    private static WeakReference<Context> contextWeakReference;
    private String envType, orgSecret;
    private static MistSdkManager mistSdkManager;

    private MistSdkManager() {
    }

    public static MistSdkManager getInstance(Context context) {
        contextWeakReference = new WeakReference<>(context);
        if (mistSdkManager == null) {
            mistSdkManager = new MistSdkManager();
        }
        return mistSdkManager;
    }

    public void init(String orgSecret, IndoorLocationCallback indoorLocationCallback, VirtualBeaconCallback virtualBeaconCallback) {
        if (orgSecret != null && !orgSecret.isEmpty()) {
            this.orgSecret = orgSecret;
            this.envType = String.valueOf(orgSecret.charAt(0));
            this.indoorLocationCallback = indoorLocationCallback;
            this.virtualBeaconCallback = virtualBeaconCallback;
        } else {
            Toast.makeText(contextWeakReference.get(), "Org Secret not present", Toast.LENGTH_SHORT).show();
        }
    }

    public synchronized void startMistSDK() {
        if (indoorLocationManager == null) {
            indoorLocationManager = IndoorLocationManager.getInstance(contextWeakReference.get(), orgSecret);
            indoorLocationManager.setVirtualBeaconCallback(virtualBeaconCallback);
            indoorLocationManager.start(indoorLocationCallback);
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
            indoorLocationManager = null;
        }
    }

    private synchronized void restartMistSDK() {
        if (indoorLocationManager != null) {
            stopMistSDK();
            indoorLocationManager.setVirtualBeaconCallback(virtualBeaconCallback);
            indoorLocationManager.start(indoorLocationCallback);
        }
    }
}
