package com.example.samplebluedotexperience.initializer

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.mist.android.BatteryUsage
import com.mist.android.IndoorLocationCallback
import com.mist.android.IndoorLocationManager
import com.mist.android.VirtualBeaconCallback
import com.mist.android.external.config.LogLevel
import com.mist.android.external.config.MistCallbacks
import com.mist.android.external.config.MistConfiguration
import java.lang.ref.WeakReference

class MistSdkManager{

    /**
     * Required by Mist SDK for initialization
     * IndoorLocationManager
     * IndoorLocationCallback
     * VirtualBeaconCallback
     */

    private var indoorLocationManager: IndoorLocationManager?=null
    private lateinit var contextWeakReference: WeakReference<Context>
    private var mistConfiguration: MistConfiguration? = null
    private var mistCallbacks : MistCallbacks? = null
    private var sdkInitializer : MistSdkManager? = null
    private var orgSecret : String? = null

    fun getInstance(context: Context): MistSdkManager {
        contextWeakReference = WeakReference<Context>(context)
        if (sdkInitializer == null) {
            sdkInitializer = MistSdkManager()
        }
        return sdkInitializer as MistSdkManager
    }

    fun init(orgSecret:String, indoorLocationCallback: IndoorLocationCallback, virtualBeaconCallback: VirtualBeaconCallback?) {
        if (orgSecret.isNotEmpty()) {
            Log.d("", "Sample Blue Dot Init $orgSecret")
            this.orgSecret = orgSecret
            this.mistCallbacks = MistCallbacks(
                indoorLocationCallback = indoorLocationCallback
            )
            this.mistConfiguration = MistConfiguration(
                context = contextWeakReference.get()!!,
                token = orgSecret,
                enableLog = true,
                logLevel = LogLevel.INFO,
                batteryUsage = BatteryUsage.HIGH_BATTERY_USAGE_HIGH_ACCURACY
            )
        }
    }

        fun startMistSdk(){
            if(indoorLocationManager==null){
                indoorLocationManager = IndoorLocationManager
                indoorLocationManager?.mistCallbacks = mistCallbacks!!
                mistConfiguration?.let { indoorLocationManager?.start(it)  }
//                indoorLocationManager?.start(mistConfiguration!!)
        }
        else{
            restartMistSdk()
        }
    }

    @Synchronized
    fun stopMistSdk(){
        if (indoorLocationManager!=null){
            indoorLocationManager?.stop()
        }
    }

    fun destroyMistSdk(){
        if (indoorLocationManager!=null){
            indoorLocationManager?.stop()
        }
    }

    @Synchronized
    private fun restartMistSdk() {
        if(indoorLocationManager!=null){
            stopMistSdk()
            indoorLocationManager?.start(mistConfiguration!!)
        }
    }

}