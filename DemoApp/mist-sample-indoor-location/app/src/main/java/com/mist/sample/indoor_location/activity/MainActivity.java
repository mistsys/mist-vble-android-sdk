package com.mist.sample.indoor_location.activity;

import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.mist.sample.indoor_location.R;
import com.mist.sample.indoor_location.fragment.MapFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        setUpMapFragment();
    }

    private void setUpMapFragment() {
        Fragment mapFragment = getSupportFragmentManager().findFragmentByTag(MapFragment.TAG);
        if (mapFragment == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.frame_map, MapFragment.newInstance(), MapFragment.TAG).commit();
        }
    }

}
