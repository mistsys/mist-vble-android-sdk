package com.example.samplebluedotexperience.fragment

import android.Manifest
import android.app.AlertDialog
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.fragment.app.Fragment
import com.example.samplebluedotexperience.databinding.MapFragmentBinding
import com.example.samplebluedotexperience.initializer.MistSdkManager
import com.mist.android.ErrorType
import com.mist.android.IndoorLocationCallback
import com.mist.android.MistEvent
import com.mist.android.MistMap
import com.mist.android.MistPoint
import com.squareup.picasso.Callback
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso

class MapFragment : Fragment(), IndoorLocationCallback {

    private var _binding : MapFragmentBinding?=null
    private val binding get() = _binding!!

    private var mistSdkManager = MistSdkManager()

    val TAG = MapFragment::class.java.simpleName

    private var permissionRequestBluetoothLocation : Int = 1

    private val permissionRequestBackgroundLocation : Int = 2

    private val requestEnableBluetooth : Int = 1

    private val sdkToken : String = "sdk-token"

    private lateinit var mainApplication : Application

    private lateinit var orgSecret : String

    private var floorPlanImageUrl : String = ""

    private var addedMap : Boolean = false

    private var scaleXFactor: Double = 0.0

    private var scaleYFactor : Double = 0.0

    private var scaleFactorCalled : Boolean = false

    private var floorImageLeftMargin : Float = 0.0F

    private var floorImageTopMargin : Float = 0.0F

    private lateinit var currentMap : MistMap

    fun newInstance(sdktoken: String): MapFragment {
        val bundle = Bundle()
        bundle.putString(sdkToken, sdktoken)
        val mapFragment = MapFragment()
        mapFragment.arguments = bundle
        return mapFragment
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = MapFragmentBinding.inflate(inflater,container,false)
        binding.progressBar.visibility= View.VISIBLE
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (activity != null) {
            mainApplication = requireActivity().application
        }
        if (arguments != null) {
            orgSecret = requireArguments().getString(sdkToken).toString()
        }
        mistSdkManager.getInstance(mainApplication.applicationContext)
    }


    @RequiresApi(Build.VERSION_CODES.S)
    override fun onStart() {
        super.onStart()
        Log.d(TAG,"SampleBlueDot onStart called")
        if(checkPermissionAndStartSDK()){
            if(!orgSecret.isEmpty()){
                startSDK(orgSecret)
            }
            else{
                Toast.makeText(activity,"Empty Org Secret key!", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG,"SampleBlueDot onDestroy called")
        _binding = null
        mistSdkManager.destroyMistSdk()
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG,"SampleBlueDot onStop called")
        mistSdkManager.stopMistSdk()
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun showLocationBluetoothPermissionDialog(permissionRequired: MutableList<String>) {
        if(activity!=null){
            val builder = AlertDialog.Builder(activity)
            builder.setTitle("This app needs bluetooth and location permission")
            builder.setMessage("Please grant bluetooth/location access so this app can detect beacons in the background")
            builder.setPositiveButton(android.R.string.ok, null)
            builder.setOnDismissListener {
                val permissionToRequest =permissionRequired.filter {
                    checkSelfPermission(requireActivity(),it)!= PackageManager.PERMISSION_GRANTED }.toTypedArray()
                if(permissionToRequest.isNotEmpty()){
                    requestPermissions(arrayOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN,Manifest.permission.ACCESS_FINE_LOCATION),permissionRequestBluetoothLocation)
                }
            }
            builder.show()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (activity != null) {
            when (requestCode) {
                permissionRequestBluetoothLocation ->
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        Log.d(TAG, "fine location permission granted !!")
                        checkIfBluetoothEnabled()
                        if(checkBackgroundLocation()) {
                            // Start the SDK when permissions are provided
                            if(!orgSecret.isEmpty()) {
                                startSDK(orgSecret)
                            }
                        }
                    } else {
                        val builder = AlertDialog.Builder(activity)
                        builder.setTitle("Functionality Limited")
                        builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background")
                        builder.setPositiveButton(android.R.string.ok, null)
                        builder.setOnDismissListener{}
                        builder.show()
                    }

                permissionRequestBackgroundLocation -> {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                        if(!orgSecret.isEmpty()) {
                            startSDK(orgSecret)
                        }
                    }
                }
            }
        }
    }

    private fun checkIfBluetoothEnabled() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && activity != null && requireActivity().checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        val bluetoothAdapter : BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if(!(bluetoothAdapter.isEnabled && bluetoothAdapter.state== BluetoothAdapter.STATE_ON)){
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, requestEnableBluetooth)
        }
    }

    private fun startSDK(orgSecret: String) {
        Log.d(TAG, "SampleBlueDot startSdk called$orgSecret")
        mistSdkManager.init(orgSecret,this,null)
        mistSdkManager.startMistSdk()
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun checkPermissionAndStartSDK() : Boolean {
        val permissionRequired : MutableList<String> = ArrayList()
        if(activity!=null && requireActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            permissionRequired.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        // For API 31 we need BLUETOOTH_SCAN permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && activity != null && requireActivity().checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            permissionRequired.add(Manifest.permission.BLUETOOTH_SCAN)
        }
        // For API 31 we need BLUETOOTH_CONNECT permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && activity != null && requireActivity().checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            permissionRequired.add(Manifest.permission.BLUETOOTH_CONNECT)
        }
        if(permissionRequired.size > 0){
            showLocationBluetoothPermissionDialog(permissionRequired)
        }
        else{
            checkIfBluetoothEnabled()
            if(checkBackgroundLocation()){
                return true
            }
        }
        return false
    }

    private fun checkBackgroundLocation(): Boolean {
        if(requireActivity().checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            requestPermissions(arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION), permissionRequestBackgroundLocation)
            return false
        }
        else{
            return true
        }
    }

    /** Implementation of Mist Location Sdk callback methods.
     * onRelativeLocationUpdated
     * onMapUpdated
     * onError
     * didRangeVirtualBeacon
     * onVirtualBeaconListUpdated
     */

    override fun onRelativeLocationUpdated(relativeLocation: MistPoint?) {
        //Returns updated location of the mobile client (as a point (X, Y) measured in meters from the map origin, i.e., relative X, Y)
        if(activity !=null){
            requireActivity().runOnUiThread {
                if (addedMap) {
                    if (relativeLocation != null) {
                        renderBlueDot(relativeLocation)
                    }
                }
            }
        }
    }

    override fun onMapUpdated(map: MistMap?) {
        // Returns update map for the mobile client as a []MSTMap object
        Log.d(TAG, "SampleBlueDot onMapUpdated called")
        floorPlanImageUrl = map!!.url.toString()
        Log.d(TAG, "SampleBlueDot $floorPlanImageUrl")
        // Set the current map
        if(activity!=null && (binding.floorplanImage.drawable==null || !this.currentMap.id.equals(map.id))){
            this.currentMap = map
            requireActivity().runOnUiThread {
                renderImage(floorPlanImageUrl)
            }
        }
    }
    override fun onError(error: ErrorType, message: String) {
        Log.d(TAG,"SampleBlueDot onError called $message errorType $error")
        requireActivity().runOnUiThread {
            binding.floorplanbluedot.visibility = View.GONE
            binding.floorplanImage.visibility = View.GONE
            binding.progressBar.visibility = View.GONE
            binding.txtError.visibility = View.VISIBLE
            binding.txtError.text = message
        }
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

    private fun renderImage(floorPlanImageUrl: String?) {
        Log.d(TAG,"In Picasso")
        addedMap = false
        binding.floorplanImage.visibility= View.VISIBLE
        Picasso.with(activity).load(floorPlanImageUrl).networkPolicy(NetworkPolicy.OFFLINE).into(binding.floorplanImage, object :
            Callback {
            override fun onSuccess() {
                Log.d(TAG, "Image loaded successfully from the cached")
                addedMap = true
                binding.floorplanbluedot.visibility = View.VISIBLE
                binding.progressBar.visibility = View.GONE
                if (!scaleFactorCalled) {
                    setupScaleFactorForFloorPlan()
                }
            }

            override fun onError() {
                Picasso.with(activity).load(floorPlanImageUrl)
                    .into(binding.floorplanImage, object : Callback {
                        override fun onSuccess() {
                            binding.floorplanbluedot.visibility = View.VISIBLE
                            binding.progressBar.visibility = View.GONE
                            addedMap = true
                            if (!scaleFactorCalled) {
                                setupScaleFactorForFloorPlan()
                            }
                            Log.d(TAG,"Image downloaded from server successfully !!")
                        }

                        override fun onError() {
                            binding.progressBar.visibility = View.GONE
                            Log.d(TAG, "Could not download the image from the server")
                        }
                    })
            }
        })

    }

    private fun renderBlueDot(point : MistPoint) {
        binding.floorplanImage.visibility = View.VISIBLE
        binding.floorplanbluedot.visibility = View.VISIBLE
        if(activity!=null){
            requireActivity().runOnUiThread {
                if (binding.floorplanImage.drawable != null && addedMap) {
                    // When rendering bluedot hiding old error text
                    binding.txtError.visibility = View.GONE
                    val xPos: Float = convertCloudPointToFloorPlanXScale(point.x)
                    val yPos: Float = convertCloudPointToFloorPlanYScale(point.y)
                    // If scaleX and scaleY are not defined, check again
                    if (!scaleFactorCalled && (scaleXFactor == 0.0 || scaleYFactor == 0.0)) {
                        setupScaleFactorForFloorPlan()
                    }
                    val leftMargin: Float = floorImageLeftMargin + (xPos - (binding.floorplanbluedot.width / 2))
                    val topMargin: Float = floorImageTopMargin + (yPos - (binding.floorplanbluedot.height / 2))
                    binding.floorplanbluedot.x = leftMargin
                    binding.floorplanbluedot.y = topMargin
                }
            }
        }
    }

    private fun setupScaleFactorForFloorPlan(){
        val vto: ViewTreeObserver = binding.floorplanImage.viewTreeObserver
        vto.addOnGlobalLayoutListener {
            floorImageLeftMargin= binding.floorplanImage.left.toFloat()
            floorImageTopMargin = binding.floorplanImage.top.toFloat()
            if (binding.floorplanImage.drawable != null) {
                scaleXFactor = binding.floorplanImage.width / (binding.floorplanImage.drawable.intrinsicWidth).toDouble()
                scaleYFactor = binding.floorplanImage.height / (binding.floorplanImage.drawable.intrinsicHeight).toDouble()
                scaleFactorCalled = true
            }
        }
    }

    /**
     * converting the y point from meter's to pixel with the present scaling factor of the map
     * rendered in the imageview
     */
    private fun convertCloudPointToFloorPlanYScale(y: Double): Float {
        return (y * this.scaleYFactor * currentMap.ppm!!).toFloat()
    }
    /**
     * Converting the x point from meter's to pixel with the present scaling factor of the map
     * rendered in the imageview
     */
    private fun convertCloudPointToFloorPlanXScale(x: Double): Float {
        return (x * this.scaleXFactor * currentMap.ppm!!).toFloat()
    }
}