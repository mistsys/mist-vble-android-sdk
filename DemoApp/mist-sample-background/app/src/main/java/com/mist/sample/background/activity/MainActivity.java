package com.mist.sample.background.activity;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import com.mist.sample.background.R;
import com.mist.sample.background.fragment.HomeFragment;
import com.mist.sample.background.fragment.MapFragment;
import com.mist.sample.background.utils.SharedPrefUtils;
import com.mist.sample.background.utils.Utils;

public class MainActivity extends AppCompatActivity implements HomeFragment.HomeFragmentListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.app_title_bar_name);
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
     * This method is settingup the Map scrren with passing the SDK token needed by it for Mist SDK to start working
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
    public void onSDKTokenSelected(String token) {
        SharedPrefUtils.saveSDKToken(this.getApplicationContext(), Utils.TOKEN_PREF_KEY_NAME, token);
        setUpMapFragment(token);
    }

}
