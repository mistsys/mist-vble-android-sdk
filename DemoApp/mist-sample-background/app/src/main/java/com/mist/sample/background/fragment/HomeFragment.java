package com.mist.sample.background.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.mist.sample.background.R;
import com.mist.sample.background.utils.Utils;

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class HomeFragment extends Fragment {

    public static final String TAG = HomeFragment.class.getSimpleName();

    public static String portalSDKToken = "PHBpcMnTmO4akRTznjkYUTUL2NWy9zAq"; // REPLACE THIS TOKEN
    private HomeFragmentListener listner;

    private Unbinder unbinder;

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
            listner = (HomeFragmentListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement HomeFragmentListener");
        }
    }

    @OnClick(R.id.btn_enter)
    public void onClick() {
        if (!Utils.isValidToken(portalSDKToken) && getActivity() != null) {
            Toast.makeText(getActivity(), R.string.enter_sdk_token, Toast.LENGTH_LONG).show();
            return;
        }
        listner.onSDKTokenSelected(portalSDKToken);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    /**
     * HomeFragmentListener
     */
    public interface HomeFragmentListener {
        void onSDKTokenSelected(String portalSDKToken);
    }
}
