package com.mist.sample.bluedot.fragement;

import android.Manifest;
import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.mist.android.IndoorLocationCallback;
import com.mist.android.ErrorType;
import com.mist.android.MistEvent;
import com.mist.android.MistMap;
import com.mist.android.MistPoint;
import com.mist.sample.bluedot.databinding.MapFragmentBinding;
import com.mist.sample.bluedot.initializer.MistSdkManager;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Objects;

public class MapFragment extends Fragment implements IndoorLocationCallback {
    private MapFragmentBinding binding;
    private static MistSdkManager mistSdkManager;
    public static final String TAG = MapFragment.class.getSimpleName();
    private static final int PERMISSION_REQUEST_BLUETOOTH_LOCATION = 1;
    private static final int REQUEST_ENABLE_BLUETOOTH = 1;
    private static final String SDK_TOKEN = "sdkToken";
    private static final String ORG_ID = "orgId";
    private static Application mainApplication;
    private String orgSecret, orgId;
    // For Map rendering
    private String floorPlanImageUrl = "";
    private boolean addedMap = false;
    private double scaleXFactor;
    private double scaleYFactor;
    private boolean scaleFactorCalled;
    private float floorImageLeftMargin;
    private float floorImageTopMargin;

    // Stores the map information returned from mist SDK
    public MistMap currentMap;

    public static MapFragment newInstance(String sdkToken, String orgId) {
        Bundle bundle = new Bundle();
        bundle.putString(SDK_TOKEN, sdkToken);
        bundle.putString(ORG_ID, orgId);
        MapFragment mapFragment = new MapFragment();
        mapFragment.setArguments(bundle);
        return mapFragment;
    }

    /**
     * Implementation of fragment lifecycle methods.
     * https://developer.android.com/guide/fragments/lifecycle
     * onCreateView
     * onViewCreated
     * onStart
     * onDestroyView
     * onStop
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = MapFragmentBinding.inflate(inflater,container,false);
        View view = binding.getRoot();
        binding.progressBar.setVisibility(View.VISIBLE);
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
            orgId = getArguments().getString(ORG_ID);
        }
        mistSdkManager = MistSdkManager.getInstance(mainApplication.getApplicationContext());
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "SampleBlueDot onStart called");
        checkPermissionAndStartSDK();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        mistSdkManager.destroy();
    }

    @Override
    public void onStop() {
        super.onStop();
        mistSdkManager.stopMistSDK();
    }

    private void startSDK(String orgSecret, String orgId) {
        Log.d(TAG, "SampleBlueDot startSdk called" + orgSecret);
        if (orgSecret != null) {
            mistSdkManager.init(orgSecret, orgId, this);
            mistSdkManager.startMistSDK();
        }
    }

    /**
     * Checking if Bluetooth is enabled or not, it is required for ble scanning.
     */
    private void checkIfBluetoothEnabled() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && getActivity().checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!(bluetoothAdapter != null && bluetoothAdapter.isEnabled() && bluetoothAdapter.getState() == BluetoothAdapter.STATE_ON)) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BLUETOOTH);
        }
    }

    /**
     * Checking permissions required for running location sdk and starting the sdk if permissions
     * are provided
     */
    private void checkPermissionAndStartSDK() {
        ArrayList<String> permissionRequired = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && getActivity() != null && getActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionRequired.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        // For API 31 we need BLUETOOTH_SCAN permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && getActivity() != null && getActivity().checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            permissionRequired.add(Manifest.permission.BLUETOOTH_SCAN);
        }
        // For API 31 we need BLUETOOTH_CONNECT permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && getActivity() != null && getActivity().checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            permissionRequired.add(Manifest.permission.BLUETOOTH_CONNECT);
        }
        if (permissionRequired.size() > 0) {
            showLocationBluetoothPermissionDialog(permissionRequired);
        } else {
            checkIfBluetoothEnabled();
            Log.d(TAG, "SampleBlueDot initMistSDK called");
            if(orgSecret.isEmpty()){
                Log.d(TAG,"Empty Org Secret key!");
                Toast.makeText(getActivity(),"Empty Org Secret key!",Toast.LENGTH_LONG).show();
            }
            else{
                startSDK(orgSecret, orgId);
            }
        }
    }

    /**
     * Creates dialog for asking the permissions @param permissionRequired
     */
    private void showLocationBluetoothPermissionDialog(ArrayList<String> permissionRequired) {
        if (getActivity() != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("This app needs bluetooth and location permission");
            builder.setMessage("Please grant bluetooth/location access so this app can detect beacons in the background.");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    requestPermissions(permissionRequired.stream().toArray(String[]::new), PERMISSION_REQUEST_BLUETOOTH_LOCATION);
                }
            });
            builder.show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (getActivity() != null) {
            if (requestCode == PERMISSION_REQUEST_BLUETOOTH_LOCATION) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "fine location permission granted !!");
                    checkIfBluetoothEnabled();
                    // Start the SDK when permissions are provided
                    if (!orgSecret.isEmpty() && !orgId.isEmpty()) {
                        startSDK(orgSecret, orgId);
                    } else {
                        Toast.makeText(getActivity(), "Empty Org Secret key!", Toast.LENGTH_LONG).show();
                    }
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.").setPositiveButton(android.R.string.ok, null).setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }
                    }).show();
                }
            }
        }
    }

    @Override
    public void onReceiveEvent(@NonNull MistEvent event) {
        if (event instanceof MistEvent.OnRelativeLocationUpdate) {
            onRelativeLocationUpdated(((MistEvent.OnRelativeLocationUpdate) event).getPoint());
        } else if (event instanceof MistEvent.OnMapUpdate) {
            onMapUpdated(((MistEvent.OnMapUpdate) event).getMap());
        } else if (event instanceof MistEvent.OnError) {
            ErrorType errorType = ((MistEvent.OnError) event).getError();
            String errorMessage = errorType.toString();
            onError(errorType, errorMessage);
        } else {
            System.out.println(event);
        }
    }

    /**
     * We need to implement this method as per our business logic. These methods will be called for
     * IndoorLocationCallback @param relativeLocation
     */

    private void onRelativeLocationUpdated(@Nullable MistPoint mistPoint) {
        /**
         * Returns updated location of the mobile client (as a point (X, Y) measured in meters from the map origin, i.e., relative X, Y)
         */
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (currentMap != null && addedMap) {
                        renderBlueDot(mistPoint);
                    }
                }
            });
        }
    }

    private void onMapUpdated(@Nullable MistMap mistMap) {
        /**
         * Returns update map for the mobile client as a {@link}MSTMap object
         */
        Log.d(TAG, "SampleBlueDot onMapUpdated called");
        floorPlanImageUrl = mistMap.getUrl();
        Log.d(TAG, "SampleBlueDot " + floorPlanImageUrl);
        // Set the current map
        if (getActivity() != null && (binding.floorplanImage.getDrawable() == null || this.currentMap == null || !Objects.requireNonNull(this.currentMap.getId()).equals(mistMap.getId()))) {
            this.currentMap = mistMap;
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
    private void onError(@NonNull ErrorType error, @NonNull String message) {
        Log.d(TAG, "SampleBlueDot onError called" + message + "errorType " + error);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                binding.floorplanBluedot.setVisibility(View.GONE);
                binding.floorplanImage.setVisibility(View.GONE);
                binding.progressBar.setVisibility(View.GONE);
                binding.txtError.setVisibility(View.VISIBLE);
                binding.txtError.setText(message);
            }
        });
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
        Log.d(TAG, "in picasso");
        addedMap = false;
        binding.floorplanImage.setVisibility(View.VISIBLE);
        Picasso.with(getActivity()).load(floorPlanImageUrl).networkPolicy(NetworkPolicy.OFFLINE).into(binding.floorplanImage, new Callback() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Image loaded successfully from the cached");
                addedMap = true;
                binding.floorplanBluedot.setVisibility(View.VISIBLE);
                binding.progressBar.setVisibility(View.GONE);
                if (!scaleFactorCalled) {
                    setupScaleFactorForFloorplan();
                }
            }

            @Override
            public void onError() {
                Picasso.with(getActivity()).load(floorPlanImageUrl).into(binding.floorplanImage, new Callback() {
                    @Override
                    public void onSuccess() {
                        binding.floorplanBluedot.setVisibility(View.VISIBLE);
                        binding.progressBar.setVisibility(View.GONE);
                        addedMap = true;
                        if (!scaleFactorCalled) {
                            setupScaleFactorForFloorplan();
                        }
                        Log.d(TAG, "Image downloaded from server successfully !!");
                    }

                    @Override
                    public void onError() {
                        binding.progressBar.setVisibility(View.GONE);
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
        binding.floorplanImage.setVisibility(View.VISIBLE);
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (binding.floorplanImage.getDrawable() != null && point != null && addedMap) {
                        // When rendering bluedot hiding old error text
                        binding.txtError.setVisibility(View.GONE);
                        float xPos = convertCloudPointToFloorplanXScale(point.getX());
                        float yPos = convertCloudPointToFloorplanYScale(point.getY());
                        // If scaleX and scaleY are not defined, check again
                        if (!scaleFactorCalled && (scaleXFactor == 0 || scaleYFactor == 0)) {
                            setupScaleFactorForFloorplan();
                        }
                        float leftMargin = floorImageLeftMargin + (xPos - ((float) binding.floorplanBluedot.getWidth() / 2));
                        float topMargin = floorImageTopMargin + (yPos - ((float) binding.floorplanBluedot.getHeight() / 2));
                        binding.floorplanBluedot.setX(leftMargin);
                        binding.floorplanBluedot.setY(topMargin);
                    }
                }
            });
        }
    }

    /**
     * Setting floor plan image.
     */
    private void setupScaleFactorForFloorplan() {
        ViewTreeObserver vto = binding.floorplanImage.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                floorImageLeftMargin = binding.floorplanImage.getLeft();
                floorImageTopMargin = binding.floorplanImage.getTop();
                if (binding.floorplanImage.getDrawable() != null) {
                    scaleXFactor = (binding.floorplanImage.getWidth() / (double) binding.floorplanImage.getDrawable().getIntrinsicWidth());
                    scaleYFactor = (binding.floorplanImage.getHeight() / (double) binding.floorplanImage.getDrawable().getIntrinsicHeight());
                    scaleFactorCalled = true;
                }
            }
        });
    }

    /**
     * Converting the x point from meter's to pixel with the present scaling factor of the map
     * rendered in the imageview
     */
    private float convertCloudPointToFloorplanXScale(double meter) {
        return (float) (meter * this.scaleXFactor * currentMap.getPpm());
//        return (float) (meter * this.scaleXFactor * currentMap.getPpm());
    }

    /**
     * converting the y point from meter's to pixel with the present scaling factor of the map
     * rendered in the imageview
     */
    private float convertCloudPointToFloorplanYScale(double meter) {
        return (float) (meter * this.scaleYFactor * currentMap.getPpm());
    }
}
