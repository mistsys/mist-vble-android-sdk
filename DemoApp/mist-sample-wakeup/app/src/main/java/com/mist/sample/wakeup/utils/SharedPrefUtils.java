package com.mist.sample.wakeup.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.mist.sample.wakeup.R;
import com.mist.sample.wakeup.model.OrgData;

/**
 * Created by anubhava on 26/03/18.
 */

public class SharedPrefUtils {
    private static final int MODE = Context.MODE_PRIVATE;
    private static final String EMPTY_STRING = "";
    private static final long DEFAULT_LONG = 0l;
    private static final Gson gson = new Gson();

    private static SharedPreferences getPreferences(Context context) {
        String appName = context.getResources().getString(R.string.app_name);
        return context.getSharedPreferences(appName, MODE);
    }

    public static void clearSharedPreferenceFile(Context context) {
        getEditor(context).clear().apply();
    }

    public static void writeBoolean(Context context, String key, boolean value) {
        getEditor(context).putBoolean(key, value).apply();
    }

    public static boolean readBoolean(Context context, String key, boolean defValue) {
        return getPreferences(context).getBoolean(key, defValue);
    }

    private static SharedPreferences.Editor getEditor(Context context) {
        return getPreferences(context).edit();
    }

    private static void writeString(Context context, String key, String value) {
        getEditor(context).putString(key, value).apply();
    }

    private static String readString(Context context, String key, String defValue) {
        return getPreferences(context).getString(key, defValue);
    }

    public static void saveConfig(Context context, OrgData orgData, String sdkSecret) {
        String orgDataString = gson.toJson(orgData);
        writeString(context, sdkSecret, orgDataString);
    }

    public static OrgData readConfig(Context context, String sdkSecret) {
        String orgDataString = readString(context, sdkSecret, EMPTY_STRING);
        if (!orgDataString.isEmpty()) {
            return gson.fromJson(orgDataString, OrgData.class);
        } else
            return null;
    }


    //saving sdktoken to sharedpref
    public static void saveSdkToken(Context context, String key, String value) {
        getEditor(context).putString(key, value).apply();
    }

    public static String readSdkToken(Context context, String key) {
        return getPreferences(context).getString(key, EMPTY_STRING);
    }

    public static boolean isAppAlive(Context context) {
        return readBoolean(context, context.getString(R.string.pref_app_state), false);
    }

    public static boolean shouldShowWelcome(Context context) {
        return readBoolean(context, context.getString(R.string.pref_should_welcome), false);
    }

    public static void setShouldShowWelcome(Context context, boolean state) {
        writeBoolean(context, context.getString(R.string.pref_should_welcome), state);
    }

    public static void setIsAppAlive(Context context, boolean state) {
        writeBoolean(context, context.getString(R.string.pref_app_state), state);
    }

}
