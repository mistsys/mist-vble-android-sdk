package com.mist.sample.background.fragment;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.mist.sample.background.R;
import com.mist.sample.background.util.SharedPrefUtils;
import com.mist.sample.background.util.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class AddTokenDialogFragment extends DialogFragment {

    private AlertDialog.Builder builder;
    private AlertDialog alertDialog;

    private static final String TAG = AddTokenDialogFragment.class.getSimpleName();
    public String sdkToken;

    @BindView(R.id.edt_token)
    EditText edtToken;
    @BindView(R.id.dialog_parent_layout)
    LinearLayout dialogParentLayout;

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
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.token_input_dialog, null);
        unbinder = ButterKnife.bind(this, view);

        //reading the sdk token from shared preference
        sdkToken = SharedPrefUtils.readSdkToken(getActivity(), Utils.TOKEN_PREF_KEY_NAME);

        //check for null sdkToken
        if (!Utils.isEmptyString(sdkToken)) {
            edtToken.setText(sdkToken);
        }

        builder = new AlertDialog.Builder(getActivity());
        alertDialog = builder.create();
        alertDialog.setView(view);
        alertDialog.show();
        return alertDialog;
    }

    //saves the sdk token to shared preference and passes it to the parent activity
    @OnClick(R.id.btn_ok)
    public void onOkClicked() {
        sdkToken = edtToken.getText().toString();
        if (!Utils.isEmptyString(sdkToken)) {
            SharedPrefUtils.saveSdkToken(getActivity(), Utils.TOKEN_PREF_KEY_NAME, edtToken.getText().toString());
            Snackbar.make(getActivity().findViewById(android.R.id.content), R.string.sdk_token_saved, Snackbar.LENGTH_LONG).show();
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

    public interface SdkTokenSavedListener {
        void onSdkTokenSaved(String token);
    }
}
