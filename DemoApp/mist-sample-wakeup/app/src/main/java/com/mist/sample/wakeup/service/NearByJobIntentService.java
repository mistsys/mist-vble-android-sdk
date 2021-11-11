package com.mist.sample.wakeup.service;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;
import android.util.Log;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;
import com.mist.sample.wakeup.activity.MainActivity;
import com.mist.sample.wakeup.utils.SharedPrefUtils;
import com.mist.sample.wakeup.utils.Utils;

public class NearByJobIntentService extends JobIntentService {

    private static Context mContext;
    private static final int JOB_ID = 100;
    private static final String TAG = NearByJobIntentService.class.getSimpleName();

    public static void enqueueWork(Context context, Intent intent) {
        mContext = context;
        enqueueWork(context, NearByJobIntentService.class, JOB_ID, intent);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        Nearby.Messages.handleIntent(intent, new MessageListener() {

            @Override
            public void onFound(Message message) {
                try {
                    if (mContext == null) {
                        mContext = getApplicationContext();
                    }
                    Log.v(TAG, "@!@!  Found Tag type -" +
                            message.getType() + " ID - " + toHexadecimal(message.getContent()));
                    if (!SharedPrefUtils.isAppAlive(mContext)) {
                        Utils.scheduleJob(mContext);
                    }
                    if (SharedPrefUtils.shouldShowWelcome(mContext)) {
                        Intent intent = new Intent(mContext, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        PendingIntent contentIntent = PendingIntent.getActivity(mContext,
                                Utils.WELCOME_NOTIFICATION_ID, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                        Utils.sendNotification(mContext, contentIntent);
                        SharedPrefUtils.setShouldShowWelcome(mContext, false);
                    }
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }
            }

            @Override
            public void onLost(Message message) {
                Log.e(TAG, "@!@!  Lost Tag type -" + message.getType() + " ID - " +
                        toHexadecimal(message.getContent()));
            }
        });
    }

    private static String toHexadecimal(byte[] digest) {
        String hash = "";
        for (byte aux : digest) {
            int b = aux & 0xff;
            if (Integer.toHexString(b).length() == 1) hash += "0";
            hash += Integer.toHexString(b);
        }
        return hash;
    }
}
