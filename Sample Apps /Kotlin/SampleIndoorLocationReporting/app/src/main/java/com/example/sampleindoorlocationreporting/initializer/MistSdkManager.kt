package com.example.sampleindoorlocationreporting.initializer

import android.content.Context
import android.widget.Toast
import com.mist.android.BatteryUsage
import com.mist.android.IndoorLocationCallback
import com.mist.android.IndoorLocationManager
import com.mist.android.VirtualBeaconCallback
import com.mist.android.external.config.LogLevel
import com.mist.android.external.config.MistCallbacks
import com.mist.android.external.config.MistConfiguration
import java.lang.ref.WeakReference

/**
 * MistSdkInitializer This class provides utility function to start the Mist SDK.
 */

class MistSdkManager {
    /**
     * Required by Mist SDK for initialization IndoorLocationManager IndoorLocationCallback
     * VirtualBeaconCallback
     */
    private var indoorLocationManager : IndoorLocationManager? = null
    private var mistConfiguration: MistConfiguration? = null
    private var mistCallbacks : MistCallbacks? = null
    private lateinit var contextWeakReference : WeakReference<Context>
    private var envType: String?=null
    private var orgSecret : String?=null
    private  lateinit var mistSdkManager : MistSdkManager


    fun getInstance(context: Context): MistSdkManager {
        contextWeakReference = WeakReference<Context>(context)
        mistSdkManager = MistSdkManager()
        return mistSdkManager
    }

    fun init(orgSecret: String?, indoorLocationCallback: IndoorLocationCallback?) {
        if (!orgSecret.isNullOrEmpty()) {
            this.orgSecret = orgSecret
            envType = orgSecret[0].toString()
            this.mistCallbacks = MistCallbacks(
                indoorLocationCallback = indoorLocationCallback,
            )
            this.mistConfiguration = MistConfiguration(
                context = contextWeakReference.get()!!,
                token = orgSecret,
                enableLog = true,
                logLevel = LogLevel.INFO,
                batteryUsage = BatteryUsage.HIGH_BATTERY_USAGE_HIGH_ACCURACY
            )
        }
        else{
            Toast.makeText(contextWeakReference.get(), "Org Secret not present", Toast.LENGTH_SHORT).show();
        }
    }

    @Synchronized
    fun startMistSDK() {
        if (indoorLocationManager == null) {
            indoorLocationManager = IndoorLocationManager
            indoorLocationManager?.mistCallbacks = mistCallbacks!!
            mistConfiguration?.let { indoorLocationManager?.start(it)  }
//            indoorLocationManager?.start(mistConfiguration!!)
        }
        else{
            restartMistSDK()
        }
    }

    @Synchronized
    private fun stopMistSDK() {
        if (indoorLocationManager!=null){
            indoorLocationManager?.stop()
        }
    }

    fun destroy() {
        if (indoorLocationManager!=null){
            indoorLocationManager?.stop()
        }
    }

    @Synchronized
    fun restartMistSDK() {
        if (indoorLocationManager != null) {
            stopMistSDK()
            indoorLocationManager?.start(mistConfiguration!!)
        }
    }
}