package com.example.samplebluedotexperience.initializer

import android.content.Context
import android.util.Log
import com.mist.android.BatteryUsage
import com.mist.android.IndoorLocationCallback
import com.mist.android.IndoorLocationManager
import com.mist.android.external.config.LogLevel
import com.mist.android.external.config.MistConfiguration
import java.lang.ref.WeakReference

class MistSdkManager{

    /**
     * Required by Mist SDK for initialization
     * IndoorLocationManager
     * IndoorLocationCallback
     */

    private var indoorLocationManager: IndoorLocationManager?=null
    private lateinit var contextWeakReference: WeakReference<Context>
    private var mistConfiguration: MistConfiguration? = null
    private var sdkInitializer : MistSdkManager? = null
    private var orgSecret : String? = null
    private var indoorLocationCallback: IndoorLocationCallback? = null

    fun getInstance(context: Context): MistSdkManager {
        contextWeakReference = WeakReference<Context>(context)
        sdkInitializer?: run {
            sdkInitializer = MistSdkManager()
        }
        return sdkInitializer as MistSdkManager
    }

    fun init(orgSecret: String, indoorLocationCallback: IndoorLocationCallback, orgId: String) {
        this.indoorLocationCallback = indoorLocationCallback
        if (orgSecret.isNotEmpty()) {
            Log.d("", "Sample Blue Dot Init $orgSecret")
            this.orgSecret = orgSecret
            this.mistConfiguration =
                MistConfiguration(
                    context = contextWeakReference.get()!!,
                    token = orgSecret,
                    enableLog = true,
                    logLevel = LogLevel.INFO,
                    batteryUsage = BatteryUsage.HIGH_BATTERY_USAGE_HIGH_ACCURACY,
                    orgId = orgId
                )
        }

    }

        fun startMistSdk(){
            if(indoorLocationManager==null){
                indoorLocationManager = IndoorLocationManager
                mistConfiguration?.let {
                    indoorLocationCallback?.let { it1 ->
                        indoorLocationManager?.start(
                            it,
                            indoorLocationCallback = it1
                        )
                    }
                }
        }
        else{
            restartMistSdk()
        }
    }

    @Synchronized
    fun stopMistSdk(){
        indoorLocationManager?.let{
            indoorLocationManager?.stop()
        }
    }

    fun destroyMistSdk(){
        indoorLocationManager?.let{
            indoorLocationManager?.stop()
        }
    }

    @Synchronized
    private fun restartMistSdk() {
        indoorLocationManager?.let{
            stopMistSdk()
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