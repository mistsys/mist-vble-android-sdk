package com.example.samplebluedotindoorlocation.fragment

import android.app.Application
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.fragment.app.Fragment
import com.example.samplebluedotindoorlocation.databinding.MapFragmentBinding
import com.example.samplebluedotindoorlocation.initializer.MistSdkManager
import com.mist.android.ErrorType
import com.mist.android.IndoorLocationCallback
import com.mist.android.MistMap
import com.mist.android.MistPoint
import com.squareup.picasso.Callback
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso

class MapFragment : Fragment(),IndoorLocationCallback {

    private var _binding : MapFragmentBinding? = null
    private val binding get() = _binding!!

    private var mistSdkManager = MistSdkManager()
    val TAG = MapFragment::class.java.simpleName

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = MapFragmentBinding.inflate(inflater,container,false)
        binding.progressBar.visibility=View.VISIBLE
        return(binding.root)
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

    override fun onStart() {
        super.onStart()
        Log.d(TAG,"SampleBlueDot onStart called")
        startSDK(orgSecret)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        mistSdkManager.destroy()
    }

    override fun onStop() {
        super.onStop()
        mistSdkManager.stopMistSDK()
    }

    private fun startSDK(orgSecret: String?) {
        Log.d(TAG, "SampleBlueDot startSdk called $orgSecret")
        mainApplication = requireActivity().application
        if(orgSecret!=null){
            mistSdkManager.init(orgSecret,this,null,mainApplication.applicationContext)
            mistSdkManager.startMistSDK()
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
                    renderBlueDot(relativeLocation)
                }
            }
        }
    }

    private fun renderBlueDot(point: MistPoint?) {
        binding.floorplanImage.visibility = View.VISIBLE
        binding.floorplanBluedot.visibility = View.VISIBLE
        if(activity!=null){
            requireActivity().runOnUiThread {
                if (binding.floorplanImage.drawable != null && point != null && addedMap) {
                    // When rendering blue dot hiding old error text
                    binding.txtError.visibility = View.GONE
                    val xPos: Float = convertCloudPointToFloorPlanXScale(point.x)
                    val yPos: Float = convertCloudPointToFloorPlanYScale(point.y)
                    // If scaleX and scaleY are not defined, check again
                    if (!scaleFactorCalled && (scaleXFactor == 0.0 || scaleYFactor == 0.0)) {
                        setupScaleFactorForFloorPlan()
                    }
                    val leftMargin: Float = floorImageLeftMargin + (xPos - (binding.floorplanBluedot.width / 2))
                    val topMargin: Float = floorImageTopMargin + (yPos - (binding.floorplanBluedot.height / 2))
                    binding.floorplanBluedot.x = leftMargin
                    binding.floorplanBluedot.y = topMargin
                }
            }
        }
    }

    override fun onMapUpdated(map: MistMap?) {
        // Returns update map for the mobile client as a []MSTMap object
        Log.d(TAG, "SampleBlueDot onMapUpdated called")
        floorPlanImageUrl = map!!.url
        Log.d(TAG, "SampleBlueDot $floorPlanImageUrl")
        // Set the current map
        if(activity!=null && (binding.floorplanImage.drawable==null  || !this.currentMap.id.equals(map.id))){
            this.currentMap = map
            requireActivity().runOnUiThread {
                renderImage(floorPlanImageUrl)
            }
        }
    }

    private fun renderImage(floorPlanImageUrl: String?) {
        Log.d(TAG,"In Picasso")
        addedMap = false
        binding.floorplanImage.visibility=View.VISIBLE
        Picasso.with(activity).load(floorPlanImageUrl).networkPolicy(NetworkPolicy.OFFLINE).into(binding.floorplanImage, object : Callback {
            override fun onSuccess() {
                Log.d(TAG, "Image loaded successfully from the cached")
                addedMap = true
                binding.floorplanBluedot.visibility = View.VISIBLE
                binding.progressBar.visibility = View.GONE
                if (!scaleFactorCalled) {
                    setupScaleFactorForFloorPlan()
                }
            }

            override fun onError() {
                Picasso.with(activity).load(floorPlanImageUrl)
                    .into(binding.floorplanImage, object : Callback {
                        override fun onSuccess() {
                            binding.floorplanBluedot.visibility = View.VISIBLE
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

    override fun onError(errorType: ErrorType?, errorMessage: String?) {
        Log.d(TAG,"SampleBlueDot onError called" + errorMessage + "errorType " + errorType)
        binding.floorplanBluedot.visibility = View.GONE
        binding.floorplanImage.visibility = View.GONE
        binding.progressBar.visibility = View.GONE
        binding.txtError.visibility = View.VISIBLE
        binding.txtError.text = errorMessage
    }

    private fun setupScaleFactorForFloorPlan() {
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
        return (y * this.scaleYFactor * currentMap.ppm).toFloat()
    }

    /**
     * Converting the x point from meter's to pixel with the present scaling factor of the map
     * rendered in the imageview
     */
    private fun convertCloudPointToFloorPlanXScale(x: Double): Float {
        return (x * this.scaleXFactor * currentMap.ppm).toFloat()
    }
}