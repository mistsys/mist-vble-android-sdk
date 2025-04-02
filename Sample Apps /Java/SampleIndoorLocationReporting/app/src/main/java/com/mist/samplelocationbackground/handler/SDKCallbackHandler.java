package com.mist.samplelocationbackground.handler;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.mist.android.IndoorLocationCallback;
import com.mist.android.MistEvent;
import com.mist.samplelocationbackground.NotificationHandler;

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

    @Override
    public void onReceiveEvent(@NonNull MistEvent event) {
        if (event instanceof MistEvent.OnError) {
            Log.v(TAG, "onError called " + ((MistEvent.OnError) event).getError().name());
            NotificationHandler.sendNotification(context, ((MistEvent.OnError) event).getError().name());
            /** Notifies the host application about any errors encountered */
        } else if (event instanceof MistEvent.OnRelativeLocationUpdate) {
            Log.v(TAG, "OnRelativeLocationUpdate called");
            NotificationHandler.sendNotification(context, "relative location updated");
        } else if (event instanceof MistEvent.OnMapUpdate) {
            Log.v(TAG, "OnMapUpdate called");
            NotificationHandler.sendNotification(context, "map updated");
        }

        /**
         * We need to implement this method as per our business logic.
         * These methods will be called for VirtualBeaconCallback
         * @param mistVirtualBeacon
         **/

        else if (event instanceof MistEvent.OnRangeVirtualBeacon) {
            Log.v(TAG, "didRangeVirtualBeacon called");
            NotificationHandler.sendNotification(context, ((MistEvent.OnRangeVirtualBeacon) event).getVirtualBeacon().getMessage());
        } else if (event instanceof MistEvent.OnUpdateVirtualBeaconList) {
            Log.v(TAG, "onVirtualBeaconListUpdated called");
            NotificationHandler.sendNotification(context, "virtual beacon list updated");
        }
    }

}
