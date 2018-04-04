package com.mist.sample.notification.fragment;


import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mist.sample.notification.R;
import com.mist.sample.notification.app.MainApplication;
import com.mist.sample.notification.utils.MistManager;
import com.mist.sample.notification.utils.Utils;
import com.mist.android.AppMode;
import com.mist.android.MSTAsset;
import com.mist.android.MSTBeacon;
import com.mist.android.MSTCentralManagerIndoorOnlyListener;
import com.mist.android.MSTCentralManagerStatusCode;
import com.mist.android.MSTClient;
import com.mist.android.MSTMap;
import com.mist.android.MSTPoint;
import com.mist.android.MSTVirtualBeacon;
import com.mist.android.MSTZone;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by anubhava on 26/03/18.
 */

public class MapFragment extends Fragment implements MSTCentralManagerIndoorOnlyListener {

    public static final String TAG = "MAPFRAGMENTTAG";
    private static final int PERMISSION_REQUEST_FINE_LOCATION = 1;
    private MainApplication mainApplication;
    private static final String sdkToken = "S1Q6gVj1b9XA6qkZ3yVZQYFpYpRAvmZA";
    private String floorPlanImageUrl = "";
    private MSTPoint mstPoint = null;
    private MSTMap mstMap = null;
    private boolean addedMap = false;
    private double scaleXFactor;
    private double scaleYFactor;
    private boolean scaleFactorCalled;
    private float floorImageLeftMargin;
    private float floorImageTopMargin;

    public HashMap<String, MSTVirtualBeacon> mstVirtualBeaconMap = new HashMap<>();

    public enum AlertType {
        bluetooth,
        network,
        location
    }

    @BindView(R.id.floorplan_layout)
    RelativeLayout floorplanLayout;
    @BindView(R.id.floorplan_bluedot)
    FrameLayout floorplanBluedotView;
    @BindView(R.id.floorplan_image)
    ImageView floorPlanImage;
    @BindView(R.id.progress_bar)
    ProgressBar progressBar;

    @BindColor(R.color.black)
    int blackColor;
    @BindColor(R.color.zone_color)
    int zoneColor;
    @BindColor(R.color.vb_color)
    int vbColor;

    private Unbinder unbinder;

    public static MapFragment newInstance() {
        return new MapFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.map_fragment, container, false);
        unbinder = ButterKnife.bind(this, view);
        progressBar.setVisibility(View.VISIBLE);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mainApplication = (MainApplication) getActivity().getApplication();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!Utils.isEmptyString(floorPlanImageUrl)) {
            renderImage(floorPlanImageUrl);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        initMISTSDK();
    }

    private void initMISTSDK() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                getActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            showLocationPermissionDialog();
        } else {
            startMistSdk();
        }
    }

    private void showLocationPermissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("This app needs location access");
        builder.setMessage("Please grant location access so this app can detect beacons in the background.");
        builder.setPositiveButton(android.R.string.ok, null);
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSION_REQUEST_FINE_LOCATION);
            }
        });
        builder.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_FINE_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "fine location permission granted !!");
                    startMistSdk();
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, " +
                            "this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }
                    });
                    builder.show();
                }
        }

    }

    private void startMistSdk() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled() &&
                Utils.isNetworkAvailable(getContext()) && Utils.isLocationServiceEnabled(getContext())) {
            runMISTSDK();
        } else {
            if (!Utils.isNetworkAvailable(getContext())) {
                showSettingsAlert(AlertType.network);
            }
            if (!Utils.isLocationServiceEnabled(getContext())) {
                showSettingsAlert(AlertType.location);
            }
            if (mBluetoothAdapter != null && !mBluetoothAdapter.isEnabled()) {
                showSettingsAlert(AlertType.bluetooth);
            }
        }
    }

    private void runMISTSDK() {
        MistManager mistManager = MistManager.newInstance(mainApplication);
        mistManager.init(sdkToken, this, AppMode.FOREGROUND);
    }

    private void showSettingsAlert(final AlertType alertType) {
        final String sTitle, sButton;
        if (alertType == AlertType.bluetooth) {
            sTitle = "Bluetooth is disabled in your device. Would you like to enable it?";
            sButton = "Goto Settings Page To Enable Bluetooth";
        } else if (alertType == AlertType.network) {
            sTitle = "Network Connection is disabled in your device. Would you like to enable it?";
            sButton = "Goto Settings Page To Enable Network Connection";
        } else {
            sTitle = "Location is disabled in your device. Would you like to enable it?";
            sButton = "Goto Settings Page To Enable Location";
        }

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setMessage(sTitle)
                .setCancelable(false)
                .setPositiveButton(sButton,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                Intent intentOpenBluetoothSettings = new Intent();
                                if (alertType == AlertType.bluetooth) {
                                    intentOpenBluetoothSettings.setAction(Settings.ACTION_BLUETOOTH_SETTINGS);
                                } else if (alertType == AlertType.network) {
                                    intentOpenBluetoothSettings.setAction(Settings.ACTION_WIFI_SETTINGS);
                                } else if (alertType == AlertType.location) {
                                    intentOpenBluetoothSettings.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                }
                                startActivity(intentOpenBluetoothSettings);
                            }
                        });
        alertDialogBuilder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        final AlertDialog.Builder builder = new
                                AlertDialog.Builder(getContext());
                        builder.setTitle("Functionality won't work");
                        builder.setMessage(sButton);
                        builder.setPositiveButton(android.R.string.ok, null);
                        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                            }
                        });
                        builder.show();
                    }
                });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    @Override
    public void onBeaconDetected(MSTBeacon[] beaconArray, String region, Date dateUpdated) {

    }

    @Override
    public void onBeaconDetected(JSONArray beaconArray, Date dateUpdated) {

    }

    @Override
    public void onBeaconListUpdated(HashMap<String, HashMap<Integer, Integer[]>> beaconList, Date dateUpdated) {

    }


    @Override
    public void onRelativeLocationUpdated(MSTPoint relativeLocation, MSTMap[] maps, Date dateUpdated) {
        if (relativeLocation != null && maps != null) {
            mstMap = maps[0];
            mstPoint = relativeLocation;
            updateRelativeLocation();
        }
    }

    private void updateRelativeLocation() {

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mstMap != null && addedMap) {
                    renderBlueDot(mstPoint);
                }
            }
        });
    }

    public void renderBlueDot(MSTPoint mstPoint) {

        if (this.floorPlanImage != null &&
                this.floorPlanImage.getDrawable() != null &&
                mstMap != null && mstPoint != null
                ) {

            this.mstPoint = mstPoint;

            float xPos = this.convertCloudPointToFloorplanXScale(mstPoint.getX());
            float yPos = this.convertCloudPointToFloorplanYScale(mstPoint.getY());

            if (this.scaleXFactor == 0 || this.scaleYFactor == 0) {
                //Defining the scaleX and scaleY for the map image
                if (!scaleFactorCalled)
                    setupScaleFactorForFloorplan();
            }

            if (this.floorplanBluedotView.getAlpha() == 0.0) {
                this.floorplanBluedotView.setAlpha((float) 1.0);
            }

            float leftMargin = floorImageLeftMargin + (xPos - (this.floorplanBluedotView.getWidth() / 2));
            float topMargin = floorImageTopMargin + (yPos - (this.floorplanBluedotView.getHeight() / 2));

            this.floorplanBluedotView.setX(leftMargin);
            this.floorplanBluedotView.setY(topMargin);
        } else if (this.floorPlanImage != null &&
                this.floorPlanImage.getDrawable() == null &&
                mstMap != null) {
        }
    }


    private void setupScaleFactorForFloorplan() {

        ViewTreeObserver vto = floorPlanImage.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                floorImageLeftMargin = floorPlanImage.getLeft();
                floorImageTopMargin = floorPlanImage.getTop();
                scaleFactorCalled = false;
                if (floorPlanImage.getDrawable() != null) {
                    scaleXFactor = (floorPlanImage.getWidth() / (double) floorPlanImage.getDrawable().getIntrinsicWidth());
                    scaleYFactor = (floorPlanImage.getHeight() / (double) floorPlanImage.getDrawable().getIntrinsicHeight());
                }
            }
        });
    }

    private float convertCloudPointToFloorplanXScale(double meter) {
        return (float) (meter * this.scaleXFactor * mstMap.getPpm());
    }

    private float convertCloudPointToFloorplanYScale(double meter) {
        return (float) (meter * this.scaleYFactor * mstMap.getPpm());
    }

    @Override
    public void onPressureUpdated(double pressure, Date dateUpdated) {

    }

    @Override
    public void onZoneStatsUpdated(MSTZone[] zones, Date dateUpdated) {

    }

    @Override
    public void onClientUpdated(MSTClient[] clients, MSTZone[] zones, Date dateUpdated) {

    }

    @Override
    public void onAssetUpdated(MSTAsset[] assets, MSTZone[] zones, Date dateUpdated) {

    }

    //called only once
    @Override
    public void onMapUpdated(MSTMap map, Date dateUpdated) {
        floorPlanImageUrl = map.getMapImageUrl();
        Log.d(TAG, floorPlanImageUrl);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                renderImage(floorPlanImageUrl);
            }
        });
    }

    private void renderImage(final String floorPlanImageUrl) {
        Log.d(TAG, "in picasso");
        addedMap = false;
        Picasso.with(getActivity()).
                load(floorPlanImageUrl).
                networkPolicy(NetworkPolicy.OFFLINE).
                into(floorPlanImage, new Callback() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "Image loaded successfully from the cached");
                        addedMap = true;
                        progressBar.setVisibility(View.GONE);

                        if (!scaleFactorCalled) {
                            setupScaleFactorForFloorplan();
                        }
                    }

                    @Override
                    public void onError() {
                        Picasso.with(getActivity()).
                                load(floorPlanImageUrl)
                                .into(floorPlanImage, new Callback() {
                                    @Override
                                    public void onSuccess() {
                                        progressBar.setVisibility(View.GONE);
                                        addedMap = true;
                                        if (!scaleFactorCalled) {
                                            setupScaleFactorForFloorplan();
                                        }
                                        Log.d(TAG, "Image downloaded from server successfully !!");
                                    }

                                    @Override
                                    public void onError() {
                                        progressBar.setVisibility(View.GONE);
                                        Log.d(TAG, "Could not download the image from the server");
                                    }
                                });
                    }
                });
    }

    @Override
    public void onVirtualBeaconListUpdated(MSTVirtualBeacon[] virtualBeacons, Date dateUpdated) {
        mstVirtualBeaconMap.clear();
        for (MSTVirtualBeacon vb : virtualBeacons) {
            mstVirtualBeaconMap.put(vb.getVbid(), vb);
        }
    }

    @Override
    public void onNotificationReceived(Date dateReceived, String message) {
        Log.d(TAG, "notification recieved!!");
        if (!Utils.isEmptyString(message)) {
            try {
                JSONObject notificationJSONObject = new JSONObject(message);
                String type = notificationJSONObject.getString("type");
                if (type.equalsIgnoreCase("zone-event-vb")) {
                    JSONObject messageObject = notificationJSONObject.optJSONObject("message");
                    if (messageObject != null) {
                        String proximity = messageObject.getString("proximity");
                        if (proximity.equals("near") || proximity.equals("immediate")) {
                            String messageToBeDisplayed = "";
                            String extra = messageObject.getString("Extra");
                            String vbID = messageObject.optString("vbID");
                            if (mstVirtualBeaconMap.containsKey(vbID)) {
                                MSTVirtualBeacon vb = mstVirtualBeaconMap.get(vbID);
                                messageToBeDisplayed = vb.getMessage();
                            }
                            if (TextUtils.isEmpty(messageToBeDisplayed)) {
                                if (TextUtils.isEmpty(extra)) {
                                    messageToBeDisplayed = "You're near the Anonymous VB";
                                } else {
                                    messageToBeDisplayed = String.format("You're %1$s %2$s" , proximity, extra);
                                }
                            }
                            showNotification(false, messageToBeDisplayed);
                        } else if (proximity.equals("far")) {
                            String messageToBeDisplayed = "";
                            String extra = messageObject.getString("Extra");
                            String vbID = messageObject.optString("vbID");
                            if (mstVirtualBeaconMap.containsKey(vbID)) {
                                MSTVirtualBeacon vb = mstVirtualBeaconMap.get(vbID);
                                messageToBeDisplayed = vb.getMessage();
                            }
                            if (TextUtils.isEmpty(messageToBeDisplayed)) {
                                if (TextUtils.isEmpty(extra)) {
                                    messageToBeDisplayed = "You're far from the Anonymous VB";
                                } else {
                                    messageToBeDisplayed = String.format("You're %1$s %2$s" , proximity, extra);
                                }
                            }
                            //action can be taken according to the need in case of far beacon
                        }
                    }

                } else if (type.equalsIgnoreCase("zones-events")) {
                    JSONObject messageObject = notificationJSONObject.optJSONObject("message");
                    if (messageObject != null) {
                        String trigger = messageObject.getString("Trigger");
                        if (trigger.equalsIgnoreCase("in")) {
                            String messageToBeDisplayed = "";
                            String extra = messageObject.getString("Extra");
                            if (TextUtils.isEmpty(extra)) {
                                messageToBeDisplayed = "You're in the Anonymous Zone";
                            } else {
                                messageToBeDisplayed = String.format("You're %1$s %2$s", trigger, extra);
                            }
                            showNotification(true, messageToBeDisplayed);
                        }
                        if (trigger.equalsIgnoreCase("out")) {
                            String messageToBeDisplayed = "";
                            String extra = messageObject.getString("Extra");
                            if (TextUtils.isEmpty(extra)) {
                                messageToBeDisplayed = "You left the Anonymous Zone";
                            } else {
                                messageToBeDisplayed = String.format("You left the %1$s", extra);
                            }
                            showNotification(true, messageToBeDisplayed);
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void showNotification(boolean isZone, String message) {
        final Snackbar snackBar = Snackbar.make(getActivity().findViewById(android.R.id.content),
                message, Snackbar.LENGTH_LONG);
        snackBar.setAction("Dismiss", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackBar.dismiss();
            }
        });
        View snackBarView = snackBar.getView();
        TextView textView = snackBarView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(blackColor);
        if (isZone) {
            snackBarView.setBackgroundColor(zoneColor);
        } else {
            snackBarView.setBackgroundColor(vbColor);
        }
        snackBar.show();
    }

    @Override
    public void onClientInformationUpdated(String clientName) {

    }

    @Override
    public void onReceivedSecret(String orgName, String orgID, String sdkSecret, String error) {

    }

    @Override
    public void receivedLogMessageForCode(String message, MSTCentralManagerStatusCode code) {
        Log.d(TAG, "receivedLogMessageForCode" + message);
    }

    @Override
    public void receivedVerboseLogMessage(String message) {
        Log.d(TAG, "receivedVerboseLogMessage" + message);
    }

    @Override
    public void onMistErrorReceived(String message, Date date) {

    }

    @Override
    public void onMistRecommendedAction(String message) {

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        MistManager.newInstance(mainApplication).disconnect();
    }

    @Override
    public void onStop() {
        super.onStop();
        MistManager.newInstance(mainApplication).disconnect();
    }
}
