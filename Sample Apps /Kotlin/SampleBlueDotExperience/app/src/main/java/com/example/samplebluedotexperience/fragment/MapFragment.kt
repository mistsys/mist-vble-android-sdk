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
import com.example.samplebluedotexperience.Constants
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

    private val constants = Constants()

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
        activity?.let {
            mainApplication = requireActivity().application
        }
        arguments?.let {
            orgSecret = requireArguments().getString(sdkToken).toString()
        }
        mistSdkManager.getInstance(mainApplication.applicationContext)
    }


    @RequiresApi(Build.VERSION_CODES.S)
    override fun onStart() {
        super.onStart()
        Log.d(TAG,"SampleBlueDot onStart called")
        if(checkPermissionAndStartSDK()){
            if(orgSecret.isNotEmpty() && constants.orgId.isNotEmpty()){
                startSDK(orgSecret,constants.orgId)
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
        activity?.let{
            val builder = AlertDialog.Builder(activity)
            builder.apply {
                setTitle("This app needs bluetooth and location permission")
                setMessage("Please grant bluetooth/location access so this app can detect beacons in the background")
                setPositiveButton(android.R.string.ok, null)
                setOnDismissListener {
                    val permissionToRequest =permissionRequired.filter {
                        checkSelfPermission(requireActivity(),it)!= PackageManager.PERMISSION_GRANTED }.toTypedArray()
                    if(permissionToRequest.isNotEmpty()){
                        requestPermissions(arrayOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN,Manifest.permission.ACCESS_FINE_LOCATION),permissionRequestBluetoothLocation)
                    }
                }
                show()
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        activity?.let {
            when (requestCode) {
                permissionRequestBluetoothLocation ->
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        Log.d(TAG, "fine location permission granted !!")
                        checkIfBluetoothEnabled()
                        if(checkBackgroundLocation()) {
                            // Start the SDK when permissions are provided
                            if(orgSecret.isNotEmpty()) {
                                startSDK(orgSecret, constants.orgId)
                            } else {}
                        } else {}
                    } else {
                        val builder = AlertDialog.Builder(activity)
                        builder.apply {
                            setTitle("Functionality Limited")
                            setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background")
                            setPositiveButton(android.R.string.ok, null)
                            setOnDismissListener{}
                            show()
                        }
                    }
                permissionRequestBackgroundLocation -> {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                        if(orgSecret.isNotEmpty()) {
                            startSDK(orgSecret, constants.orgId)
                        } else {}
                    } else {}
                }
                else -> {}
            }
        }
    }

    private fun checkIfBluetoothEnabled() {
        activity?.let{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && requireActivity().checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return
            }
        }
        val bluetoothAdapter : BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if(!(bluetoothAdapter.isEnabled && bluetoothAdapter.state== BluetoothAdapter.STATE_ON)){
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, requestEnableBluetooth)
        }
    }

    private fun startSDK(orgSecret: String, orgId: String) {
        Log.d(TAG, "SampleBlueDot startSdk called$orgSecret")
        mistSdkManager.apply {
            mistSdkManager.init(orgSecret,this@MapFragment,orgId)
            mistSdkManager.startMistSdk()
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun checkPermissionAndStartSDK() : Boolean {
        val permissionRequired : MutableList<String> = ArrayList()
        activity?.let{
            if(requireActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
                permissionRequired.add(Manifest.permission.ACCESS_FINE_LOCATION)
            }
            // For API 31 we need BLUETOOTH_SCAN permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && requireActivity().checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                permissionRequired.add(Manifest.permission.BLUETOOTH_SCAN)
            }
            // For API 31 we need BLUETOOTH_CONNECT permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && requireActivity().checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                permissionRequired.add(Manifest.permission.BLUETOOTH_CONNECT)
            }
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

    override fun onReceiveEvent(event: MistEvent) {
        when (event) {  // `when` used as a statement, not an expression
            is MistEvent.OnMapUpdate -> {
                onMapUpdated(event.map)
            }
            is MistEvent.OnRelativeLocationUpdate -> {
                onRelativeLocationUpdated(event.point)
            }

            else -> {}
        }
    }

    private fun onRelativeLocationUpdated(relativeLocation: MistPoint?) {
        //Returns updated location of the mobile client (as a point (X, Y) measured in meters from the map origin, i.e., relative X, Y)
        activity?.let{
            requireActivity().runOnUiThread {
                if (addedMap) {
                    relativeLocation?.let{
                        renderBlueDot(relativeLocation)
                    }
                }
            }
        }
    }

    private fun onMapUpdated(map: MistMap?) {
        // Returns update map for the mobile client as a []MSTMap object
        Log.d(TAG, "SampleBlueDot onMapUpdated called")
        floorPlanImageUrl = map!!.url.toString()
        Log.d(TAG, "SampleBlueDot $floorPlanImageUrl")
        // Set the current map
        activity?.let {
            binding.floorplanImage.drawable?.let {
                if (!this.currentMap.id.equals(map.id)) {
                    this.currentMap = map
                    requireActivity().runOnUiThread {
                        renderImage(floorPlanImageUrl)
                    }
                }
            } ?: run {
                this.currentMap = map
                requireActivity().runOnUiThread {
                    renderImage(floorPlanImageUrl)
                }
            }
        }
    }
    fun onError(error: ErrorType, message: String) {
        Log.d(TAG,"SampleBlueDot onError called $message errorType $error")
        requireActivity().runOnUiThread {
            binding.apply {
                floorplanbluedot.visibility = View.GONE
                floorplanImage.visibility = View.GONE
                progressBar.visibility = View.GONE
                txtError.visibility = View.VISIBLE
                txtError.text = message
            }
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
     * from OnMapUpdated Event.
     */

    private fun renderImage(floorPlanImageUrl: String?) {
        Log.d(TAG,"In Picasso")
        addedMap = false
        binding.apply {
            floorplanImage.visibility= View.VISIBLE
            Picasso.with(activity).load(floorPlanImageUrl).networkPolicy(NetworkPolicy.OFFLINE).into(floorplanImage, object :
                Callback {
                override fun onSuccess() {
                    Log.d(TAG, "Image loaded successfully from the cached")
                    addedMap = true
                    floorplanbluedot.visibility = View.VISIBLE
                    progressBar.visibility = View.GONE
                    if (!scaleFactorCalled) {
                        setupScaleFactorForFloorPlan()
                    }
                }

                override fun onError() {
                    Picasso.with(activity).load(floorPlanImageUrl).into(floorplanImage, object : Callback {
                            override fun onSuccess() {
                                floorplanbluedot.visibility = View.VISIBLE
                                progressBar.visibility = View.GONE
                                addedMap = true
                                if (!scaleFactorCalled) {
                                    setupScaleFactorForFloorPlan()
                                }
                                Log.d(TAG,"Image downloaded from server successfully !!")
                            }

                            override fun onError() {
                                progressBar.visibility = View.GONE
                                Log.d(TAG, "Could not download the image from the server")
                            }
                        })
                }
            })
        }


    }



    private fun renderBlueDot(point : MistPoint) {
        binding.apply {
            floorplanImage.visibility = View.VISIBLE
            floorplanbluedot.visibility = View.VISIBLE
            activity?.let{
                requireActivity().runOnUiThread {
                    floorplanImage.drawable?.takeIf { addedMap }?.run  {
                        // When rendering bluedot hiding old error text
                        txtError.visibility = View.GONE
                        val xPos: Float = convertCloudPointToFloorPlanXScale(point.x)
                        val yPos: Float = convertCloudPointToFloorPlanYScale(point.y)
                        // If scaleX and scaleY are not defined, check again
                        if (!scaleFactorCalled && (scaleXFactor == 0.0 || scaleYFactor == 0.0)) {
                            setupScaleFactorForFloorPlan()
                        }
                        val leftMargin: Float = floorImageLeftMargin + (xPos - (floorplanbluedot.width / 2))
                        val topMargin: Float = floorImageTopMargin + (yPos - (floorplanbluedot.height / 2))
                        floorplanbluedot.x = leftMargin
                        floorplanbluedot.y = topMargin
                    }
                }
            }
        }
    }

    private fun setupScaleFactorForFloorPlan(){
        binding.apply {
            val vto: ViewTreeObserver = floorplanImage.viewTreeObserver
            vto.addOnGlobalLayoutListener {
                floorImageLeftMargin= floorplanImage.left.toFloat()
                floorImageTopMargin = floorplanImage.top.toFloat()
                if (floorplanImage.drawable != null) {
                    scaleXFactor = floorplanImage.width / (floorplanImage.drawable.intrinsicWidth).toDouble()
                    scaleYFactor = floorplanImage.height / (floorplanImage.drawable.intrinsicHeight).toDouble()
                    scaleFactorCalled = true
                }
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