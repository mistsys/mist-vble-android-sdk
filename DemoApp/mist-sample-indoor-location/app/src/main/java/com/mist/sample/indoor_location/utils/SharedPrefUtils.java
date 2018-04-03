package com.mist.sample.indoor_location.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.mist.sample.indoor_location.R;
import com.mist.sample.indoor_location.model.OrgData;
import com.google.gson.Gson;

import java.util.Set;

/**
 * Created by anubhava on 26/03/18.
 */

public class SharedPrefUtils {
    private static final int MODE = Context.MODE_PRIVATE;
    private static final String EMPTY_STRING = "";
    private static final long DEFAULT_LONG = 0l;
    private static final Gson gson = new Gson();

    public static SharedPreferences getPreferences(Context context) {
        String appName = context.getResources().getString(R.string.app_name);
        return context.getSharedPreferences(appName, MODE);
    }

    public static void clearSharedPreferenceFile(Context context) {
        getEditor(context).clear().apply();
    }

    public static SharedPreferences.Editor getEditor(Context context) {
        return getPreferences(context).edit();
    }

    public static void writeString(Context context, String key, String value) {
        getEditor(context).putString(key, value).apply();
    }

    public static String readString(Context context, String key, String defValue) {
        return getPreferences(context).getString(key, defValue);
    }

    public static void saveConfig(Context context, OrgData orgData, String sdkSecret) {
        String orgDataString = gson.toJson(orgData);
        writeString(context, sdkSecret, orgDataString);
    }

    public static OrgData readConfig(Context context, String sdkSecret) {
        String orgDataString = readString(context, sdkSecret, "");
        if (!orgDataString.isEmpty()) {
            return gson.fromJson(orgDataString, OrgData.class);
        } else
            return null;

    }


}
