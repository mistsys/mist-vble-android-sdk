package com.example.samplebluedotexperience

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.samplebluedotexperience.databinding.ActivityMainBinding
import com.example.samplebluedotexperience.fragment.MapFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    private val constants = Constants()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Load BlueDot Map screen in fragment, permissions are checked inside this fragment.
        setUPMapFragment(constants.orgSecret)
    }

    
    private fun setUPMapFragment(orgSecret:String) {
        val mapFragment = supportFragmentManager.findFragmentByTag(MapFragment().TAG)
        mapFragment?: run {
            supportFragmentManager.beginTransaction().replace(R.id.frame_fragment, MapFragment().newInstance(orgSecret), MapFragment().TAG).addToBackStack(MapFragment().TAG).commit()
        }
    }
}