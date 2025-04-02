package com.example.samplebluedotindoorlocation.fragment

import android.app.Application
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.samplebluedotindoorlocation.databinding.MapFragmentBinding
import com.example.samplebluedotindoorlocation.initializer.MistSdkManager
import com.example.samplebluedotindoorlocation.Constants
import com.mist.android.ErrorType
import com.mist.android.IndoorLocationCallback
import com.mist.android.MistEvent
import com.mist.android.MistMap
import com.mist.android.MistPoint
import com.squareup.picasso.Callback
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso

class MapFragment : Fragment(), IndoorLocationCallback {

    private var _binding: MapFragmentBinding? = null
    private val binding get() = _binding!!

    private var mistSdkManager = MistSdkManager()
    val TAG = MapFragment::class.java.simpleName

    private val sdkToken: String = "sdk-token"

    private lateinit var mainApplication: Application

    private lateinit var orgSecret: String

    private var floorPlanImageUrl: String = ""

    private var addedMap: Boolean = false

    private var scaleXFactor: Double = 0.0

    private var scaleYFactor: Double = 0.0

    private var scaleFactorCalled: Boolean = false

    private var floorImageLeftMargin: Float = 0.0F

    private var floorImageTopMargin: Float = 0.0F

    private lateinit var currentMap: MistMap

    private val constants = Constants()

    fun newInstance(sdktoken: String): MapFragment {
        val bundle = Bundle()
        bundle.putString(sdkToken, sdktoken)
        val mapFragment = MapFragment()
        mapFragment.arguments = bundle
        return mapFragment
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MapFragmentBinding.inflate(inflater, container, false)
        binding.apply {
            progressBar.visibility = View.VISIBLE
            return (root)
        }
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

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "SampleBlueDot onStart called")
        if (orgSecret.isNotEmpty() && constants.orgId.isNotEmpty()) {
            startSDK(orgSecret, constants.orgId)
        } else {
            Toast.makeText(activity, "Empty Org Secret key or Org Id!", Toast.LENGTH_LONG).show()
        }
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

    private fun startSDK(orgSecret: String?, orgId: String?) {
        Log.d(TAG, "SampleBlueDot startSdk called $orgSecret")
        mainApplication = requireActivity().application
        mistSdkManager.apply {
            init(orgSecret, this@MapFragment, constants.orgId)
            startMistSDK()
        }
    }

    override fun onReceiveEvent(event: MistEvent) {
        when (event) {
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
        activity?.let {
            requireActivity().runOnUiThread {
                if (addedMap) {
                    renderBlueDot(relativeLocation)
                }
            }
        }
    }

    private fun renderBlueDot(point: MistPoint?) {
        binding.apply {
            floorplanImage.visibility = View.VISIBLE
            floorplanBluedot.visibility = View.VISIBLE
            activity?.let {
                requireActivity().runOnUiThread {// change
                    floorplanImage.drawable?.takeIf { addedMap }?.let {
                        point?.let {
                            // When rendering blue dot hiding old error text
                            txtError.visibility = View.GONE
                            val xPos: Float = convertCloudPointToFloorPlanXScale(point.x)
                            val yPos: Float = convertCloudPointToFloorPlanYScale(point.y)
                            // If scaleX and scaleY are not defined, check again
                            if (!scaleFactorCalled && (scaleXFactor == 0.0 || scaleYFactor == 0.0)) {
                                setupScaleFactorForFloorPlan()
                            }
                            val leftMargin: Float =
                                floorImageLeftMargin + (xPos - (floorplanBluedot.width / 2))
                            val topMargin: Float =
                                floorImageTopMargin + (yPos - (floorplanBluedot.height / 2))
                            floorplanBluedot.x = leftMargin
                            floorplanBluedot.y = topMargin
                        }
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

    private fun renderImage(floorPlanImageUrl: String?) {
        Log.d(TAG, "In Picasso")
        addedMap = false
        binding.apply {
            floorplanImage.visibility = View.VISIBLE
            Picasso.with(activity).load(floorPlanImageUrl).networkPolicy(NetworkPolicy.OFFLINE)
                .into(floorplanImage, object : Callback {
                    override fun onSuccess() {
                        Log.d(TAG, "Image loaded successfully from the cached")
                        addedMap = true
                        floorplanBluedot.visibility = View.VISIBLE
                        progressBar.visibility = View.GONE
                        if (!scaleFactorCalled) {
                            setupScaleFactorForFloorPlan()
                        }
                    }

                    override fun onError() {
                        Picasso.with(activity).load(floorPlanImageUrl)
                            .into(floorplanImage, object : Callback {
                                override fun onSuccess() {
                                    floorplanBluedot.visibility = View.VISIBLE
                                    progressBar.visibility = View.GONE
                                    addedMap = true
                                    if (!scaleFactorCalled) {
                                        setupScaleFactorForFloorPlan()
                                    }
                                    Log.d(TAG, "Image downloaded from server successfully !!")
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

//    private fun onError(error: ErrorType, message: String) {
//        Log.d(TAG,"SampleBlueDot onError called" + message + "errorType " + error)
//        requireActivity().runOnUiThread {
//            binding.apply {
//                floorplanBluedot.visibility = View.GONE
//                floorplanImage.visibility = View.GONE
//                progressBar.visibility = View.GONE
//                txtError.visibility = View.VISIBLE
//                txtError.text = message
//            }
//        }
//    }

    private fun setupScaleFactorForFloorPlan() {
        binding.apply {
            val vto: ViewTreeObserver = floorplanImage.viewTreeObserver
            vto.addOnGlobalLayoutListener {
                floorImageLeftMargin = floorplanImage.left.toFloat()
                floorImageTopMargin = floorplanImage.top.toFloat()
                floorplanImage.drawable?.let {
                    scaleXFactor =
                        floorplanImage.width / (floorplanImage.drawable.intrinsicWidth).toDouble()
                    scaleYFactor =
                        floorplanImage.height / (floorplanImage.drawable.intrinsicHeight).toDouble()
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