package com.mist.sample.samplelocationbackgroundandbluedot;

import static com.mist.sample.samplelocationbackgroundandbluedot.Constants.ORG_ID;
import static com.mist.sample.samplelocationbackgroundandbluedot.Constants.ORG_SECRET;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.mist.sample.samplelocationbackgroundandbluedot.databinding.ActivityMainBinding;
import com.mist.sample.samplelocationbackgroundandbluedot.fragment.MapFragment;
import com.mist.sample.samplelocationbackgroundandbluedot.initializer.ServiceInitializer;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private static final int PERMISSION_REQUEST_BLUETOOTH_LOCATION = 1;
    private static final int REQUEST_ENABLE_BLUETOOTH = 1;
    private static final int PERMISSION_REQUEST_BACKGROUND_LOCATION = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        /** Stopping the location sdk background service,if its running. */
        ServiceInitializer.stopLocationService(getApplicationContext());
        if (checkPermissions()) {
            /** If permissions are provide load the Bluedot map fragment. */
            setUpMapFragment(ORG_SECRET, ORG_ID);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("SampleBlueDot", "onStop called ");
        /**
         * Starting the location SDK in background when app goes in background.
         */
        ServiceInitializer.startLocationService(getApplicationContext());
    }

    /**
     * This method is setting up the Map screen with passing the SDK token needed by it for Mist SDK
     * to start working @param orgSecret sdk token used for enrollment
     */
    private void setUpMapFragment(String orgSecret, String orgId) {
        Fragment mapFragment = getSupportFragmentManager().findFragmentByTag(MapFragment.TAG);
        if (mapFragment == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.frame_fragment, MapFragment.newInstance(orgSecret, orgId), MapFragment.TAG).addToBackStack(MapFragment.TAG).commit();
        }
    }

    /**
     * Check and Request for background location permissions.
     */
    public boolean checkBackgroundLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, PERMISSION_REQUEST_BACKGROUND_LOCATION);
            return false;
        } else {
            return true;
        }
    }

    /**
     * Checking permissions required for running location sdk and blue dot experience.
     */
    private boolean checkPermissions() {
        ArrayList<String> permissionRequired = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionRequired.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            /* For API 31 we need BLUETOOTH_SCAN permission*/
            permissionRequired.add(Manifest.permission.BLUETOOTH_SCAN);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            /* For API 31 we need BLUETOOTH_CONNECT permission*/
            permissionRequired.add(Manifest.permission.BLUETOOTH_CONNECT);
        }
        if (permissionRequired.size() > 0) {
            showLocationBluetoothPermissionDialog(permissionRequired);
        } else {
            checkIfBluetoothEnabled();
            /**
             * We check for background location only when permissions for fine location is granted
             */
            if (checkBackgroundLocation()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checking if Bluetooth is enabled or not, it is required for ble scanning.
     */
    private void checkIfBluetoothEnabled() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!(bluetoothAdapter != null && bluetoothAdapter.isEnabled() && bluetoothAdapter.getState() == BluetoothAdapter.STATE_ON)) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BLUETOOTH);
        }
    }

    /**
     * Creates dialog for asking the permissions @param permissionRequired
     */
    private void showLocationBluetoothPermissionDialog(ArrayList<String> permissionRequired) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUEST_BLUETOOTH_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkIfBluetoothEnabled();
                    if (checkBackgroundLocation()) {
                        setUpMapFragment(ORG_SECRET, ORG_ID);
                    }
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.").setPositiveButton(android.R.string.ok, null).setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }
                    }).show();
                }
                break;
            case PERMISSION_REQUEST_BACKGROUND_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setUpMapFragment(ORG_SECRET, ORG_ID);
                }
        }
    }
}