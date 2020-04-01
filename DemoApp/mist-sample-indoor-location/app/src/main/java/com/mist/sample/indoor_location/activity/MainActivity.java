package com.mist.sample.indoor_location.activity;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import com.mist.sample.indoor_location.R;
import com.mist.sample.indoor_location.fragment.AddTokenDialogFragment;
import com.mist.sample.indoor_location.fragment.HomeFragment;
import com.mist.sample.indoor_location.fragment.MapFragment;

public class MainActivity extends AppCompatActivity implements HomeFragment.SdkTokenReceivedListener,
        AddTokenDialogFragment.SdkTokenSavedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.app_title_bar_name);
            //setting up the home fragment
            setUpHomeFragment();
        }
    }

    /**
     * This method is just setting up the home fragment
     */
    private void setUpHomeFragment() {
        Fragment tokenFragment = getSupportFragmentManager().
                findFragmentByTag(HomeFragment.TAG);

        if (tokenFragment == null) {
            getSupportFragmentManager().beginTransaction().
                    add(R.id.frame_fragment,
                            HomeFragment.newInstance(), HomeFragment.TAG).
                    addToBackStack(HomeFragment.TAG).
                    commit();
        }
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
            finish();
        } else if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
            getSupportFragmentManager().popBackStack();
            getSupportFragmentManager().getBackStackEntryCount();
        } else {
            super.onBackPressed();
        }
    }


    /**
     * This method is setting up the Map screen with passing the SDK token needed by it for Mist SDK to start working
     *
     * @param sdkToken sdk token used for enrollment
     */
    private void setUpMapFragment(String sdkToken) {
        Fragment mapFragment = getSupportFragmentManager().
                findFragmentByTag(MapFragment.TAG);

        if (mapFragment == null) {
            getSupportFragmentManager().
                    beginTransaction().
                    replace(R.id.frame_fragment,
                            MapFragment.newInstance(sdkToken), MapFragment.TAG).
                    addToBackStack(MapFragment.TAG).
                    commit();
        }
    }

    @Override
    public void onSdkTokenSaved(String token) {
        Snackbar.make(findViewById(android.R.id.content), R.string.sdk_token_saved, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void OnSdkTokenReceived(String sdkToken) {
        setUpMapFragment(sdkToken);
    }

}
