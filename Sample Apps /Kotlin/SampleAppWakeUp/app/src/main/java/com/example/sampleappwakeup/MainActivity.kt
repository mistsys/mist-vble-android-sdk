package com.example.sampleappwakeup

import android.os.Bundle
import com.example.sampleappwakeup.application.MainApplication
import com.example.sampleappwakeup.util.AltBeaconUtil

import org.altbeacon.beacon.BeaconManager

class MainActivity : PermissionHandlerActivity(){
    private lateinit var mainApplication : MainApplication

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mainApplication = (application as MainApplication)
        checkPermissions()
        BeaconManager.setDebug(true)
    }

    override fun startBeaconMonitor() {
        AltBeaconUtil().startBeaconMonitor(mainApplication.getBeaconManager(), mainApplication)
    }
}