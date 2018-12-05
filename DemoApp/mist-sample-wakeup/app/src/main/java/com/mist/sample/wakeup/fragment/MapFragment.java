package com.mist.sample.wakeup.fragment;


import android.Manifest;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
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

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.MessageFilter;
import com.google.android.gms.nearby.messages.MessagesOptions;
import com.google.android.gms.nearby.messages.NearbyMessagesStatusCodes;
import com.google.android.gms.nearby.messages.NearbyPermissions;
import com.google.android.gms.nearby.messages.Strategy;
import com.google.android.gms.nearby.messages.SubscribeOptions;
import com.mist.android.AppMode;
import com.mist.android.BatteryUsage;
import com.mist.android.MSTAsset;
import com.mist.android.MSTBeacon;
import com.mist.android.MSTCentralManagerIndoorOnlyListener;
import com.mist.android.MSTCentralManagerStatusCode;
import com.mist.android.MSTClient;
import com.mist.android.MSTMap;
import com.mist.android.MSTPoint;
import com.mist.android.MSTVirtualBeacon;
import com.mist.android.MSTZone;
import com.mist.android.MistLocationAdvanceListener;
import com.mist.android.model.AppModeParams;
import com.mist.sample.wakeup.R;
import com.mist.sample.wakeup.app.MainApplication;
import com.mist.sample.wakeup.model.OrgData;
import com.mist.sample.wakeup.receiver.NearByBroadCastReceiver;
import com.mist.sample.wakeup.service.NearByJobIntentService;
import com.mist.sample.wakeup.utils.MistManager;
import com.mist.sample.wakeup.utils.SharedPrefUtils;
import com.mist.sample.wakeup.utils.Utils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;

import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by anubhava on 26/03/18.
 */

public class MapFragment extends Fragment implements MSTCentralManagerIndoorOnlyListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, MistManager.fragmentInteraction {

    public static final String TAG = MapFragment.class.getSimpleName();
    private static final int PERMISSION_REQUEST_FINE_LOCATION = 1;
    private static final String SDK_TOKEN = "sdkToken";
    private MainApplication mainApplication;
    private String sdkToken;
    private String floorPlanImageUrl = "";
    private MSTPoint mstPoint = null;
    private boolean addedMap = false;
    private double scaleXFactor;
    private double scaleYFactor;
    private boolean scaleFactorCalled;
    private float floorImageLeftMargin;
    private float floorImageTopMargin;
    public MSTMap currentMap;
    private Unbinder unbinder;
    private HandlerThread sdkHandlerThread;
    private Handler sdkHandler;
    private GoogleApiClient googleApiClient;

    @Override
    public void onOrgDataReceived() {
        subscribe();
    }

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
    @BindView(R.id.txt_error)
    TextView txtError;


    public static MapFragment newInstance(String sdkToken) {
        Bundle bundle = new Bundle();
        bundle.putString(SDK_TOKEN, sdkToken);
        MapFragment mapFragment = new MapFragment();
        mapFragment.setArguments(bundle);
        return mapFragment;
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
        if (getActivity() != null)
            mainApplication = (MainApplication) getActivity().getApplication();
        if (getArguments() != null)
            sdkToken = getArguments().getString(SDK_TOKEN);
			 if (havePermissions()) {
            buildGoogleApiClient();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sdkHandlerThread = new HandlerThread("SDKHandler");
        sdkHandlerThread.start();
        sdkHandler = new Handler(sdkHandlerThread.getLooper());
        MistManager.newInstance(mainApplication).setFragmentInteractionListener(this);
    }

    @Override
    public void onStart() {

        super.onStart();
        try {
            //stopping the scheduled job when the app comes to the foreground
            Utils.stopScheduledJob(mainApplication);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        //disconnecting the Mist sdk, to make sure there is no prior active instance
        MistManager.newInstance(mainApplication).disconnect();
        MistManager.newInstance(mainApplication).
                setAppMode(new AppModeParams(AppMode.FOREGROUND,
                        BatteryUsage.HIGH_BATTERY_USAGE_HIGH_ACCURACY,
                        true, 0.5, 1));

        //initializing the Mist sdk
        initMISTSDK();
        SharedPrefUtils.setShouldShowWelcome(getActivity(), true);
    }

    @Override
    public void onStop() {
        super.onStop();
        //stopping the Mist sdk
        MistManager.newInstance(mainApplication).disconnect();
        MistManager.newInstance(mainApplication).
                setAppMode(new AppModeParams(AppMode.BACKGROUND,
                        BatteryUsage.LOW_BATTERY_USAGE_LOW_ACCURACY,
                        true, 0.5, 1));
        sdkHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
        try {
            //scheduling the job to run Mist sdk in the background
            Utils.scheduleJob(mainApplication.getApplicationContext());
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }
        }, 500);

        //disconnecting from the google api client
        if (googleApiClient != null && googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
        SharedPrefUtils.setShouldShowWelcome(getActivity(), true);
    }

    private void initMISTSDK() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && getActivity() != null &&
                getActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            showLocationPermissionDialog();
        } else {
            startMistSdk();
        }
    }

    //permission dialogs
    private void showLocationPermissionDialog() {
        if (getActivity() != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (getActivity() != null) {
        switch (requestCode) {
            case PERMISSION_REQUEST_FINE_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "fine location permission granted !!");
                    startMistSdk();
                    buildGoogleApiClient();
                    if (googleApiClient != null && !googleApiClient.isConnected()) {
                        googleApiClient.connect();
                    }
                } else {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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
    }

    /**
     * This method checks for the availability for Internet , Location and Bluetooth and show dialog if anything is not enabled else start the Mist SDK
     */
    private void startMistSdk() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled() && getActivity() != null &&
                Utils.isNetworkAvailable(getActivity()) && Utils.isLocationServiceEnabled(getActivity())) {
            runMISTSDK();
        } else {
            if (getActivity() != null && !Utils.isNetworkAvailable(getActivity())) {
                showSettingsAlert(AlertType.network);
            }
            if (getActivity() != null && !Utils.isLocationServiceEnabled(getActivity())) {
                showSettingsAlert(AlertType.location);
            }
            if (mBluetoothAdapter != null && !mBluetoothAdapter.isEnabled()) {
                showSettingsAlert(AlertType.bluetooth);
            }
        }
    }

    //initializing the Mist sdk with sdkToken
    private void runMISTSDK() {
        MistManager mistManager = MistManager.newInstance(mainApplication);
        mistManager.init(sdkToken, this, AppMode.FOREGROUND);
    }

    /**
     * This method show the alert as per AlertType
     *
     * @param alertType Type of Alert
     *                  bluetooth
     *                  network
     *                  location
     */
    private void showSettingsAlert(final AlertType alertType) {
        if (getActivity() != null) {
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
                                    AlertDialog.Builder(getActivity());
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

    /**
     * This callback provide the location of the device
     *
     * @param relativeLocation provide x,y of the device on particular map
     * @param maps
     * @param dateUpdated      time stamp of the location provided
     */
    @Override
    public void onRelativeLocationUpdated(MSTPoint relativeLocation, MSTMap[] maps, Date dateUpdated) {
        if (relativeLocation != null && maps != null) {
            mstPoint = relativeLocation;
            updateRelativeLocation();
        }
    }

    private void updateRelativeLocation() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (currentMap != null && addedMap) {
                        renderBlueDot(mstPoint);
                    }
                }
            });
        }
    }

    //logic to show the blue dot for the location
    public void renderBlueDot(final MSTPoint point) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (floorPlanImage != null && floorPlanImage.getDrawable() != null && currentMap != null && point != null && addedMap) {
                        float xPos = convertCloudPointToFloorplanXScale(point.getX());
                        float yPos = convertCloudPointToFloorplanYScale(point.getY());

                        // If scaleX and scaleY are not defined, check again
                        if (!scaleFactorCalled && (scaleXFactor == 0 || scaleYFactor == 0)) {
                            setupScaleFactorForFloorplan();
                        }
                        float leftMargin = floorImageLeftMargin + (xPos - (floorplanBluedotView.getWidth() / 2));
                        float topMargin = floorImageTopMargin + (yPos - (floorplanBluedotView.getHeight() / 2));

                        floorplanBluedotView.setX(leftMargin);
                        floorplanBluedotView.setY(topMargin);
                        floorplanBluedotView.setVisibility(View.VISIBLE);
                    }
                }
            });
        }
    }


    //calculating the scale factors
    private void setupScaleFactorForFloorplan() {
        if (floorPlanImage != null) {
            ViewTreeObserver vto = floorPlanImage.getViewTreeObserver();
            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (floorPlanImage != null) {
                        floorImageLeftMargin = floorPlanImage.getLeft();
                        floorImageTopMargin = floorPlanImage.getTop();
                        if (floorPlanImage.getDrawable() != null) {
                            scaleXFactor = (floorPlanImage.getWidth() / (double) floorPlanImage.getDrawable().getIntrinsicWidth());
                            scaleYFactor = (floorPlanImage.getHeight() / (double) floorPlanImage.getDrawable().getIntrinsicHeight());
                                scaleFactorCalled = true;
                        }
                    }
                }
            });
        }
    }

    //converting the x point from meter's to pixel with the present scaling factor of the map rendered in the imageview
    private float convertCloudPointToFloorplanXScale(double meter) {
        return (float) (meter * this.scaleXFactor * currentMap.getPpm());
    }

    //converting the y point from meter's to pixel with the present scaling factor of the map rendered in the imageview
    private float convertCloudPointToFloorplanYScale(double meter) {
        return (float) (meter * this.scaleYFactor * currentMap.getPpm());
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

    /**
     * This callback provide the detail of map user is on
     *
     * @param map         Map object having details about the map
     * @param dateUpdated
     */
    @Override
    public void onMapUpdated(MSTMap map, Date dateUpdated) {
        floorPlanImageUrl = map.getMapImageUrl();
        Log.d(TAG, floorPlanImageUrl);
        if (getActivity() != null && (floorPlanImage.getDrawable() == null || this.currentMap == null || !this.currentMap.getMapId().equals(map.getMapId()))) {
            // Set the current map
            this.currentMap = map;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    renderImage(floorPlanImageUrl);
                }
            });
        }
    }

    /**
     * This method is used for rendering the map image using the url from the MSTMap object received from OnMapUpdated callback
     *
     * @param floorPlanImageUrl map image url
     */
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
                                        if(progressBar!=null) {
                                            progressBar.setVisibility(View.GONE);
                                        }
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

    }

    @Override
    public void onNotificationReceived(Date dateReceived, String message) {

    }

    @Override
    public void onClientInformationUpdated(String clientName) {

    }

    @Override
    public void onReceivedSecret(String orgName, String orgID, String sdkSecret, String error) {

    }

    @Override
    public void receivedLogMessageForCode(String message, MSTCentralManagerStatusCode code) {
    }

    @Override
    public void receivedVerboseLogMessage(String message) {
    }

    //callback for error
    @Override
    public void onMistErrorReceived(String message, Date date) {
        progressBar.setVisibility(View.GONE);
        txtError.setVisibility(View.VISIBLE);
        txtError.setText(message);
    }

    @Override
    public void onMistRecommendedAction(String message) {

    }



    private synchronized void buildGoogleApiClient() {
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(getActivity())
                    .addApi(Nearby.MESSAGES_API, new MessagesOptions.Builder()
                            .setPermissions(NearbyPermissions.BLE)
                            .build())
                    .addConnectionCallbacks(this)
                    .build();
        }
    }

    private boolean havePermissions() {
        return ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "GoogleApiClient Connected !!");
        subscribe();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, getString(R.string.connection_suspended) + i);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Snackbar.make(getActivity().findViewById(android.R.id.content),
                getString(R.string.play_service_exception) + connectionResult.getErrorMessage(),
                Snackbar.LENGTH_LONG).show();
    }

    private void subscribe() {

        String orgId = null, subOrgId = null;

        OrgData orgData = SharedPrefUtils.readConfig(getActivity().getApplicationContext(), sdkToken);

        if (orgData != null) {
            orgId = orgData.getOrgId();
            subOrgId = orgId.substring(0, orgId.length() - 2);

            MessageFilter filter = new MessageFilter.Builder()
                    .includeIBeaconIds(UUID.fromString(orgId), null, null)
                    .includeIBeaconIds(UUID.fromString("00000000-0000-0000-0000-000000000001"), null, null)
                    .includeIBeaconIds(UUID.fromString("00000000-0000-0000-0000-000000000002"), null, null)
                    .includeIBeaconIds(UUID.fromString("00000000-0000-0000-0000-000000000003"), null, null)
                    .includeIBeaconIds(UUID.fromString("00000000-0000-0000-0000-000000000004"), null, null)
                    .includeIBeaconIds(UUID.fromString("00000000-0000-0000-0000-000000000005"), null, null)
                    .includeIBeaconIds(UUID.fromString("00000000-0000-0000-0000-000000000006"), null, null)
                    .includeIBeaconIds(UUID.fromString("00000000-0000-0000-0000-000000000007"), null, null)
                    .includeIBeaconIds(UUID.fromString("00000000-0000-0000-0000-000000000008"), null, null)
                    .includeIBeaconIds(UUID.fromString("00000000-0000-0000-0000-000000000009"), null, null)
                    .includeIBeaconIds(UUID.fromString("00000000-0000-0000-0000-00000000000a"), null, null)
                    .includeIBeaconIds(UUID.fromString("00000000-0000-0000-0000-00000000000b"), null, null)
                    .includeIBeaconIds(UUID.fromString("00000000-0000-0000-0000-00000000000c"), null, null)
                    .includeIBeaconIds(UUID.fromString("00000000-0000-0000-0000-00000000000d"), null, null)
                    .includeIBeaconIds(UUID.fromString(subOrgId + "00"), null, null)
                    .includeIBeaconIds(UUID.fromString(subOrgId + "01"), null, null)
                    .includeIBeaconIds(UUID.fromString(subOrgId + "02"), null, null)
                    .includeIBeaconIds(UUID.fromString(subOrgId + "03"), null, null)
                    .includeIBeaconIds(UUID.fromString(subOrgId + "04"), null, null)
                    .includeIBeaconIds(UUID.fromString(subOrgId + "05"), null, null)
                    .includeIBeaconIds(UUID.fromString(subOrgId + "06"), null, null)
                    .includeIBeaconIds(UUID.fromString(subOrgId + "07"), null, null)
                    .includeIBeaconIds(UUID.fromString(subOrgId + "08"), null, null)
                    .includeIBeaconIds(UUID.fromString(subOrgId + "0a"), null, null)
                    .includeIBeaconIds(UUID.fromString(subOrgId + "0b"), null, null)
                    .includeIBeaconIds(UUID.fromString(subOrgId + "0c"), null, null)
                    .includeIBeaconIds(UUID.fromString(subOrgId + "0d"), null, null)
                    .build();

            SubscribeOptions options = new SubscribeOptions.Builder()
                    .setStrategy(Strategy.BLE_ONLY)
                    .setFilter(filter)
                    .build();

            Nearby.Messages.subscribe(googleApiClient, getPendingIntent(), options)
                    .setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(@NonNull Status status) {
                            if (status.isSuccess()) {
                                Log.d(TAG, "Successfully Subscribed");
                                getActivity().startService(getBackgroundSubscribeServiceIntent());
                            } else {
                                Log.d(TAG, "Operation Failed, Error : " +
                                        NearbyMessagesStatusCodes.getStatusCodeString(status.getStatusCode()));
                            }
                        }
                    });
        }
    }

    private Intent getBackgroundSubscribeServiceIntent() {
        return new Intent(getActivity(), NearByJobIntentService.class);
    }

    private PendingIntent getPendingIntent() {
        Intent nearByIntent = new Intent(getActivity(), NearByBroadCastReceiver.class);
        return PendingIntent.getBroadcast(getActivity(), 0, nearByIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        sdkHandler = null;

        if (sdkHandlerThread != null) {
            sdkHandlerThread.quitSafely();
            sdkHandlerThread = null;
        }

        try {
            //stopping the scheduled job when the app comes to the foreground
            Utils.stopScheduledJob(mainApplication);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        //disconnecting the Mist sdk, to make sure there is no prior active instance
        MistManager.newInstance(mainApplication).destroy();
    }
}
