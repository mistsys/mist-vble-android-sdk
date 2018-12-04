package com.mist.sample.indoor_location.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.mist.sample.indoor_location.R;
import com.mist.sample.indoor_location.utils.SharedPrefUtils;
import com.mist.sample.indoor_location.utils.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class AddTokenDialogFragment extends DialogFragment {

    private AlertDialog alertDialog;
    private static final String TOKEN_PREF_KEY_NAME = "sdkToken";
    private static final String TAG = AddTokenDialogFragment.class.getSimpleName();
    public String sdkToken;

    @BindView(R.id.edt_token)
    EditText edtToken;

    private SdkTokenSavedListener sdkTokenSavedListener;
    private Unbinder unbinder;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            sdkTokenSavedListener = (SdkTokenSavedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement SdkTokenSavedListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Activity activity = getActivity();
        if(activity!=null) {
            View view = LayoutInflater.from(activity).inflate(R.layout.token_input_dialog, null);
            unbinder = ButterKnife.bind(this, view);

            //reading the sdk token from shared preference
            sdkToken = SharedPrefUtils.readSdkToken(getActivity(), TOKEN_PREF_KEY_NAME);

            //check for null sdkToken
            if (!Utils.isEmptyString(sdkToken)) {
                edtToken.setText(sdkToken);
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            alertDialog = builder.create();
            alertDialog.setView(view);
            alertDialog.show();
        }
        return alertDialog;
    }

    //saves the sdk token to shared preference and passes it to the parent activity
    @OnClick(R.id.btn_ok)
    public void onOkClicked() {
        sdkToken = edtToken.getText().toString();
        if (!Utils.isEmptyString(sdkToken)) {
            SharedPrefUtils.saveSdkToken(getActivity(), TOKEN_PREF_KEY_NAME, edtToken.getText().toString());
            sdkTokenSavedListener.onSdkTokenSaved(edtToken.getText().toString());
            alertDialog.dismiss();
        }
    }

    @OnClick(R.id.btn_cancel)
    public void onCancelClicked() {
        alertDialog.dismiss();
    }

    public static AddTokenDialogFragment newInstance() {
        return new AddTokenDialogFragment();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onPause() {
        super.onPause();
        alertDialog.dismiss();
    }

    public interface SdkTokenSavedListener {
        void onSdkTokenSaved(String token);
    }
}
