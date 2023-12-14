package com.example.samplebluedotindoorlocation

import android.Manifest
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.samplebluedotindoorlocation.databinding.ActivityMainBinding
import com.example.samplebluedotindoorlocation.fragment.MapFragment
import com.example.samplebluedotindoorlocation.initializer.ServiceInitializer

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val constants =Constants()
    private val serviceInitializer = ServiceInitializer()

    private val permissionRequestBluetoothLocation : Int = 1
    private val requestEnableBluetooth : Int = 1
    private val permissionRequestBackgroundLocation : Int = 2

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /** Stopping the location sdk background service,if its running. */
        serviceInitializer.stopLocationService(applicationContext)
        if(checkPermissions()){
            /** If permissions are provide load the Blue dot map fragment. */
            setUpMapFragment(constants.orgSecret)
        }
    }

    override fun onStop() {
        super.onStop()
        Log.d("SampleBlueDot","onStop called")
        /**
         * Starting the location SDK in background when app goes in background.
         */
        serviceInitializer.startLocationService(applicationContext)
    }

    private fun setUpMapFragment(orgSecret: String) {
        val mapFragment : Fragment? = supportFragmentManager.findFragmentByTag(MapFragment().TAG)
        if(mapFragment == null){
            supportFragmentManager.beginTransaction().replace(R.id.frame_fragment,MapFragment().newInstance(orgSecret),MapFragment().TAG).addToBackStack(MapFragment().TAG).commit()
        }
    }

    /**
     * Checking permissions required for running location sdk and blue dot experience.
     */
    @RequiresApi(Build.VERSION_CODES.S)
    private fun checkPermissions(): Boolean {
        val permissionRequired = ArrayList<String>()
        if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            permissionRequired.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        // For API 31 we need BLUETOOTH_SCAN permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            permissionRequired.add(Manifest.permission.BLUETOOTH_SCAN)
        }
        // For API 31 we need BLUETOOTH_CONNECT permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            permissionRequired.add(Manifest.permission.BLUETOOTH_CONNECT)
        }
        if(permissionRequired.size > 0){
            showLocationBluetoothPermissionDialog(permissionRequired)
        }
        else{
            checkIfBluetoothEnabled()
            /**
             * We check for background location only when permissions for fine location is granted
             */
            if (checkBackgroundLocation()) {
                return true
            }
        }
        return false
    }

    /**
     * Creates dialog for asking the permissions @param permissionRequired
     */
    @RequiresApi(Build.VERSION_CODES.S)
    private fun showLocationBluetoothPermissionDialog(permissionRequired: ArrayList<String>) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("This app needs bluetooth and location permission")
        builder.setMessage("Please grant bluetooth/location access so this app can detect beacons in the background")
        builder.setPositiveButton(android.R.string.ok, null)
        builder.setOnDismissListener {
            val permissionToRequest = permissionRequired.filter {
                ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
            }.toTypedArray()
            if (permissionToRequest.isNotEmpty()) {
                requestPermissions(arrayOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.ACCESS_FINE_LOCATION), permissionRequestBluetoothLocation)
            }
        }
        builder.show()
    }

    /**
     * Checking if Bluetooth is enabled or not, it is required for ble scanning.
     */
    private fun checkIfBluetoothEnabled() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (!(bluetoothAdapter != null && bluetoothAdapter.isEnabled && bluetoothAdapter.state!= BluetoothAdapter.STATE_ON)){
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, requestEnableBluetooth)
        }
    }

    /**
     * Check and Request for background location permissions.
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun checkBackgroundLocation(): Boolean {
        return if(checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            requestPermissions(arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION), permissionRequestBackgroundLocation)
            false
        } else{
            true
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            permissionRequestBluetoothLocation -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    checkIfBluetoothEnabled()
                    if(checkBackgroundLocation()){
                        setUpMapFragment(constants.orgSecret)
                    }
                }
                else{
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("Functionality limited!")
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.")
                    builder.show()
                }
            }

            permissionRequestBackgroundLocation -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    setUpMapFragment(constants.orgSecret)
                }
            }
        }
    }

}
