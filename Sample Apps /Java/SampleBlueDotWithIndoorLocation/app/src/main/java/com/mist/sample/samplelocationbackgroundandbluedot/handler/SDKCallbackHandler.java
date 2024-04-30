package com.mist.sample.samplelocationbackgroundandbluedot.handler;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mist.android.ErrorType;
import com.mist.android.IndoorLocationCallback;
import com.mist.android.MistEvent;
import com.mist.android.MistMap;
import com.mist.android.MistPoint;
import com.mist.android.MistVirtualBeacon;
import com.mist.android.VirtualBeaconCallback;
import com.mist.sample.samplelocationbackgroundandbluedot.NotificationHandler;

/**
 * SDKCallbackHandler We provide business logic from application perspective for different callback
 * from Mist SDK
 */
public class SDKCallbackHandler implements IndoorLocationCallback {
    Context context;

    public SDKCallbackHandler(Context context) {
        this.context = context;
    }

    private static final String TAG = "SampleLocationApp";

    /**
     * We need to implement this method as per our business logic. These methods will be called for IndoorLocationCallback
     * @param relativeLocation
     */

    /**
     * Returns updated location of the mobile client (as a point (X, Y) measured in meters from the map origin, i.e., relative X, Y)
     */
    @Override
    public void onRelativeLocationUpdated(@Nullable MistPoint mistPoint) {
        Log.v(TAG, "onRelativeLocationUpdated called");
        NotificationHandler.sendNotification(context, mistPoint.toString());
    }

    /**
     * Returns update map for the mobile client as a {@link}MSTMap object
     */
    @Override
    public void onMapUpdated(@Nullable MistMap mistMap) {
        Log.v(TAG, "onMapUpdated called");
    }

    @Override
    public void onReceiveEvent(@NonNull MistEvent event) {
        if (event instanceof MistEvent.OnError){
            Log.v(TAG, "onError called " + ((MistEvent.OnError) event).getError().name());
            NotificationHandler.sendNotification(context, ((MistEvent.OnError) event).getError().name());
            /* Notifies the host application about any errors encountered */
        }

        /**
         * We need to implement this method as per our business logic.
         * These methods will be called for VirtualBeaconCallback
         * @param mistVirtualBeacon
         **/

        else if (event instanceof MistEvent.OnRangeVirtualBeacon) {
            Log.v(TAG, "didRangeVirtualBeacon called");
            NotificationHandler.sendNotification(context, ((MistEvent.OnRangeVirtualBeacon) event).getVirtualBeacon().getMessage());
        }
        else if (event instanceof MistEvent.OnUpdateVirtualBeaconList) {
            Log.v(TAG, "onVirtualBeaconListUpdated called");
            NotificationHandler.sendNotification(context, "virtual beacon list updated");
        }
    }

}
