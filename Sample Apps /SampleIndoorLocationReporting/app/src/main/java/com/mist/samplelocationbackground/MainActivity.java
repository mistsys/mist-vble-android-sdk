package com.mist.samplelocationbackground;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import com.mist.samplelocationbackground.initializer.ServiceInitializer;

import java.util.ArrayList;

/**
 * MainActivity This class start the location service.
 */
public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_BLUETOOTH_LOCATION = 1;
    private static final int PERMISSION_REQUEST_BACKGROUND_LOCATION = 2;
    private static final int REQUEST_ENABLE_BLUETOOTH = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (checkPermissions()) {
            /** Start the Location Service once org Secret & all permissions are provided. */
            ServiceInitializer.startLocationService(getApplicationContext());
        }
    }

    /**
     * Checking permissions required for running location sdk.
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
            /** We check for background location only when permissions for fine location is granted */
            if (checkBackgroundLocation()) {
                return true;
            }
        }
        return false;
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
                        ServiceInitializer.startLocationService(getApplicationContext());
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
                    ServiceInitializer.startLocationService(getApplicationContext());
                }
        }
    }
}
