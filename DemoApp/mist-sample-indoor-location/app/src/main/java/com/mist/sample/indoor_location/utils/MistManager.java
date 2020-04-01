package com.mist.sample.indoor_location.utils;

import android.app.Application;
import android.text.TextUtils;
import android.widget.Toast;

import com.mist.android.AppMode;
import com.mist.android.BatteryUsage;
import com.mist.android.MSTCentralManager;
import com.mist.android.MSTCentralManagerIndoorOnlyListener;
import com.mist.android.MSTOrgCredentialsCallback;
import com.mist.android.MSTOrgCredentialsManager;
import com.mist.android.MistLocationAdvanceListener;
import com.mist.android.model.AppModeParams;
import com.mist.sample.indoor_location.app.MainApplication;
import com.mist.sample.indoor_location.model.OrgData;

import java.lang.ref.WeakReference;
import java.util.Date;

/**
 * Created by anubhava on 26/03/18.
 */

/**
 * This is the interactor class which will interact with Mist SDK for
 * Enrollment
 * starting Mist SDK
 * stopping Mist SDK
 * Reconnection
 * Setting Mode
 */
public class MistManager implements MSTOrgCredentialsCallback {

    private static WeakReference<Application> mApp;
    private static MistManager mistManager;
    private String sdkToken;
    private String envType;
    private MSTCentralManagerIndoorOnlyListener indoorOnlyListener;
    private MistLocationAdvanceListener advanceListener;
    private AppMode appMode = AppMode.FOREGROUND;
    private OrgData orgData;
    private MSTOrgCredentialsManager mstOrgCredentialsManager;
    private volatile MSTCentralManager mstCentralManager;

    private MistManager() {
    }

    /**
     * Constructor for creating singleton instance of the interactor class
     *
     * @param mainApplication application instance needed by Mist SDK
     * @return
     */
    public static MistManager newInstance(MainApplication mainApplication) {
        mApp = new WeakReference<Application>(mainApplication);
        if (mistManager == null) {
            mistManager = new MistManager();
        }
        return mistManager;
    }

    /**
     * This method will enroll the device and start the Mist SDK on successful enrollment, if we already have the detail of enrollment response detail we can just start the SDK with those details
     *
     * @param sdkToken           Token used for enrollment
     * @param indoorOnlyListener listener on which callback for location,map,notification can be heard
     * @param advanceListener    listener on which callback for lat, lon, speed can be heard
     * @param appMode            mode of the app (Background,Foreground)
     */
    public void init(String sdkToken, MSTCentralManagerIndoorOnlyListener indoorOnlyListener,
                     MistLocationAdvanceListener advanceListener,
                     AppMode appMode) {
        if (sdkToken != null && !sdkToken.isEmpty()) {
            this.sdkToken = sdkToken;
            this.envType = String.valueOf(sdkToken.charAt(0));
            this.indoorOnlyListener = indoorOnlyListener;
            this.advanceListener = advanceListener;
            this.appMode = appMode;
            orgData = SharedPrefUtils.readConfig(mApp.get(), sdkToken);
            if (orgData == null || orgData.getSdkSecret() == null || orgData.getSdkSecret().isEmpty()) {
                MSTOrgCredentialsManager.enrollDeviceWithToken(mApp.get(), sdkToken, this);
            } else {
                connect(indoorOnlyListener, advanceListener, appMode);
            }
        } else {
            Toast.makeText(mApp.get(), "Empty SDK Token", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * This method is used to start the Mist SDk
     *
     * @param indoorOnlyListener listener on which callback for location,map,notification can be heard
     * @param appMode            mode of the app (Background,Foreground)
     */
    private synchronized void connect(MSTCentralManagerIndoorOnlyListener indoorOnlyListener, MistLocationAdvanceListener advanceListener, AppMode appMode) {
        if (mstCentralManager == null) {
            mstCentralManager = new MSTCentralManager(mApp.get(), orgData.getOrgId(), orgData.getSdkSecret());
            mstCentralManager.setMSTCentralManagerIndoorOnlyListener(indoorOnlyListener);
            mstCentralManager.setMistLocationAdvanceListener(advanceListener);

            mstCentralManager.setEnvironment(Utils.getEnvironment(envType));
            if (appMode.equals(AppMode.FOREGROUND)) {
                setAppMode(new AppModeParams(AppMode.FOREGROUND, BatteryUsage.HIGH_BATTERY_USAGE_HIGH_ACCURACY, true, 0.5, 15));
            } else {
                setAppMode(new AppModeParams(AppMode.BACKGROUND, BatteryUsage.LOW_BATTERY_USAGE_LOW_ACCURACY, true, 0.5, 15));
            }
            mstCentralManager.start();
        } else {
            reconnect();
        }
    }

    /**
     * @param appModeParams params to let SDK know about the scanning frequency and the state of the app (background or foreground)
     *                      call this method to switch the mode when app changes the mode between foreground and background
     */
    public void setAppMode(AppModeParams appModeParams) {
        if (this.mstCentralManager != null) {
            this.mstCentralManager.setAppMode(appModeParams);
            this.appMode = appModeParams.getAppMode();
        }
    }

    /**
     * This is the callback method which will receive the following information from the Mist SDK enrollment call
     *
     * @param orgName   name of the token used for the enrollment
     * @param orgID     organization id
     * @param sdkSecret secret needed to start the Mist SDK
     * @param error     error message if any
     * @param envType   envType which will be used to set the environment
     */
    @Override
    public void onReceivedSecret(String orgName, String orgID, String sdkSecret, String error, String envType) {
        if (!TextUtils.isEmpty(sdkSecret) && !TextUtils.isEmpty(orgID) && !TextUtils.isEmpty(sdkSecret)) {
            saveConfig(orgName, orgID, sdkSecret, envType);
            connect(indoorOnlyListener, this.advanceListener, appMode);
        } else {
            if (!Utils.isEmptyString(error)) {
                if (indoorOnlyListener != null) {
                    indoorOnlyListener.onMistErrorReceived(error, new Date());
                }
            }
        }
    }

    /**
     * This method is saving the following details so that we can use it again for starting Mist SDK without need for enrollment again
     *
     * @param orgName   name of the token used for the enrollment
     * @param orgID     organization id
     * @param sdkSecret secret needed to start the Mist SDK
     * @param envType   envType which will be used to set the environment
     */
    private void saveConfig(String orgName, String orgID, String sdkSecret, String envType) {
        orgData = new OrgData(orgName, orgID, sdkSecret, envType);
        SharedPrefUtils.saveConfig(mApp.get(), orgData, sdkToken);
    }

    /**
     * This method will stop the Mist SDK
     */
    public void disconnect() {
        if (mstCentralManager != null) {
            mstCentralManager.stop();
        }
    }

    /**
     * This method will reconnect he Mist SDK
     */
    private synchronized void reconnect() {
        if (mstCentralManager != null) {
            disconnect();
            mstCentralManager.setMSTCentralManagerIndoorOnlyListener(indoorOnlyListener);
            mstCentralManager.start();
        }
    }

    /**
     * This method will clear/destroy the Mist SDK instance
     */
    public synchronized void destroy() {
        if (mstCentralManager != null) {
            mstCentralManager.stop();
            mstCentralManager = null;
        }
    }
}
