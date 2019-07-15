package com.mist.sample.background.utils;

import android.content.Context;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Log;

import com.mist.sample.background.service.MSTSDKBackgroundService;

/**
 * Created by anubhava on 02/04/18.
 */

public class Utils {

    private static final String TAG = Utils.class.getSimpleName();

    public static final String TOKEN_PREF_KEY_NAME = "sdkToken";

    /**
     * Check if connectivity is available
     *
     * @param context
     * @return
     */
    public static boolean isNetworkAvailable(Context context) {
        boolean isNet = false;
        if (context != null) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager != null) {
                NetworkInfo result = connectivityManager.getActiveNetworkInfo();
                if (result != null && result.isConnectedOrConnecting()) {
                    isNet = true;
                }
            }
        }
        return isNet;
    }

    /**
     * Check if location services are enabled
     *
     * @param context
     * @return
     */
    public static boolean isLocationServiceEnabled(Context context) {
        boolean gpsEnabled = false, networkEnabled = false;

        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            try {
                gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            } catch (RuntimeException ex) {
                Log.e(TAG, "Cannot access GPS provider: " + ex.getLocalizedMessage());
            }

            try {
                networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            } catch (RuntimeException ex) {
                Log.e(TAG, "Cannot access GPS provider: " + ex.getLocalizedMessage());
            }
        }
        return gpsEnabled || networkEnabled;
    }

    /**
     * Get the environment name given env
     *
     * @param envType
     * @return
     */
    public static String getEnvironment(String envType) throws Exception {
        String env;
        envType = envType.toUpperCase();
        if (envType.charAt(0) == 'P'){
            env = "Production";
        } else if (envType.charAt(0) == 'E') {
            env = "EU";
        } else if (envType.charAt(0) == 'K') {
            env = "Kalam";
        } else {
            throw new Exception("Invalid environemnt is specified");
        }
        return env;
    }

    public static boolean isEmptyString(String value) {
        return TextUtils.isEmpty(value) || value.equalsIgnoreCase("null");
    }

    /**
     * Check if the token is empty and valid
     *
     * @param token
     * @return
     */
    public static boolean isValidToken(String token) {
        if (TextUtils.isEmpty(token))
            return false;
        token = token.toUpperCase();
        return (token.charAt(0) == 'P' ||
                token.charAt(0) == 'S' ||
                token.charAt(0) == 'E' ||
                token.charAt(0) == 'K');
    }
}
