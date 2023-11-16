package com.mist.sample.bluedot.initializer;

import android.content.Context;
import android.util.Log;

import com.mist.android.ClientInformationCallback;
import com.mist.android.IndoorLocationCallback;
import com.mist.android.IndoorLocationManager;
import com.mist.android.VirtualBeaconCallback;

import java.lang.ref.WeakReference;

public class MistSdkManager{
    /**
     * Required by Mist SDK for initialization
     * IndoorLocationManager
     * IndoorLocationCallback
     * VirtualBeaconCallback
     */
    private IndoorLocationManager indoorLocationManager;
    private IndoorLocationCallback indoorLocationCallback;
    private VirtualBeaconCallback virtualBeaconCallback;

    private ClientInformationCallback clientInformationCallback;

    private static WeakReference<Context> contextWeakReference;
    private String envType, orgSecret;
    private static MistSdkManager sdkInitializer;

    private MistSdkManager() {
    }

    public static MistSdkManager getInstance(Context context) {
        contextWeakReference = new WeakReference<>(context);
        if (sdkInitializer == null) {
            sdkInitializer = new MistSdkManager();
        }
        return sdkInitializer;
    }

    public void init(String orgSecret, IndoorLocationCallback indoorLocationCallback, VirtualBeaconCallback virtualBeaconCallback, ClientInformationCallback clientInformationCallback) {
        if (orgSecret != null && !orgSecret.isEmpty()) {
            Log.d("", "SampleBlueDot init" + orgSecret);
            this.orgSecret = orgSecret;
            this.indoorLocationCallback = indoorLocationCallback;
            this.virtualBeaconCallback = virtualBeaconCallback;
            this.clientInformationCallback = clientInformationCallback;
        }
    }

    public synchronized void startMistSDK() {
        if (indoorLocationManager == null) {
            Log.d("", "indoorLocationManager start" + orgSecret);
            indoorLocationManager = IndoorLocationManager.getInstance(contextWeakReference.get(), orgSecret);
            indoorLocationManager.getClientInformation(clientInformationCallback);
            indoorLocationManager.setVirtualBeaconCallback(virtualBeaconCallback);
            indoorLocationManager.start(indoorLocationCallback);
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
            indoorLocationManager = null;
        }
    }

    public void updateClientName(String clientName){
        indoorLocationManager.saveClientInformation(clientName,clientInformationCallback);
    }

    private synchronized void restartMistSDK() {
        if (indoorLocationManager != null) {
            stopMistSDK();
            indoorLocationManager.setVirtualBeaconCallback(virtualBeaconCallback);
            indoorLocationManager.getClientInformation(clientInformationCallback);
            indoorLocationManager.start(indoorLocationCallback);
        }
    }
}
