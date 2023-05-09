package com.mist.samplelocationbackground.handler;

import android.content.Context;
import android.util.Log;

import com.mist.android.ErrorType;
import com.mist.android.IndoorLocationCallback;
import com.mist.android.MistMap;
import com.mist.android.MistPoint;
import com.mist.android.MistVirtualBeacon;
import com.mist.android.VirtualBeaconCallback;
import com.mist.samplelocationbackground.NotificationHandler;

/**
 * SDKCallbackHandler We provide business logic from application perspective for different callback
 * from Mist SDK
 */
public class SDKCallbackHandler implements VirtualBeaconCallback, IndoorLocationCallback {
    Context context;

    public SDKCallbackHandler(Context context) {
        this.context = context;
    }

    private static final String TAG = "SampleLocationApp";

    /**
     * We need to implement this method as per our business logic.
     * These methods will be called for IndoorLocationCallback
     * @param relativeLocation
     */
    @Override
    public void onRelativeLocationUpdated(MistPoint relativeLocation) {
        Log.v(TAG, "onRelativeLocationUpdated called");
        NotificationHandler.sendNotification(context, relativeLocation.toString());
        /** Returns updated location of the mobile client (as a point (X, Y) measured in meters from the map origin, i.e., relative X, Y) */
    }

    @Override
    public void onMapUpdated(MistMap map) {
        Log.v(TAG, "onMapUpdated called");
        /** Returns update map for the mobile client as a {@link}MSTMap object */
    }

    @Override
    public void onError(ErrorType errorType, String errorMessage) {
        Log.v(TAG, "onError called" + errorMessage);
        NotificationHandler.sendNotification(context, errorMessage);
        /** Notifies the host application about any errors encountered */
    }

    /**
     * We need to implement this method as per our business logic.
     * These methods will be called for VirtualBeaconCallback
     * @param mistVirtualBeacon
     */
    @Override
    public void didRangeVirtualBeacon(MistVirtualBeacon mistVirtualBeacon) {
        Log.v(TAG, "didRangeVirtualBeacon called");
        NotificationHandler.sendNotification(context, mistVirtualBeacon.message);
    }

    @Override
    public void onVirtualBeaconListUpdated(MistVirtualBeacon[] virtualBeacons) {
        Log.v(TAG, "onVirtualBeaconListUpdated called");
        NotificationHandler.sendNotification(context, "virtual beacon list updated");
    }
}
