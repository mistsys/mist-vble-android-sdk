package com.example.sampleappwakeup

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.sampleappwakeup.application.MainApplication
import com.example.sampleappwakeup.util.AltBeaconUtil

class BootReceiver : BroadcastReceiver() {

    private val altBeaconUtil = AltBeaconUtil()
    override fun onReceive(context: Context?, intent: Intent?) {
        when(intent?.action){
            Intent.ACTION_BOOT_COMPLETED ->{
                Toast.makeText(context,"SampleWakeUpBroadcastReceiver: ACTION_BOOT_COMPLETED",Toast.LENGTH_SHORT).show()
                val mainApplication: MainApplication = context!!.applicationContext as MainApplication
                altBeaconUtil.startBeaconMonitor(mainApplication.getBeaconManager(),mainApplication)
            }
        }
    }
}