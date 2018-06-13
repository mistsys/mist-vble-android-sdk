package com.mist.sample.wayfinding.fragment;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.DisplayMetrics;
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

import com.google.android.gms.maps.model.LatLng;
import com.mist.android.AppMode;
import com.mist.android.BatteryUsage;
import com.mist.android.MSTAsset;
import com.mist.android.MSTBeacon;
import com.mist.android.MSTCentralManagerIndoorOnlyListener;
import com.mist.android.MSTCentralManagerStatusCode;
import com.mist.android.MSTClient;
import com.mist.android.MSTMap;
import com.mist.android.MSTPoint;
import com.mist.android.MSTPointType;
import com.mist.android.MSTVirtualBeacon;
import com.mist.android.MSTZone;
import com.mist.android.MistLocationAdvanceListener;

import com.mist.android.deadReckoning.path.MSTEdges;
import com.mist.android.deadReckoning.path.MSTNode;
import com.mist.android.deadReckoning.path.Utility;
import com.mist.android.model.AppModeParams;
import com.mist.sample.wayfinding.R;
import com.mist.sample.wayfinding.app.MainApplication;
import com.mist.sample.wayfinding.model.SurveyPoints;
import com.mist.sample.wayfinding.util.DrawLine;
import com.mist.sample.wayfinding.util.MSTGraph;
import com.mist.sample.wayfinding.util.MSTPath;
import com.mist.sample.wayfinding.util.MSTWayFinder;
import com.mist.sample.wayfinding.util.MistManager;
import com.mist.sample.wayfinding.util.Utils;
import com.mist.sample.wayfinding.util.ZoomLayout;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by anubhava on 02/04/18.
 */

public class MapFragment extends Fragment implements MSTCentralManagerIndoorOnlyListener, MistLocationAdvanceListener,
        ZoomLayout.ZoomViewTouchListener {

    public static final String TAG = MapFragment.class.getSimpleName();
    private static final int PERMISSION_REQUEST_FINE_LOCATION = 1;
    private MainApplication mainApplication;
    public static String sdkToken = "";
    private String floorPlanImageUrl = "";
    private MSTPoint mstPoint = null;
    private MSTMap mstMap = null;
    private boolean addedMap = false;
    private double scaleXFactor;
    private float zoomScaleFactor = 1;
    private double scaleYFactor;
    private boolean scaleFactorCalled;
    private float floorImageLeftMargin;
    private float floorImageTopMargin;
    private Unbinder unbinder;
    private HandlerThread sdkHandlerThread;
    private Handler sdkHandler;
    private MSTMap currentMap;
    private Handler blueDotHandler;
    private ArrayList<CharSequence> surveyNames;
    private HashMap<Integer, JSONObject> multiSurveyPath;
    private boolean isNewPath = false;
    private String mapName = "";
    private boolean isScaleFactorCalculated = false;
    private int scale;
    private WayfinerAsyncTask wayfinerAsyncTask;
    private boolean hasAddedWayfinding;
    private MSTWayFinder wayfinder;
    private HashMap<String, Object> nodes;
    //private MSTPoint currentMstPoint = null;
    private MSTGraph graph;
    private boolean isActualData = false;
    private MSTPoint startingPoint;
    private MSTPoint endingPoint, lastEndingPoint;
    private MSTPoint showXYMstPoint;
    private MSTPoint preFVPoint;
    private boolean isWayfindingAdded = false;
    private boolean isAsycTaskFinished = true;
    private RenderWayfindingAsyncTask renderWayfindingAsyncTask;
    String startingName, endingName;
    ArrayList<String> pathArr;
    private ArrayList<String> _previousPathArr;
    ArrayList<MSTPath> pathArrayList;
    MSTPoint nearestMstPoint = null, closestMstPoint;


    @Override
    public void onTouchZoomView(float x, float y) {
        drawTouchedDot(x, y);
        //Log.v("onTouchZoomView", ""+x+","+y);

    }

    public void drawTouchedDot(float x, float y) {

        if (this.currentMap != null) {
            setDestinationPoint(new MSTPoint(x, y));
            //renderWayfinding();
        }
    }

    @Override
    public void onZoomScaleValue(float scale) {
//       /* float scale1 = 0;
// *//*       if (scale <= 1.5)
//            scale1 = 1;
//        else {
//            if (scale > 3)
//                scale1 = (float) 0.3;
//            else if (scale >= 1.5)
//                scale1 = (float) 0.5;
//            else
//                scale1 = (float) 0.8;
//        }*//*
//        scale1 = 1 / scale;
//
//        this.zoomScaleFactor = scale1;
//
//        View view1 = floorplanLayout.findViewById(R.id.floorplan_bluedot);
//        View view2 = floorplanLayout.findViewWithTag("renderNearestBluedot");
//        View view3 = floorplanLayout.findViewWithTag("wayfindingpath");
//
//        setScaleValue(view1, scale1);
//        setScaleValue(view2, scale1);
//        setScaleValue(view3, scale1);
//        setScaleValue(snapPathDestinationView, scale1);
//
//
//        int childcount = floorplanVBLayout.getChildCount();
//        View v;
//        for (int i = 0; i < childcount; i++) {
//            v = floorplanVBLayout.getChildAt(i);
//            setScaleValue(v, scale1);
//        }
//        RelativeLayout breadCrumbLayout = (RelativeLayout) floorplanLayout.findViewWithTag("motionLayout");
//        if (breadCrumbLayout != null) {
//            int breadCrumbChildCount = breadCrumbLayout.getChildCount();
//            View breadCrumbView;
//            for (int i = 0; i < breadCrumbChildCount; i++) {
//                breadCrumbView = breadCrumbLayout.getChildAt(i);
//                setScaleValue(breadCrumbView, scale1);
//            }
//        }
////        if(testPathDotLabelArray.size()>0) {
////            View label;
////            for (int i = 0; i < testPathDotLabelArray.size(); i++) {
////                label = floorplanLayout.findViewWithTag(testPathDotLabelArray.get(i).getTag());
////                setScaleValue(label, scale1);
////            }
////        }
//*/
    }

    private void setScaleValue(View view, float scale) {
        if (view != null && view.getVisibility() == View.VISIBLE) {
            view.setScaleX(scale);
            view.setScaleY(scale);
        }
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
    @BindView(R.id.floorplan_zoomlayout)
    ZoomLayout zoomLayout;

    public static MapFragment newInstance(String sdkToken) {
        MapFragment.sdkToken = sdkToken;
        return new MapFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.map_fragment, container, false);
        unbinder = ButterKnife.bind(this, view);
        progressBar.setVisibility(View.VISIBLE);
        blueDotHandler = new Handler(Looper.getMainLooper());
        zoomLayout.setListener(this);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mainApplication = (MainApplication) getActivity().getApplication();
        surveyNames = new ArrayList<>();
        multiSurveyPath = new HashMap<>();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //handler = new Handler(Looper.getMainLooper());
        sdkHandlerThread = new HandlerThread("SDKHandler");
        sdkHandlerThread.start();
        sdkHandler = new Handler(sdkHandlerThread.getLooper());
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
        sdkHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //initializing the Mist sdk
                initMistSdk();
            }
        }, 500);
        if (blueDotHandler != null) {
            blueDotHandler.post(sendingTask);
        } else {
            blueDotHandler = new Handler(Looper.getMainLooper());
            blueDotHandler.post(sendingTask);
        }
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
        blueDotHandler.removeCallbacks(sendingTask);
        blueDotHandler = null;
    }

    private void initMistSdk() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                getContext().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
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
            if (!mBluetoothAdapter.isEnabled()) {
                showSettingsAlert(AlertType.bluetooth);
            }
        }
    }

    //initializing the Mist Sdk with sdkToken
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

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
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

    //called when new location is received
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

    //logic to show the blue dot for the location
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

        }
//        else if (this.floorPlanImage != null &&
//                this.floorPlanImage.getDrawable() == null &&
//                mstMap != null) {
//        }
    }

    //calculating the scale factors
    private void setupScaleFactorForFloorplan() {
        if (floorPlanImage != null) {
            ViewTreeObserver vto = floorPlanImage.getViewTreeObserver();
            if (vto != null) {
                vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        if (floorPlanImage != null) {
                            floorPlanImage.getViewTreeObserver().addOnGlobalLayoutListener(this);
                            floorImageLeftMargin = floorPlanImage.getLeft();
                            floorImageTopMargin = floorPlanImage.getTop();
                            scaleFactorCalled = false;
                            if (floorPlanImage.getDrawable() != null) {
                                scaleXFactor = (floorPlanImage.getWidth() /
                                        (double) floorPlanImage.getDrawable().getIntrinsicWidth());
                                scaleYFactor = (floorPlanImage.getHeight() /
                                        (double) floorPlanImage.getDrawable().getIntrinsicHeight());
                            }

                        }
                    }
                });
            }
        }
    }

    //converting the x point from meter's to pixel with the present scaling
    private float convertCloudPointToFloorplanXScale(double meter) {
        return (float) (meter * this.scaleXFactor * mstMap.getPpm());
    }

    //converting the y point from meter's to pixel with the present scaling
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

    //called when new map is received
    @Override
    public void onMapUpdated(final MSTMap map, Date dateUpdated) {
        floorPlanImageUrl = map.getMapImageUrl();
        Log.d(TAG, floorPlanImageUrl);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //renderImage(floorPlanImageUrl);
                addIndoorMap(map);
            }
        });
    }

    private void addIndoorMap(MSTMap mstMap) {
        if (mstMap != null) {
            if (this.currentMap == null || !this.currentMap.getMapId().equals(mstMap.getMapId())) {
                this.currentMap = mstMap;
                loadSiteSurveyPathReceivedFromServer();
                isNewPath = true;
                mapName = mstMap.getMapName();
                isScaleFactorCalculated = false;
                scaleXFactor = 0;
                scaleYFactor = 0;
                //loadMapFromSDK();
                loadMap();
            }
        }
    }

    private void loadSiteSurveyPathReceivedFromServer() {
        try {
            if (currentMap != null && !TextUtils.isEmpty(this.currentMap.getSiteSurveyPath())) {
                JSONObject siteSurveyPath = null;
                JSONArray siteSurveyPathString = new JSONArray(this.currentMap.getSiteSurveyPath());
                int index = 0;
                surveyNames.clear();
                multiSurveyPath.clear();
                for (int i = 0; i < siteSurveyPathString.length(); i++) {
                    if (siteSurveyPathString.opt(i) != null) {
                        siteSurveyPath = new JSONObject(siteSurveyPathString.opt(i).toString());
                    }
                    if (siteSurveyPath != null) {
                        JSONArray nodesFromFile = siteSurveyPath.optJSONArray("nodes");
                        if (!Utility.isEmpty(nodesFromFile) && nodesFromFile.length() > 0) {
                            ArrayList<SurveyPoints> tempArray = new ArrayList<>();
                            for (int j = 0; j < nodesFromFile.length(); j++) {
                                JSONObject node = (JSONObject) nodesFromFile.get(j);
                                JSONObject position = node.getJSONObject("position");
                                String name = node.getString("name");

                                double x = position.getDouble("x");
                                double y = position.getDouble("y");
                                PointF point = new PointF((float) (x), (float) (y));
                                tempArray.add(new SurveyPoints(point, name));
                            }
                            //
                            siteSurveyPath.put("path-data", tempArray);
                            multiSurveyPath.put(index, siteSurveyPath);
                            surveyNames.add(siteSurveyPath.optString("name"));
                            index++;
                        }
                    }
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "Encountered JSONException while parsing survey path: " + e.getLocalizedMessage());
        }
    }


    private void loadMap() {
        if (this.currentMap != null) {
            try {
                DisplayMetrics displayMetrics = new DisplayMetrics();
                getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                int height = displayMetrics.heightPixels;
                double dscale = (currentMap.getMapHeight() * currentMap.getPpm() / height);
                if (dscale < 1) {
                    scale = 1;
                } else {
                    scale = (int) dscale;
                }

                Picasso.with(getActivity()).load(currentMap.getMapImageUrl()).resize((int)
                        (currentMap.getMapWidth() * currentMap.getPpm() / scale), (int)
                        (currentMap.getMapHeight() * currentMap.getPpm() / scale)).into(floorPlanImage);
                // Defining the scaleX and scaleY for the map image
                if (!isScaleFactorCalculated) {
                    setupScaleFactorForFloorplan(new Callback() {
                        @Override
                        public void onSuccess() {
                            addedMap = true;
                            progressBar.setVisibility(View.GONE);
                            callWayfinerAsyncTask();
                        }

                        @Override
                        public void onError() {

                        }
                    });

                }
            } catch (Exception e) {
                Log.e(TAG, "Exception in loadMap " + e.getMessage());
//                if (preferenceHelper.getBooleanValue(PreferenceConstants.KEY_ENABLE_PACKET_LOGGING)) {
//                    Utility.saveLogInFile(Utility.APP_SIDE_LOGGING_FILENAME, Utility.getDateString() + "\t" + "Exception in loadMap " + e.getMessage());
//                }
            }
        }

    }

    private void callWayfinerAsyncTask() {
        if (wayfinerAsyncTask != null && wayfinerAsyncTask.getStatus() != AsyncTask.Status.FINISHED) {
            wayfinerAsyncTask.cancel(true);
        }

        wayfinerAsyncTask = null;
        wayfinerAsyncTask = new WayfinerAsyncTask();
        wayfinerAsyncTask.execute();
    }

    private void setupScaleFactorForFloorplan(final Callback cb) {
        if (!isScaleFactorCalculated && (scaleXFactor == 0 || scaleYFactor == 0)) {
            ViewTreeObserver vto = this.floorPlanImage.getViewTreeObserver();
            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    floorPlanImage.getViewTreeObserver().removeGlobalOnLayoutListener(this);

                    if (floorPlanImage.getDrawable() != null) {
                        floorImageLeftMargin = floorPlanImage.getLeft();
                        floorImageTopMargin = floorPlanImage.getTop();
                        scaleXFactor = (floorPlanImage.getWidth() / (double) floorPlanImage.getDrawable().getIntrinsicWidth());
                        scaleYFactor = (floorPlanImage.getHeight() / (double) floorPlanImage.getDrawable().getIntrinsicHeight());
                        isScaleFactorCalculated = true;
                    }
                    cb.onSuccess();
                }
            });
        } else {
            cb.onError();
        }
    }

    //map image rendering
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

    //called when successfully registration fails
    @Override
    public void onMistErrorReceived(String message, Date date) {
        progressBar.setVisibility(View.GONE);
        txtError.setVisibility(View.VISIBLE);
        txtError.setText(message);
    }

    @Override
    public void onMistRecommendedAction(String message) {

    }

    @Override
    public void onDRSnappedLocationUpdated(MSTPoint mstPoint, MSTMap mstMap, Date date) {
        Log.d(TAG, "DR Snapped Location Updated.");
    }

    @Override
    public void onDRRawLocationUpdated(MSTPoint mstPoint, MSTMap mstMap, Date date) {
        Log.d(TAG, "DR Raw Location Updated.");
    }

    @Override
    public void onLESnappedLocationUpdated(MSTPoint mstPoint, MSTMap mstMap, Date date) {
        Log.d(TAG, "LE Snapped Location Updated.");
    }

    @Override
    public void onLERawLocationUpdated(MSTPoint mstPoint, MSTMap mstMap, Date date) {
        Log.d(TAG, "LE Raw Location Updated.");
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
        MistManager.newInstance(mainApplication).destory();
    }

    private Runnable sendingTask = new Runnable() {
        @Override
        public void run() {
            blueDotHandler.postDelayed(this, 1000);
            renderBluedot1(mstPoint);
        }
    };


    private class WayfinerAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            if (!hasAddedWayfinding)
                getWayFindingData();
            if (hasAddedWayfinding && wayfinder != null) {
                wayfinder.getShowPathList(nodes, scaleXFactor, scaleYFactor, currentMap);
                wayfinder.drawShowPath(nodes, scaleXFactor, scaleYFactor, currentMap);
            }
            //setMSTVirtualBeacon(this.mstVirtualBeaconArrayList);
            return null;
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            renderBluedot1(mstPoint);
            //addVirtualBeacon();
        }
    }

    private void getWayFindingData() {
        if (currentMap == null)
            return;
        JSONObject wayfindingPath = null;
        String wayFindingPathString = this.currentMap.getWayfindingPath();
        if (!TextUtils.isEmpty(wayFindingPathString)) {
            try {
                wayfindingPath = new JSONObject(wayFindingPathString);
            } catch (JSONException e) {
                Log.e(TAG, "JSONException while creating wayfindingPath" + e.getMessage());
            }
        }
        if (wayfindingPath != null) {
            loadWayfindingData(wayfindingPath);
        } else {
            Log.w(TAG, "Wayfinding path is set");
        }


    }

    /**
     * Render the bluedot based on relative location.
     *
     * @param mstPoint
     */
    public void renderBluedot(MSTPoint mstPoint) {
        // this.currentMstPoint = mstPoint;
    }

    private void loadWayfindingData(JSONObject mapJSON) {

        if (this.graph == null) {
            this.graph = new MSTGraph();
        }
        if (this.nodes == null) {
            this.nodes = new HashMap<>();
        }

        try {

            String sCoordinate = mapJSON.optString("coordinate");
            if (!Utility.isEmptyString(sCoordinate) && sCoordinate.equals("actual"))
                isActualData = true;
            else
                isActualData = false;

            JSONArray nodesFromFile = mapJSON.optJSONArray("nodes");

            if (!Utility.isEmpty(nodesFromFile) && nodesFromFile.length() > 0) {

                for (int i = 0; i < nodesFromFile.length(); i++) {
                    JSONObject node = (JSONObject) nodesFromFile.get(i);
                    String name = node.getString("name");
                    JSONObject position = node.getJSONObject("position");
                    JSONObject edges = node.getJSONObject("edges");
                    double x = position.getDouble("x");
                    double y = position.getDouble("y");
                    this.nodes.put(name, new MSTNode(name, new MSTPoint(x, y), edges));
                }

                Iterator<Map.Entry<String, Object>> it = this.nodes.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<String, Object> node = it.next();
                    String nodeName = node.getKey();
                    MSTNode mMSTNode = (MSTNode) node.getValue();
                    // this.calculateEdgeDistanceForNode(mMSTNode);
                    this.graph.addVertex(nodeName, mMSTNode.getEdges());
                }

                this.hasAddedWayfinding = true;
                this.wayfinder = new MSTWayFinder(mapJSON, isActualData);

            }
        } catch (JSONException e) {
            Log.e(TAG, "Encountered JSONException: " + e.getLocalizedMessage());
        }

    }

    /**
     * Render the bluedot based on relative location.
     *
     * @param mstPoint
     */
    public void renderBluedot1(MSTPoint mstPoint) {

        if (floorPlanImage != null && this.currentMap != null && mstPoint != null) {

            if (this.floorPlanImage.getDrawable() != null) {
                float xPos = this.convertCloudPointToFloorplanXScale(mstPoint.getX());
                float yPos = this.convertCloudPointToFloorplanYScale(mstPoint.getY());
                this.mstPoint = mstPoint;
                if (!isScaleFactorCalculated && (this.scaleXFactor == 0 || this.scaleYFactor == 0)) {
                    // Defining the scaleX and scaleY for the map image
                    setupScaleFactorForFloorplan(new Callback() {
                        @Override
                        public void onSuccess() {
                            callWayfinerAsyncTask();
                        }

                        @Override
                        public void onError() {

                        }
                    });

                    return;
                }

                setStartingPoint(mstPoint);
                if (this.floorplanBluedotView.getAlpha() == 0.0) {
                    this.floorplanBluedotView.setAlpha((float) 1.0);
                }


                float leftMargin = floorImageLeftMargin + (xPos - (this.floorplanBluedotView.getWidth() / 2));
                float topMargin = floorImageTopMargin + (yPos - (this.floorplanBluedotView.getHeight() / 2));

                this.floorplanBluedotView.setX(leftMargin);
                this.floorplanBluedotView.setY(topMargin);

                showXYMstPoint = new MSTPoint(leftMargin, topMargin);

                if (preFVPoint == null)
                    preFVPoint = new MSTPoint(0, 0);

                if (!hasAddedWayfinding)
                    getWayFindingData();


                renderShowPath();
                visibleView("show_path_view");
                visibleView("edgesPointLayout");

//                    if (!preferenceHelper.getBooleanValue(PreferenceConstants.KEY_SHOW_PATHS, false)) {
//                        //hideView("show_path_view");
//                        //hideView("edgesPointLayout");
//                    } else {
//                        visibleView("show_path_view");
//                        visibleView("edgesPointLayout");
//                    }


//                    if (!preferenceHelper.getBooleanValue(PreferenceConstants.KEY_SHOW_BLUEDOT, true)) {
//                        removeViewByTagname("motionLayout");
//                        preHasMotionPoint = null;
//                        this.floorplanBluedotView.setAlpha((float) 0.0);
//                    } else {
//                        /**
//                         * This check for MSTPointType and motion and accordingly set the color for the dot
//                         * Yellow:  MSTPointType.Cloud and isHasMotion
//                         * Blue:    MSTPointType.Cloud and !isHasMotion
//                         * Purple:  MSTPointType.Device
//                         * Gray :   MSTPointType.LastKnown
//                         * **/
//                        this.floorplanBluedotView.setAlpha((float) 1.0);
//                        if (mstPoint.getLocationType().equals(MSTPointType.Cloud)) {
//                            if (mstPoint.isHasMotion()) {
//                                bluedot_flashlight_image.setBackgroundTintList(ContextCompat
//                                        .getColorStateList(mContext, R.color.yellowDot));
//                            } else {
//                                bluedot_flashlight_image.setBackgroundTintList(ContextCompat
//                                        .getColorStateList(mContext, R.color.blueDot));
//                            }
//                        } else if (mstPoint.getLocationType().equals(MSTPointType.Device)) {
//                            bluedot_flashlight_image.setBackgroundTintList(ContextCompat
//                                    .getColorStateList(mContext, R.color.purpleDot));
//                        } else {
//                            bluedot_flashlight_image.setBackgroundTintList(ContextCompat
//                                    .getColorStateList(mContext, R.color.grayDot));
//                        }
//
//
//                        if (preferenceHelper.getIntValue(PreferenceConstants.KEY_BREAD_CRUMB) > 0) {
//                            if (preHasMotionPoint != null) {
//                                addMotionViews(preHasMotionPoint);
//                            }
//                            floorplanBluedotView.bringToFront();
//                            preHasMotionPoint = mstPoint;
//                        } else {
//                            removeViewByTagname("motionLayout");
//                        }
//                    }
                // renderWayfinding();

                if (true && this.hasAddedWayfinding && this.endingPoint != null) {
                    renderWayfinding();
                } else {
                    if (this.isWayfindingAdded) {
                        isWayfindingAdded = false;
                    }

                        removeViewByTagname("wayfindingpath");
                        hideView("snapPathDestinationView");
                        removeViewByTagname("renderNearestBluedot");

                    // renderSnapToPath();
                }

//                    if (preferenceHelper.getBooleanValue(PreferenceConstants.KEY_SHOWXY, true) &&
//                            preferenceHelper.getBooleanValue(PreferenceConstants.KEY_SHOW_BLUEDOT, true)) {
//                        xyValueTextView.setText("" + mstPoint.getX() + ", " + mstPoint.getY());
//                        xyValueTextView.setVisibility(View.VISIBLE);
//                    } else {
//                        xyValueTextView.setVisibility(View.GONE);
//                    }

            } else {
                //loadMapFromSDK();
                loadMap();
            }
        } else {
//                spsLayout.setVisibility(View.GONE);
//                packetIntervalLayout.setVisibility(View.GONE);
//                drInfoLayout.setVisibility(View.GONE);
        }
    }

    // REMOVE THE VIEW FROM PARENT LAYOUT BY TAG NAME
    private void removeViewByTagname(String sTagName) {

        View wayfindingLineView = floorplanLayout.findViewWithTag(sTagName);
        if (wayfindingLineView != null)
            floorplanLayout.removeView(wayfindingLineView);
    }

    // Hide VIEW
    private void hideView(String sTagName) {

        View wayfindingLineView = floorplanLayout.findViewWithTag(sTagName);
        if (wayfindingLineView != null)
            wayfindingLineView.setVisibility(View.GONE);
    }


    private void visibleView(String sTagName) {

        View wayfindingLineView = floorplanLayout.findViewWithTag(sTagName);
        if (wayfindingLineView != null)
            wayfindingLineView.setVisibility(View.VISIBLE);
    }

    private void renderShowPath() {
        // ADD SHOW PATH VIEW
        if (hasAddedWayfinding && mstPoint != null) {

            if (getActivity() != null && this.wayfinder != null) {

                ArrayList<MSTEdges> edgesArrayList = new ArrayList<>();
                edgesArrayList.addAll(this.wayfinder.getEdges());

                if (edgesArrayList != null && edgesArrayList.size() > 0 && (edgesArrayList.get(0).getMstScreenPoint() == null
                        || isNewPath)) {
//                    removeViewByTagname("show_path_view");
//                    removeViewByTagname("edgesPointLayout");

                    addEdges(edgesArrayList);

                    ArrayList<MSTPath> mstPathArrayList = this.wayfinder.getShowPathArrayList();
                    DrawLine drawLine = new DrawLine(getActivity(), mstPathArrayList, null, null,
                            scaleXFactor, scaleYFactor, currentMap, isActualData);
                    drawLine.setTag("show_path_view");
                    RelativeLayout.LayoutParams lineParams = new
                            RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                            RelativeLayout.LayoutParams.MATCH_PARENT);
                    lineParams.topMargin = (int) floorImageTopMargin;
                    lineParams.leftMargin = (int) floorImageLeftMargin;
                    drawLine.setLayoutParams(lineParams);
                    floorplanLayout.addView(drawLine);
                    isNewPath = false;
                }
            }
        }
    }

    private void renderWayfinding() {

        // ADD WAYFINDING PATH VIEW

        if (getActivity() != null && isAsycTaskFinished) {
            this.wayfinder.setStartingPoint(mstPoint);
            isWayfindingAdded = true;
            this.lastEndingPoint = this.endingPoint;
            if (renderWayfindingAsyncTask != null && renderWayfindingAsyncTask.getStatus() != AsyncTask.Status.FINISHED) {
                renderWayfindingAsyncTask.cancel(true);
            }
            isAsycTaskFinished = false;
            renderWayfindingAsyncTask = null;
            renderWayfindingAsyncTask = new RenderWayfindingAsyncTask();
            renderWayfindingAsyncTask.execute();
        }/*else  {
            removeViewByTagname("wayfindingpath");
            hideView("edgesPointLayout");
            hideView("snapPathDestinationView");
            removeViewByTagname("renderNearestBluedot");
        }*/
    }

    private void setStartingPoint(MSTPoint mstPoint) {
        this.startingPoint = mstPoint;
        if (wayfinder != null)
            this.wayfinder.setStartingPoint(this.startingPoint);
    }

    private void setDestinationPoint(MSTPoint mstPoint) {
        this.endingPoint = mstPoint;
        if (wayfinder != null)
            this.wayfinder.setDestinationPoint(this.endingPoint);
    }

    private void addEdges(ArrayList<MSTEdges> edgesArrayList) {

        if (getActivity() == null) {
            return;
        }

        RelativeLayout edges = new RelativeLayout(getActivity());
        edges.setTag("edgesPointLayout");
        RelativeLayout.LayoutParams lineParams = new
                RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        lineParams.topMargin = (int) floorImageTopMargin;
        lineParams.leftMargin = (int) floorImageLeftMargin;
        edges.setLayoutParams(lineParams);

        MSTPoint mstPoint;
        RelativeLayout.LayoutParams params;
        for (MSTEdges mstEdges : edgesArrayList) {
            mstPoint = mstEdges.getMstPoint();

            View view = new View(getActivity());
            params = new RelativeLayout.LayoutParams(12, 12);
            view.setBackgroundColor(Color.parseColor("#5699f6"));
            if (!isActualData) {
                params.leftMargin = (int) ((mstPoint.getX() * scaleXFactor * scaleXFactor * currentMap.getPpm()) - 6);
                params.topMargin = (int) ((mstPoint.getY() * scaleYFactor * currentMap.getPpm()) - 6);
            } else {
                params.leftMargin = (int) ((mstPoint.getX() * scaleXFactor) - 6);
                params.topMargin = (int) ((mstPoint.getY() * scaleYFactor) - 6);
            }
            view.setLayoutParams(params);
            mstEdges.setMstScreenPoint(new MSTPoint(params.leftMargin + floorImageLeftMargin,
                    params.topMargin + floorImageTopMargin));

            edges.addView(view);
        }

        floorplanLayout.addView(edges);
    }

    private class RenderWayfindingAsyncTask extends AsyncTask<String, Void, String> {


        @Override
        protected String doInBackground(String... urls) {

            startingName = wayfinder.getNearestPositionName(startingPoint, currentMap);
            //endingName   = FloorplanFragment.this.wayfinder.getNearestPositionName1(FloorplanFragment.this.endingPoint, FloorplanFragment.this.currentMap, scaleXFactor, scaleYFactor);
            endingName = wayfinder.getNearestPositionName(endingPoint);

            System.out.println("wayfinding startingPoint: " + startingName + "," + "endingPoint: " + endingName);

            pathArr = graph.findPathFrom(startingName, endingName);
            if (_previousPathArr == null || (pathArr.size() != 0 && hasPathChanged(pathArr))) {

                if (!isScaleFactorCalculated && (scaleXFactor == 0 || scaleYFactor == 0)) {
                    setupScaleFactorForFloorplan();
                }

                _previousPathArr = pathArr;

                pathArrayList = wayfinder.drawWayfingPath(pathArr, scaleXFactor, scaleYFactor, currentMap);
//
//                if (preferenceHelper.getBooleanValue(PreferenceConstants.KEY_SNAP_TO_PATH, false)) {
//
//                    float xPos = FloorplanFragment.this.convertCloudPointToFloorplanXScale(mstPoint.getX());
//                    float yPos = FloorplanFragment.this.convertCloudPointToFloorplanYScale(mstPoint.getY());
//
//                    ArrayList<MSTPath> pathList = FloorplanFragment.this.wayfinder.getPathArrayList();
//                    nearestMstPoint = FloorplanFragment.this.wayfinder.getSnapPathPosition(xPos, yPos, pathList, startingName);
//                } else {
//                    nearestMstPoint = null;
//                }
                nearestMstPoint = null;

                closestMstPoint = wayfinder.getNearestPosition(endingName);

            }


            return null;
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {

            if (getActivity() == null)
                return;


            _previousPathArr = pathArr;
            removeViewByTagname("wayfindingpath");
            removeViewByTagname("snapPathDestinationView");
            DrawLine drawLine = new DrawLine(getActivity(),
                    pathArrayList, pathArr, nearestMstPoint, scaleXFactor, scaleYFactor, currentMap, isActualData);
            if (drawLine != null) {
                drawLine.setTag("wayfindingpath");
                RelativeLayout.LayoutParams lineParams = new
                        RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.MATCH_PARENT);
                lineParams.topMargin = (int) floorImageTopMargin;
                lineParams.leftMargin = (int) floorImageLeftMargin;
                drawLine.setLayoutParams(lineParams);
                floorplanLayout.addView(drawLine);
            }

            //setSanptoPath();

            //-----------------------------------------stp2.0------------------------------------------------------------
//            if (closestMstPoint != null) {
//                addSnapPathDestinationPoint(closestMstPoint);
//            }

            floorplanBluedotView.bringToFront();

            isAsycTaskFinished = true;
        }

        private boolean hasPathChanged(ArrayList<String> pathArr) {
            ArrayList<String> copyArr = new ArrayList<>(pathArr);
            Collections.reverse(copyArr);

            if (_previousPathArr.size() != copyArr.size()) {
                return true;
            }

            // Compare each of the value from path array to make sure they're identical.
            // If any of the values match return false.
            for (int i = 0; i < _previousPathArr.size(); i++) {
                String currentNodeNameAtIndex = (String) _previousPathArr.get(i);
                String newNodeNameAtIndex = (String) copyArr.get(i);
                if (!currentNodeNameAtIndex.equals(newNodeNameAtIndex)) {
                    return true;
                }
            }
            return false;
        }
    }


}

