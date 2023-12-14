package com.example.sampleindoorlocationreporting.handler


import android.content.Context
import android.util.Log
import com.example.sampleindoorlocationreporting.NotificationHandler
import com.mist.android.ErrorType
import com.mist.android.IndoorLocationCallback
import com.mist.android.MistMap
import com.mist.android.MistPoint
import com.mist.android.MistVirtualBeacon
import com.mist.android.VirtualBeaconCallback

class SDKCallbackHandler(private val context: Context) : VirtualBeaconCallback, IndoorLocationCallback{

    private val TAG : String = "SampleLocationApp"
    private val notificationHandler = NotificationHandler()

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
        notificationHandler.sendNotification(context,errorMessage)
        /** Notifies the host application about any errors encountered */
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