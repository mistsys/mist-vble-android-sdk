package com.example.sampleappwakeup.service

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.sampleappwakeup.Constants
import com.example.sampleappwakeup.NotificationHandler
import com.example.sampleappwakeup.application.MainApplication
import com.example.sampleappwakeup.util.AltBeaconUtil
import com.mist.android.ErrorType
import com.mist.android.IndoorLocationCallback
import com.mist.android.MistMap
import com.mist.android.MistPoint
import com.mist.android.MistVirtualBeacon
import com.mist.android.VirtualBeaconCallback

/**
 * SDKCallbackHandler
 * We provide business logic from application perspective for different callback
 * from Mist SDK
 */
class SDKCallbackHandler(private var context: Context) : VirtualBeaconCallback, IndoorLocationCallback {
    private var lastFailedTime : Long = 0
    private var failedCount : Long = 0
    private val notificationHandler = NotificationHandler()
    private val altBeaconUtil = AltBeaconUtil()
    private val constants = Constants()
    private val mainApplication = MainApplication()

    private val TAG : String = "SampleWakeUpApp"

    /**
     * We need to implement this method as per our business logic.
     * These methods will be called for IndoorLocationCallback
     * @param relativeLocation
     */

    override fun onRelativeLocationUpdated(relativeLocation: MistPoint?) {
        Log.v(TAG,"onRelativeLocationUpdated called")
        notificationHandler.sendNotification(context,relativeLocation.toString())
        /** Returns updated location of the mobile client (as a point (X, Y) measured in meters from the map origin, i.e., relative X, Y) */
    }

    override fun onMapUpdated(map: MistMap?) {
        Log.v(TAG,"onMapUpdated called")
        /** Returns update map for the mobile client as a {@link}MSTMap object */
    }

    override fun onError(errorType: ErrorType, errorMessage: String) {
        Log.v(TAG, "onError called $errorMessage")
        if(errorType!=ErrorType.NO_BEACONS_DETECTED){
            /**
             * Stopping location service only for vble related errors
             */
            notificationHandler.sendNotification(context,errorMessage)
            return
        }
        if(failedCount % 10 < 1){
            notificationHandler.sendNotification(context, "$errorMessage $failedCount")
        }
        if(lastFailedTime == 0L){
            lastFailedTime = System.currentTimeMillis()
        }
        val timeSinceInitialFailedCall : Long = System.currentTimeMillis() - lastFailedTime
        failedCount++
        if(isDeviceLocked(context)){
            return
        }
        /**
         * We are stopping the location sdk, if we get more then NO_VBLE_FAIL_COUNT_LIMIT vble error after 5 min.
         */
        if(timeSinceInitialFailedCall >= constants.noVBLETimeMs && failedCount > constants.noVBLEFailCountLimit){
            /** Stopping the location SDK */
            val mainApplication= mainApplication.getApplication()
            MistSdkManager().getInstance(mainApplication)?.stopMistSDK()
            /** Here we reduce the scan time for alt beacon so that it starts scanning for beacon frequently. */
            altBeaconUtil.decreaseBeaconScanPeriod(mainApplication.getBeaconManager())
            /** Stopping the location foreground service*/
            val intent = Intent(context,LocationForegroundService::class.java)
            context.stopService(intent)
        } else if(timeSinceInitialFailedCall >= constants.noVBLETimeMs && failedCount < constants.noVBLEFailCountLimit){
            failedCount = 1
            lastFailedTime = System.currentTimeMillis()
        }
    }

    private fun isDeviceLocked(context: Context): Boolean {
        val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        return keyguardManager.isDeviceLocked
    }

    /**
     * We need to implement this method as per our business logic.
     * These methods will be called for VirtualBeaconCallback
     * @param mistVirtualBeacon
     */
    override fun didRangeVirtualBeacon(mistVirtualBeacon: MistVirtualBeacon) {
        Log.v(TAG,"didRangeVirtualBeacon called")
        notificationHandler.sendNotification(context,mistVirtualBeacon.message)
    }

    override fun onVirtualBeaconListUpdated(virtualBeacons: Array<out MistVirtualBeacon>?) {
        Log.v(TAG,"onVirtualBeaconListUpdated called")
        notificationHandler.sendNotification(context,"virtual beacon list updated")
    }

}