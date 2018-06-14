package com.mist.sample.wayfinding.util;

import android.app.Application;
import android.text.TextUtils;
import android.util.Log;

import com.mist.android.AppMode;
import com.mist.android.BatteryUsage;
import com.mist.android.MSTCentralManager;
import com.mist.android.MSTCentralManagerIndoorOnlyListener;
import com.mist.android.MSTOrgCredentialsCallback;
import com.mist.android.MSTOrgCredentialsManager;
import com.mist.android.model.AppModeParams;
import com.mist.sample.wayfinding.app.MainApplication;
import com.mist.sample.wayfinding.model.OrgData;

import java.lang.ref.WeakReference;
import java.util.Date;

/**
 * Created by anubhava on 02/04/18.
 */

public class MistManager implements MSTOrgCredentialsCallback {

    private static final String TAG = MistManager.class.getSimpleName();
    private static WeakReference<Application> mApp;
    private static MistManager mistManager;
    private String sdkToken;
    private String envType;
    private MSTCentralManagerIndoorOnlyListener indoorOnlyListener;
    private AppMode appMode = AppMode.FOREGROUND;
    private OrgData orgData;
    private MSTOrgCredentialsManager mstOrgCredentialsManager;
    private volatile MSTCentralManager mstCentralManager;

    public MistManager() {
    }

    public static MistManager newInstance(MainApplication mainApplication) {
        mApp = new WeakReference<Application>(mainApplication);
        if (mistManager == null) {
            mistManager = new MistManager();
        }
        return mistManager;
    }

    public void init(String sdkToken, MSTCentralManagerIndoorOnlyListener indoorOnlyListener,
                     AppMode appMode) {
        if (sdkToken != null && !sdkToken.isEmpty()) {
            this.sdkToken = sdkToken;
            this.envType = String.valueOf(sdkToken.charAt(0));
            this.indoorOnlyListener = indoorOnlyListener;
            if(appMode !=null) {
                this.appMode = appMode;
            }
            orgData = SharedPrefUtils.readConfig(mApp.get(), sdkToken);
            if (orgData == null || orgData.getSdkSecret() == null || orgData.getSdkSecret().isEmpty()) {
                if (mstOrgCredentialsManager == null) {
                    mstOrgCredentialsManager = new MSTOrgCredentialsManager(mApp.get(), this);
                }
                mstOrgCredentialsManager.enrollDeviceWithToken(sdkToken);

            } else {
                connect(indoorOnlyListener, this.appMode);
            }
        } else {
            Log.d(TAG, "Empty SDK Token");
        }
    }

    private synchronized void connect(MSTCentralManagerIndoorOnlyListener indoorOnlyListener, AppMode appMode) {
        if (mstCentralManager == null) {
            mstCentralManager = new MSTCentralManager(mApp.get(),
                    orgData.getOrgId(), orgData.getSdkSecret(), indoorOnlyListener);
            mstCentralManager.setEnvironment(Utils.getEnvironment(envType));
            if (appMode.equals(AppMode.FOREGROUND)) {
                setAppMode(new AppModeParams(AppMode.FOREGROUND, BatteryUsage.HIGH_BATTERY_USAGE_HIGH_ACCURACY,
                        true, 0.5, 1));
            } else {
                setAppMode(new AppModeParams(AppMode.BACKGROUND, BatteryUsage.LOW_BATTERY_USAGE_LOW_ACCURACY,
                        true, 0.5, 1));
            }
            mstCentralManager.start();
        } else {
            reconnect();
        }
    }

    public void setAppMode(AppModeParams appModeParams) {
        if (this.mstCentralManager != null) {
            this.mstCentralManager.setAppMode(appModeParams);
            this.appMode = appModeParams.getAppMode();
        }
    }

    @Override
    public void onReceivedSecret(String orgName, String orgID, String sdkSecret, String error, String envType) {
        if (!TextUtils.isEmpty(sdkSecret) && !TextUtils.isEmpty(orgID) && !TextUtils.isEmpty(sdkSecret)) {
            saveConfig(orgName, orgID, sdkSecret, envType);
            connect(indoorOnlyListener, appMode);
        } else {
            if (!Utils.isEmptyString(error)) {
                if (indoorOnlyListener != null) {
                    indoorOnlyListener.onMistErrorReceived(error, new Date());
                }
            }
        }
    }

    private void saveConfig(String orgName, String orgID, String sdkSecret, String envType) {
        orgData = new OrgData(orgName, orgID, sdkSecret, envType);
        SharedPrefUtils.saveConfig(mApp.get(), orgData, sdkToken);
    }

    public void disconnect() {
        if (mstCentralManager != null) {
            mstCentralManager.stop();
        }
    }

    private synchronized void reconnect() {
        if (mstCentralManager != null) {
            disconnect();
            mstCentralManager.setMSTCentralManagerIndoorOnlyListener(indoorOnlyListener);
            mstCentralManager.start();
        }
    }
    public synchronized void destory(){
        if (mstCentralManager != null) {
            mstCentralManager.stop();
            mstCentralManager = null;
        }
    }
}
