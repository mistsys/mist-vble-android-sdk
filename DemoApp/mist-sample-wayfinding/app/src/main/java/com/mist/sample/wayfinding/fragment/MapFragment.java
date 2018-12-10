package com.mist.sample.wayfinding.fragment;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import com.mist.sample.wayfinding.R;
import com.mist.sample.wayfinding.app.MainApplication;
import com.mist.sample.wayfinding.wayfindingpath.DrawLine;
import com.mist.sample.wayfinding.wayfindingpath.MSTEdges;
import com.mist.sample.wayfinding.wayfindingpath.MSTGraph;
import com.mist.sample.wayfinding.wayfindingpath.MSTNode;
import com.mist.sample.wayfinding.wayfindingpath.MSTPath;
import com.mist.sample.wayfinding.wayfindingpath.MSTWayFinder;
import com.mist.sample.wayfinding.utils.MistManager;
import com.mist.sample.wayfinding.utils.Utils;
import com.mist.sample.wayfinding.wayfindingpath.ZoomLayout;
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

public class MapFragment extends Fragment implements MSTCentralManagerIndoorOnlyListener,
        ZoomLayout.ZoomViewTouchListener {

    public static final String TAG = MapFragment.class.getSimpleName();
    private static final int PERMISSION_REQUEST_FINE_LOCATION = 1;
    private static final String SDK_TOKEN = "sdkToken";
    private MainApplication mainApplication;
    private String sdkToken;
    private String floorPlanImageUrl = "";
    private MSTPoint mstPoint = null;
    private boolean addedMap = false;
    private double scaleXFactor;
    private float zoomScaleFactor = 1;
    private double scaleYFactor;
    private float floorImageLeftMargin;
    private float floorImageTopMargin;
    private Unbinder unbinder;
    private MSTMap currentMap;
    private boolean isNewPath = false;
    private boolean scaleFactorCalled = false;
    private int scale;
    private WayfinerAsyncTask wayfinerAsyncTask;
    private boolean hasAddedWayfinding;
    private MSTWayFinder wayfinder;
    private HashMap<String, Object> nodes;
    private MSTGraph graph;
    private boolean isActualData = false;
    private MSTPoint startingPoint;
    private MSTPoint endingPoint;
    private boolean isWayfindingAdded = false;
    private boolean isAsycTaskFinished = true;
    private RenderWayfindingAsyncTask renderWayfindingAsyncTask;
    String startingName, endingName;
    ArrayList<String> pathArr;
    private ArrayList<String> _previousPathArr;
    ArrayList<MSTPath> pathArrayList;
    MSTPoint nearestMstPoint = null, closestMstPoint;
    private View snapPathDestinationView;


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


    public enum AlertType {
        bluetooth,
        network,
        location
    }

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
        zoomLayout.setListener(this);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getActivity() != null)
            mainApplication = (MainApplication) getActivity().getApplication();
        if (getArguments() != null)
            sdkToken = getArguments().getString(SDK_TOKEN);
    }

    @Override
    public void onStart() {
        super.onStart();
        initMISTSDK();
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
        isNewPath = true;
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
                                            setupScaleFactorForFloorplan(new Callback() {
                                                @Override
                                                public void onSuccess() {
                                                    progressBar.setVisibility(View.GONE);
                                                    callWayfinerAsyncTask();
                                                }

                                                @Override
                                                public void onError() {

                                                }
                                            });
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

    /**
     * This is for calling the wayfinder task to draw the way finding path
     */
    private void callWayfinerAsyncTask() {
        if (wayfinerAsyncTask != null && wayfinerAsyncTask.getStatus() != AsyncTask.Status.FINISHED) {
            wayfinerAsyncTask.cancel(true);
        }

        wayfinerAsyncTask = null;
        wayfinerAsyncTask = new WayfinerAsyncTask();
        wayfinerAsyncTask.execute();
    }

    /**
     * This method calculate the scaling factor of the image rendering in image view by taking in account of intrinsic
     * dimension so that we have the factor to multiply while we position the bluedot on the map image
     *
     * @param cb callback to let know about the scaling calculation status
     */
    private void setupScaleFactorForFloorplan(final Callback cb) {
        if (!scaleFactorCalled && (scaleXFactor == 0 || scaleYFactor == 0)) {
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
                        scaleFactorCalled = true;
                    }
                    cb.onSuccess();
                }
            });
        } else {
            cb.onError();
        }
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        //disconnecting the Mist sdk, to make sure there is no prior active instance
        MistManager.newInstance(mainApplication).destroy();
    }

    @Override
    public void onStop() {
        super.onStop();
        //disconnecting the Mist SDK
        MistManager.newInstance(mainApplication).disconnect();
    }

    /**
     * Callback triggered with the coordinates of the touch point
     * @param x
     * @param y
     */
    @Override
    public void onTouchZoomView(float x, float y) {
        drawTouchedDot(x, y);

    }

    /**
     * this method is used to set the destination at the point touched by user
     * @param x
     * @param y
     */
    public void drawTouchedDot(float x, float y) {

        if (this.currentMap != null) {
            setDestinationPoint(new MSTPoint(x, y));
        }
    }

    /**
     * This callback provide us the scaling of the view done so we can translate all the assets like bluedot , paths to appropriate zoom level
     * @param scale
     */
    @Override
    public void onZoomScaleValue(float scale) {
        float scale1 = 1 / scale;

        this.zoomScaleFactor = scale1;

        View view1 = floorplanLayout.findViewById(R.id.floorplan_bluedot);
        View view2 = floorplanLayout.findViewWithTag("renderNearestBluedot");
        View view3 = floorplanLayout.findViewWithTag("wayfindingpath");

        setScaleValue(view1, scale1);
        setScaleValue(view2, scale1);
        setScaleValue(view3, scale1);
        setScaleValue(snapPathDestinationView, scale1);

    }

    private void setScaleValue(View view, float scale) {
        if (view != null && view.getVisibility() == View.VISIBLE) {
            view.setScaleX(scale);
            view.setScaleY(scale);
        }
    }

    //async task to show path lists and draw paths
    private class WayfinerAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            if (!hasAddedWayfinding)
                getWayFindingData();
            if (hasAddedWayfinding && wayfinder != null) {
                wayfinder.getShowPathList(nodes, scaleXFactor, scaleYFactor, currentMap);
                wayfinder.drawShowPath(nodes, scaleXFactor, scaleYFactor, currentMap);
            }
            return null;
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            renderBlueDot(mstPoint);
        }
    }

    //extracting wayfinding path data from current map
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

    //getting the nodes of the wayfinding paths
    private void loadWayfindingData(JSONObject mapJSON) {

        if (this.graph == null) {
            this.graph = new MSTGraph();
        }
        if (this.nodes == null) {
            this.nodes = new HashMap<>();
        }

        try {
            String sCoordinate = mapJSON.optString("coordinate");
            if (!Utils.isEmptyString(sCoordinate) && sCoordinate.equals("actual"))
                isActualData = true;
            else
                isActualData = false;

            JSONArray nodesFromFile = mapJSON.optJSONArray("nodes");

            if (!Utils.isEmpty(nodesFromFile) && nodesFromFile.length() > 0) {

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
    public void renderBlueDot(MSTPoint mstPoint) {

        if (floorPlanImage != null && this.currentMap != null && mstPoint != null) {

            if (this.floorPlanImage.getDrawable() != null) {
                float xPos = this.convertCloudPointToFloorplanXScale(mstPoint.getX());
                float yPos = this.convertCloudPointToFloorplanYScale(mstPoint.getY());
                this.mstPoint = mstPoint;
                if (!scaleFactorCalled && (this.scaleXFactor == 0 || this.scaleYFactor == 0)) {
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

                float leftMargin = floorImageLeftMargin + (xPos - (this.floorplanBluedotView.getWidth() / 2));
                float topMargin = floorImageTopMargin + (yPos - (this.floorplanBluedotView.getHeight() / 2));

                this.floorplanBluedotView.setX(leftMargin);
                this.floorplanBluedotView.setY(topMargin);

                if (!hasAddedWayfinding)
                    getWayFindingData();


                renderShowPath();
                visibleView("show_path_view");
                visibleView("edgesPointLayout");

                if (this.hasAddedWayfinding && this.endingPoint != null) {
                    renderWayfinding();
                } else {
                    if (this.isWayfindingAdded) {
                        isWayfindingAdded = false;
                    }

                    removeViewByTagname("wayfindingpath");
                    hideView("snapPathDestinationView");
                    removeViewByTagname("renderNearestBluedot");

                }

            } else {
                renderImage(floorPlanImageUrl);
            }
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

                if (edgesArrayList.size() > 0 && (edgesArrayList.get(0).getMstScreenPoint() == null
                        || isNewPath)) {

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
            if (renderWayfindingAsyncTask != null && renderWayfindingAsyncTask.getStatus() != AsyncTask.Status.FINISHED) {
                renderWayfindingAsyncTask.cancel(true);
            }
            isAsycTaskFinished = false;
            renderWayfindingAsyncTask = null;
            renderWayfindingAsyncTask = new RenderWayfindingAsyncTask();
            renderWayfindingAsyncTask.execute();
        }
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

    //wayfinding logic
    private class RenderWayfindingAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            startingName = wayfinder.getNearestPositionName(startingPoint, currentMap);
            endingName = wayfinder.getNearestPositionName(endingPoint);

            System.out.println("wayfinding startingPoint: " + startingName + "," + "endingPoint: " + endingName);

            pathArr = graph.findPathFrom(startingName, endingName);
            if (_previousPathArr == null || (pathArr.size() != 0 && hasPathChanged(pathArr))) {

                if (!scaleFactorCalled && (scaleXFactor == 0 || scaleYFactor == 0)) {
                    setupScaleFactorForFloorplan();
                }

                _previousPathArr = pathArr;

                pathArrayList = wayfinder.drawWayfingPath(pathArr, scaleXFactor, scaleYFactor, currentMap);

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

            if (closestMstPoint != null) {
                addSnapPathDestinationPoint(closestMstPoint);
            }

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
                String currentNodeNameAtIndex = _previousPathArr.get(i);
                String newNodeNameAtIndex = copyArr.get(i);
                if (!currentNodeNameAtIndex.equals(newNodeNameAtIndex)) {
                    return true;
                }
            }
            return false;
        }
    }

    //adding the destination point
    private void addSnapPathDestinationPoint(MSTPoint mstPoint) {
        if (mstPoint != null && getActivity() != null) {

            snapPathDestinationView = floorplanLayout.findViewWithTag("snapPathDestinationView");

            if (snapPathDestinationView == null) {
                snapPathDestinationView = new View(getActivity());
                snapPathDestinationView.setTag("snapPathDestinationView");
                snapPathDestinationView.setBackgroundResource(R.drawable.snap_destination_pointer);
                floorplanLayout.addView(snapPathDestinationView);
            }

            snapPathDestinationView.setScaleX(this.zoomScaleFactor);
            snapPathDestinationView.setScaleY(this.zoomScaleFactor);


            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(30, 30);

            if (!isActualData) {
                params.leftMargin = (int) (floorImageLeftMargin + (mstPoint.getX() * scaleXFactor * currentMap.getPpm()) - 15);
                params.topMargin = (int) (floorImageTopMargin + (mstPoint.getY() * scaleYFactor * currentMap.getPpm()) - 15);
            } else {
                params.leftMargin = (int) (floorImageLeftMargin + (mstPoint.getX() * scaleXFactor) - 15);
                params.topMargin = (int) (floorImageTopMargin + (mstPoint.getY() * scaleYFactor) - 15);
            }

            snapPathDestinationView.setLayoutParams(params);
            snapPathDestinationView.setVisibility(View.VISIBLE);
            snapPathDestinationView.invalidate();
        }
    }

}

