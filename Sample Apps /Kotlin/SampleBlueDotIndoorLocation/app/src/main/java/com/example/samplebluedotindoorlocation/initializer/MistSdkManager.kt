package com.example.samplebluedotindoorlocation.initializer

import android.content.Context
import com.mist.android.BatteryUsage
import com.mist.android.IndoorLocationCallback
import com.mist.android.IndoorLocationManager
import com.mist.android.external.config.LogLevel
import com.mist.android.external.config.MistConfiguration
import java.lang.ref.WeakReference

class MistSdkManager {

    private var indoorLocationManager: IndoorLocationManager?=null
    private var mistConfiguration: MistConfiguration? = null
    private var contextWeakReference :WeakReference<Context>? = null
    private var envType: String?=null
    private var orgSecret : String?=null
    private var mistSdkManager : MistSdkManager? = null
    private var indoorLocationCallback: IndoorLocationCallback? = null

    fun getInstance(context: Context): MistSdkManager? {
        contextWeakReference = WeakReference<Context>(context)
        mistSdkManager?: run {
            mistSdkManager = MistSdkManager()
        }
        return mistSdkManager
    }



    fun init(orgSecret: String?, indoorLocationCallback: IndoorLocationCallback?, orgId: String?) {
        this.indoorLocationCallback = indoorLocationCallback
        if (!orgSecret.isNullOrEmpty()) {
            this.orgSecret = orgSecret
            this.envType = orgSecret[0].toString()
            this.mistConfiguration = orgId?.let {
                MistConfiguration(
                    context = contextWeakReference?.get()!!,
                    token = orgSecret,
                    enableLog = true,
                    logLevel = LogLevel.INFO,
                    batteryUsage = BatteryUsage.HIGH_BATTERY_USAGE_HIGH_ACCURACY,
                    orgId = it
                )
            }
        }
    }

    @Synchronized
    fun startMistSDK() {

        indoorLocationManager?.let {
            restartMistSDK()
        } ?: run{
            indoorLocationManager = IndoorLocationManager
            mistConfiguration?.let {mistconfiguration ->
                indoorLocationCallback?.let { indoorlocationcallback ->
                    indoorLocationManager?.start(
                        mistconfiguration,
                        indoorLocationCallback = indoorlocationcallback
                    )
                }
            }
        }
    }

    @Synchronized
    fun stopMistSDK() {
        indoorLocationManager?.let{
            indoorLocationManager?.stop()
        }
    }

    fun destroy() {
        indoorLocationManager?.let{
            indoorLocationManager?.stop()
        }
    }

    @Synchronized
    fun restartMistSDK() {
        indoorLocationManager?.let {
            stopMistSDK()
            mistConfiguration?.let {mistconfiguration ->
                indoorLocationCallback?.let { indoorlocationcallback ->
                    indoorLocationManager?.start(
                        mistconfiguration,
                        indoorLocationCallback = indoorlocationcallback
                    )
                }
            }
        }
    }

}