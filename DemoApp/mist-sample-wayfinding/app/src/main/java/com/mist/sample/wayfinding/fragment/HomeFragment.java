package com.mist.sample.wayfinding.fragment;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.snackbar.Snackbar;
import com.github.clans.fab.FloatingActionMenu;
import com.mist.sample.wayfinding.R;
import com.mist.sample.wayfinding.utils.SharedPrefUtils;
import com.mist.sample.wayfinding.utils.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class HomeFragment extends Fragment {

    private static final String TOKEN_PREF_KEY_NAME = "sdkToken";
    public static final String TAG = HomeFragment.class.getSimpleName();
    // you can replace this text with you sdk token
    public static String sdkToken = "";

    @BindView(R.id.token_menu)
    FloatingActionMenu fabTokenMenu;

    private Unbinder unbinder;
    private SdkTokenReceivedListener sdkTokenReceivedListener;

    //returns an instance of the HomeFragment
    public static Fragment newInstance() {
        return new HomeFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home_fragment, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    //checking if the interface is implemented by the parent activity
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            sdkTokenReceivedListener = (SdkTokenReceivedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement SdkTokenReceivedListener");
        }
    }

    @OnClick(R.id.btn_enter)
    public void onClick() {
        sdkToken = TextUtils.isEmpty(SharedPrefUtils.readSdkToken(getActivity(), TOKEN_PREF_KEY_NAME))?sdkToken:SharedPrefUtils.readSdkToken(getActivity(), TOKEN_PREF_KEY_NAME);
        if (Utils.isEmptyString(sdkToken) && getActivity() != null) {
            Snackbar.make(getActivity().findViewById(android.R.id.content), R.string.enter_sdk_token, Snackbar.LENGTH_LONG).show();
        } else if (sdkToken.toUpperCase().charAt(0) == 'P' || sdkToken.toUpperCase().charAt(0) == 'S' || sdkToken.toUpperCase().charAt(0) == 'E' || sdkToken.charAt(0) == 'G' || sdkToken.charAt(0) == 'g' ||  sdkToken.charAt(0) == 'M') {
            sdkTokenReceivedListener.OnSdkTokenReceived(sdkToken);
        } else {
            Toast.makeText(getActivity(), R.string.valid_sdk_token, Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.add_token_button)
    public void onClickAddTokenButton() {
        if (getFragmentManager() != null) {
            AddTokenDialogFragment tokenDialogFragment = AddTokenDialogFragment.newInstance();
            tokenDialogFragment.show(getFragmentManager(), "dialog");
            tokenDialogFragment.setCancelable(false);
        }
        fabTokenMenu.close(true);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    //interface to send the token to the parent activity
    public interface SdkTokenReceivedListener {
        void OnSdkTokenReceived(String sdkToken);
    }
}
