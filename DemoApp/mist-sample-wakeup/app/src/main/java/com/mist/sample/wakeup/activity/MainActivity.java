package com.mist.sample.wakeup.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import com.mist.sample.wakeup.R;
import com.mist.sample.wakeup.fragment.AddTokenDialogFragment;
import com.mist.sample.wakeup.fragment.HomeFragment;
import com.mist.sample.wakeup.fragment.MapFragment;
import com.mist.sample.wakeup.utils.SharedPrefUtils;

public class MainActivity extends AppCompatActivity implements HomeFragment.SdkTokenReceivedListener,
        AddTokenDialogFragment.SdkTokenSavedListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setTitle(R.string.app_title_bar_name);

        //setting up the home fragment
        setUpHomeFragment();
        SharedPrefUtils.setIsAppAlive(this, true);
    }

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

    //map fragment is called after the sdk token is received
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
        //take any action if needed
    }

    @Override
    public void OnSdkTokenReceived(String sdkToken) {
        setUpMapFragment(sdkToken);
    }

    @Override
    protected void onDestroy() {
        SharedPrefUtils.setIsAppAlive(this, false);
        SharedPrefUtils.setIsWelcomeShown(this, false);
        super.onDestroy();
    }
}
