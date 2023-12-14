package com.mist.sample.samplelocationbackgroundandbluedot.fragment;

import android.app.Application;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.mist.android.IndoorLocationCallback;
import com.mist.android.ErrorType;
import com.mist.android.MistMap;
import com.mist.android.MistPoint;
import com.mist.sample.samplelocationbackgroundandbluedot.R;
import com.mist.sample.samplelocationbackgroundandbluedot.initializer.MistSdkManager;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MapFragment extends Fragment implements IndoorLocationCallback {
    private static MistSdkManager mistSdkManager;
    public static final String TAG = MapFragment.class.getSimpleName();
    private static final String SDK_TOKEN = "sdkToken";
    private static Application mainApplication; /* For Map rendering*/
    private String orgSecret;
    private String floorPlanImageUrl = "";
    private boolean addedMap = false;
    private double scaleXFactor;
    private double scaleYFactor;
    private boolean scaleFactorCalled;
    private float floorImageLeftMargin;
    private float floorImageTopMargin; /*Stores the map information returned from mist SDK*/
    public MistMap currentMap;
    private Unbinder unbinder;
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

    /**
     * Implementation of fragment lifecycle methods.
     * https://developer.android.com/guide/fragments/lifecycle
     *
     * onCreateView
     * onViewCreated
     * onStart
     * onDestroyView
     * onStop
     *
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.map_fragment, container, false);
        unbinder = ButterKnife.bind(this, view);
        progressBar.setVisibility(View.VISIBLE);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getActivity() != null) {
            mainApplication = getActivity().getApplication();
        }
        if (getArguments() != null) {
            orgSecret = getArguments().getString(SDK_TOKEN);
        }
        mistSdkManager = MistSdkManager.getInstance(mainApplication.getApplicationContext());
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "SampleBlueDot onStart called");
        startSDK(orgSecret);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        mistSdkManager.destroy();
    }

    @Override
    public void onStop() {
        super.onStop();
        mistSdkManager.stopMistSDK();
    }

    private void startSDK(String orgSecret) {
        Log.d(TAG, "SampleBlueDot startSdk called" + orgSecret);
        if (orgSecret != null) {
            mistSdkManager.init(orgSecret, this, null);
            mistSdkManager.startMistSDK();
        }
    }
    /**
     * Implementation of Mist Location Sdk callback methods.
     * onRelativeLocationUpdated
     * onMapUpdated
     * onError
     * didRangeVirtualBeacon
     * onVirtualBeaconListUpdated
     */

    /**
     * We need to implement this method as per our business logic. These methods will be called for
     * IndoorLocationCallback @param relativeLocation
     */

    @Override
    public void onRelativeLocationUpdated(MistPoint relativeLocation) {
        /** Returns updated location of the mobile client (as a point (X, Y) measured in meters from the map origin, i.e., relative X, Y) */
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (currentMap != null && addedMap) {
                        renderBlueDot(relativeLocation);
                    }
                }
            });
        }
    }

    /**
     * Returns update map for the mobile client as a {@link}MSTMap object
     */
    @Override
    public void onMapUpdated(MistMap map) {
        Log.d(TAG, "SampleBlueDot onMapUpdated called");
        floorPlanImageUrl = map.getUrl();
        Log.d(TAG, "SampleBlueDot " + floorPlanImageUrl);
        if (getActivity() != null && (floorPlanImage.getDrawable() == null || this.currentMap == null || !this.currentMap.getId().equals(map.getId()))) {
            /** Set the current map*/
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
     * Notifies the host application about any errors encountered
     */
    @Override
    public void onError(ErrorType errorType, String errorMessage) {
        Log.d(TAG, "SampleBlueDot onError called" + errorMessage + "errorType " + errorType);
        floorplanBluedotView.setVisibility(View.GONE);
        floorPlanImage.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        txtError.setVisibility(View.VISIBLE);
        txtError.setText(errorMessage);
    }

    /**
     * Utility function for rendering bluedot and floor plan
     * renderImage
     * renderBlueDot
     * setupScaleFactorForFloorplan
     * convertCloudPointToFloorplanXScale
     * convertCloudPointToFloorplanYScale
     */

    /**
     * This method is used for rendering the map image using the url from the MSTMap object received
     * from OnMapUpdated callback.
     */
    private void renderImage(final String floorPlanImageUrl) {
        floorPlanImage.setVisibility(View.VISIBLE);
        Log.d(TAG, "in picasso");
        addedMap = false;
        Picasso.with(getActivity()).load(floorPlanImageUrl).networkPolicy(NetworkPolicy.OFFLINE).into(floorPlanImage, new Callback() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Image loaded successfully from the cached");
                addedMap = true;
                floorplanBluedotView.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                if (!scaleFactorCalled) {
                    setupScaleFactorForFloorplan();
                }
            }

            @Override
            public void onError() {
                Picasso.with(getActivity()).load(floorPlanImageUrl).into(floorPlanImage, new Callback() {
                    @Override
                    public void onSuccess() {
                        progressBar.setVisibility(View.GONE);
                        floorplanBluedotView.setVisibility(View.VISIBLE);
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

    /**
     * Rendering bluedot on map using the location provided by location sdk.
     */
    public void renderBlueDot(final MistPoint point) {
        floorPlanImage.setVisibility(View.VISIBLE);
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (floorPlanImage != null && floorPlanImage.getDrawable() != null && point != null && addedMap) {
                        /** When rendering bluedot hiding old error text*/
                        txtError.setVisibility(View.GONE);
                        float xPos = convertCloudPointToFloorplanXScale(point.getX());
                        float yPos = convertCloudPointToFloorplanYScale(point.getY());
                        /** If scaleX and scaleY are not defined, check again*/
                        if (!scaleFactorCalled && (scaleXFactor == 0 || scaleYFactor == 0)) {
                            setupScaleFactorForFloorplan();
                        }
                        float leftMargin = floorImageLeftMargin + (xPos - (floorplanBluedotView.getWidth() / 2));
                        float topMargin = floorImageTopMargin + (yPos - (floorplanBluedotView.getHeight() / 2));
                        floorplanBluedotView.setX(leftMargin);
                        floorplanBluedotView.setY(topMargin);
                    }
                }
            });
        }
    }

    /**
     * Setting floor plan image.
     */
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

    /**
     * Converting the x point from meter's to pixel with the present scaling factor of the map
     * rendered in the imageview
     */
    private float convertCloudPointToFloorplanXScale(double meter) {
        return (float) (meter * this.scaleXFactor * currentMap.getPpm());
    }

    /**
     * converting the y point from meter's to pixel with the present scaling factor of the map
     * rendered in the imageview
     */
    private float convertCloudPointToFloorplanYScale(double meter) {
        return (float) (meter * this.scaleYFactor * currentMap.getPpm());
    }
}
