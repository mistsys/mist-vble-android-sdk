package com.example.sampleindoorlocationreporting.handler


import android.content.Context
import android.util.Log
import com.example.sampleindoorlocationreporting.NotificationHandler
import com.mist.android.IndoorLocationCallback
import com.mist.android.MistEvent
import com.mist.android.MistMap
import com.mist.android.MistPoint

class SDKCallbackHandler(private val context: Context) : IndoorLocationCallback{

    private val TAG : String = "SampleLocationApp"
    private val notificationHandler = NotificationHandler()

    /**
     * We need to implement this method as per our business logic.
     * These methods will be called for IndoorLocationCallback
     * @param relativeLocation
     */
    private fun onRelativeLocationUpdated(relativeLocation: MistPoint?) {
        Log.v(TAG,"onRelativeLocationUpdated called")
        notificationHandler.sendNotification(context,relativeLocation.toString())
        /** Returns updated location of the mobile client (as a point (X, Y) measured in meters from the map origin, i.e., relative X, Y) */
    }

    private fun onMapUpdated(map: MistMap?) {
        Log.v(TAG,"onMapUpdated called")
        /** Returns update map for the mobile client as a {@link}MSTMap object */
    }

    override fun onReceiveEvent(event: MistEvent) {
        when(event){
            is MistEvent.OnError ->{
                Log.v(TAG, "onError called ${event.error.name}")
                notificationHandler.sendNotification(context,event.error.name)
            }
            is MistEvent.OnMapUpdate -> {
                onMapUpdated(event.map)
            }
            is MistEvent.OnRelativeLocationUpdate -> {
                onRelativeLocationUpdated(event.point)
            }
            is MistEvent.OnRangeVirtualBeacon ->{
                Log.v(TAG, "didRangeVirtualBeacon called")
                notificationHandler.sendNotification(context, event.virtualBeacon.message.toString())
            }
            is MistEvent.OnUpdateVirtualBeaconList ->{
                Log.v(TAG, "onVirtualBeaconListUpdated called")
                notificationHandler.sendNotification(context, "virtual beacon list updated")
            }
            else -> {}
        }
    }

}