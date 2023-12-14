package com.example.samplebluedotindoorlocation.handler

import android.content.Context
import android.util.Log
import com.example.samplebluedotindoorlocation.NotificationHandler
import com.example.samplebluedotindoorlocation.NotificationHandler.*
import com.mist.android.ErrorType
import com.mist.android.IndoorLocationCallback
import com.mist.android.MistMap
import com.mist.android.MistPoint
import com.mist.android.MistVirtualBeacon
import com.mist.android.VirtualBeaconCallback

class SDKCallbackHandler(private var context: Context) : VirtualBeaconCallback, IndoorLocationCallback {
    /**
     * We need to implement this method as per our business logic. These methods will be called for IndoorLocationCallback
     * @param relativeLocation
     */
    /**
     * Returns updated location of the mobile client (as a point (X, Y) measured in meters from the map origin, i.e., relative X, Y)
     */
    private val TAG : String ="SampleLocationApp"
    private val notificationHandler=NotificationHandler()
    override fun onRelativeLocationUpdated(relativeLocation: MistPoint) {
        Log.v(TAG, "onRelativeLocationUpdated called")
        notificationHandler.sendNotification(context, relativeLocation.toString())
    }

    /**
     * Returns update map for the mobile client as a []MSTMap object
     */
    override fun onMapUpdated(map: MistMap) {
        Log.v(TAG, "onMapUpdated called")
    }

    override fun onError(errorType: ErrorType, errorMessage: String) {
        Log.v(TAG, "onError called $errorMessage")
        notificationHandler.sendNotification(context,errorMessage)
        /** Notifies the host application about any errors encountered  */
    }

    /**
     * We need to implement this method as per our business logic.
     * These methods will be called for VirtualBeaconCallback
     * @param mistVirtualBeacon
     */
    override fun didRangeVirtualBeacon(mistVirtualBeacon: MistVirtualBeacon) {
        Log.v(TAG, "didRangeVirtualBeacon called")
        notificationHandler.sendNotification(context, mistVirtualBeacon.message)
    }

    override fun onVirtualBeaconListUpdated(virtualBeacons: Array<MistVirtualBeacon>) {
        Log.v(TAG, "onVirtualBeaconListUpdated called")
        notificationHandler.sendNotification(context, "virtual beacon list updated")
    }

}