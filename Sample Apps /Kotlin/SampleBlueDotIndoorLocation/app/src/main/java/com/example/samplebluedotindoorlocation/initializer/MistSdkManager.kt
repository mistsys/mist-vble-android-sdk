package com.example.samplebluedotindoorlocation.initializer

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

class MistSdkManager {

    private var indoorLocationManager: IndoorLocationManager?=null
    private var mistConfiguration: MistConfiguration? = null
    private var mistCallbacks : MistCallbacks? = null
    private var contextWeakReference :WeakReference<Context>? = null
    private var envType: String?=null
    private var orgSecret : String?=null
    private var mistSdkManager : MistSdkManager? = null


    fun getInstance(context: Context): MistSdkManager? {
        contextWeakReference = WeakReference<Context>(context)
        if (mistSdkManager == null) {
            mistSdkManager = MistSdkManager()
        }
        return mistSdkManager
    }

    fun init(orgSecret: String?, indoorLocationCallback: IndoorLocationCallback?) {
        if (!orgSecret.isNullOrEmpty()) {
            this.orgSecret = orgSecret
            this.envType = orgSecret[0].toString()
            this.mistCallbacks = MistCallbacks(
                indoorLocationCallback = indoorLocationCallback,
            )
            this.mistConfiguration = MistConfiguration(
                context = contextWeakReference?.get()!!,
                token = orgSecret,
                enableLog = true,
                logLevel = LogLevel.INFO,
                batteryUsage = BatteryUsage.HIGH_BATTERY_USAGE_HIGH_ACCURACY
            )
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
    fun stopMistSDK() {
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