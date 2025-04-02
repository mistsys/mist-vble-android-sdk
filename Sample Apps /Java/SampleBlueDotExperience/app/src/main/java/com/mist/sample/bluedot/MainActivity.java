package com.mist.sample.bluedot;

import static com.mist.sample.bluedot.Constants.ORG_ID;
import static com.mist.sample.bluedot.Constants.ORG_SECRET;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;

import com.mist.sample.bluedot.databinding.ActivityMainBinding;
import com.mist.sample.bluedot.fragement.MapFragment;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // Load BlueDot Map screen in fragment, permissions are checked inside this fragment.
        setUpMapFragment(ORG_SECRET, ORG_ID);
    }

    /**
     * This method is setting up the Map screen with passing the SDK token needed by it for Mist SDK
     * to start working
     * @param orgSecret sdk token used for enrollment
     */
    private void setUpMapFragment(String orgSecret, String orgId) {
        Fragment mapFragment = getSupportFragmentManager().findFragmentByTag(MapFragment.TAG);
        if (mapFragment == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.frame_fragment, MapFragment.newInstance(orgSecret, orgId), MapFragment.TAG).addToBackStack(MapFragment.TAG).commit();
        }
    }
}