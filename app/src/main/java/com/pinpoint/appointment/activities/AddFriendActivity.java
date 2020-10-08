package com.pinpoint.appointment.activities;


import android.content.pm.ActivityInfo;

import androidx.databinding.DataBindingUtil;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.TextView;

import com.hbb20.CountryCodePicker;
import com.pinpoint.appointment.BaseActivity;
import com.pinpoint.appointment.BaseApplication;
import com.pinpoint.appointment.BaseConstants;
import com.pinpoint.appointment.R;
import com.pinpoint.appointment.api.ApiList;
import com.pinpoint.appointment.api.RequestCode;
import com.pinpoint.appointment.customviews.CustomDialog;
import com.pinpoint.appointment.customviews.GenericView;
import com.pinpoint.appointment.databinding.ActivityAddFriendBinding;
import com.pinpoint.appointment.helper.MessageHelper;
import com.pinpoint.appointment.helper.PrefHelper;
import com.pinpoint.appointment.interfaces.ClickEvent;
import com.pinpoint.appointment.interfaces.DataObserver;
import com.pinpoint.appointment.models.FriendDetails;
import com.pinpoint.appointment.utils.Util;
import com.pinpoint.appointment.validator.ValidationClass;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * A simple {@link Fragment} subclass.
 */
public class AddFriendActivity extends BaseActivity implements ClickEvent {


    private AddFriendActivity parent;
    private Bundle bundle;
    ActivityAddFriendBinding addFriendBinding;
    String strName, strPhone;
    private static final int GOOGLE_API_CLIENT_ID = 0;
    private CountryCodePicker ccp;

    public AddFriendActivity() {
        // Required empty public constructor
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addFriendBinding = DataBindingUtil.setContentView(this, R.layout.activity_add_friend);
        if (Util.isTabletDevice(this)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        ImageView imgBack = GenericView.findViewById(this, R.id.iv_imgMenu_Back);
        ImageView imgSettings = GenericView.findViewById(this, R.id.iv_imgSetting);
        TextView tvHeader = GenericView.findViewById(this, R.id.tv_txtTitle);
        tvHeader.setText(MessageHelper.getInstance().getAppMessage(getString(R.string.str_add_friend)));

        ccp = findViewById(R.id.ccp);
        ccp.setDefaultCountryUsingNameCode("US");

        imgBack.setImageResource(R.mipmap.ic_back);
        imgSettings.setVisibility(View.INVISIBLE);
        tvHeader.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_SEMIBOLD));
        addFriendBinding.etEdtPhone.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));
        addFriendBinding.etEdtName.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));
        addFriendBinding.inputLayoutPhone.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));
        addFriendBinding.inputLayoutName.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));
        addFriendBinding.btSignup.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_SEMIBOLD));

//
        addFriendBinding.inputLayoutPhone.setHint(MessageHelper.getInstance().getAppMessage(getString(R.string.str_phone)));
        addFriendBinding.inputLayoutName.setHint(MessageHelper.getInstance().getAppMessage(getString(R.string.str_name)));
        addFriendBinding.btSignup.setText(MessageHelper.getInstance().getAppMessage(getString(R.string.str_submit)));

        parent = this;
        addFriendBinding.etEdtPhone.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    callAPI();

                    return true;
                }
                return false;
            }
        });

       /* if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            addFriendBinding.etEdtPhone.addTextChangedListener(new PhoneNumberFormattingTextWatcher("US"));
        } else {
            addFriendBinding.etEdtPhone.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
        }*/

        Util.setupOutSideTouchHideKeyboard(GenericView.findViewById(this, R.id.parent));

        ccp.setOnCountryChangeListener(new CountryCodePicker.OnCountryChangeListener() {
            @Override
            public void onCountrySelected() {

            }
        });

    }

    private void callAPI() {
        if (checkValidation()) {
//            String phonenumber=addFriendBinding.etEdtPhone.getText().toString().replaceAll("[^\\d]", "");
            String orignalPhoneNumber = addFriendBinding.etEdtPhone.getText().toString();
            String phonenumber = orignalPhoneNumber.replace("(", "");
            phonenumber = phonenumber.replace(")", "");
            phonenumber = phonenumber.replace(" ", "");
            phonenumber = phonenumber.replace("-", "");
            FriendDetails.addFriendsData(AddFriendActivity.this, ccp.getSelectedCountryCodeWithPlus() + phonenumber, addFriendBinding.etEdtName.getText().toString(), 1, dataObserver);
        }
    }

    DataObserver dataObserver = new DataObserver() {
        @Override
        public void OnSuccess(RequestCode requestCode, Object object) {
            switch (requestCode) {
                case addfriend:
                    CustomDialog.getInstance().hide();
                    String response = object.toString();
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String message = jsonObject.getString(ApiList.KEY_MESSAGE);

                        CustomDialog.getInstance().showAlertWithButtonClick(AddFriendActivity.this, message
                                , MessageHelper.getInstance().getAppMessage(getString(R.string.str_app_name))
                                , MessageHelper.getInstance().getAppMessage(getString(R.string.str_ok))
                                ,
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        CustomDialog.getInstance().hide();
                                        BaseConstants.REFRESH_FRIENDS = true;
                                        PrefHelper.setBoolean("isnotification", false);
                                        BaseConstants.SELECT_SENT_FRIEND = true;
                                        finish();
                                    }
                                }, MessageHelper.getInstance().getAppMessage(getString(R.string.str_dismiss))
                                ,
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        CustomDialog.getInstance().hide();
                                    }
                                }, false
                        );


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }

        @Override
        public void OnFailure(RequestCode requestCode, String errorCode, String error) {
            CustomDialog.getInstance().hide();
            switch (requestCode) {
                case addfriend:
                    CustomDialog.getInstance().hide();
                    PrefHelper.setBoolean("isnotification", false);
                    if (error == null || error.equalsIgnoreCase("")) {
                        BaseConstants.REFRESH_FRIENDS = true;
                        finish();
                    } else {
                        CustomDialog.getInstance().showAlert(AddFriendActivity.this, error, errorCode, MessageHelper.getInstance().getAppMessage(getString(R.string.str_ok)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_dismiss)), false);
                    }
                    break;
            }
        }

        @Override
        public void onOtherStatus(RequestCode requestCode, Object object) {

            switch (requestCode) {
                case addfriend:
                    CustomDialog.getInstance().hide();
                    String response = object.toString();
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String message = jsonObject.getString(ApiList.KEY_MESSAGE);
                        CustomDialog.getInstance().showAlert(AddFriendActivity.this, message, MessageHelper.getInstance().getAppMessage(getString(R.string.str_fail_dialog_header)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_ok)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_dismiss)), false);
                        PrefHelper.setBoolean("isnotification", false);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    break;
            }
        }

        @Override
        public void onRetryRequest(RequestCode requestCode) {

        }
    };


    @Override
    public void onClickEvent(View view) {
        switch (view.getId()) {


            case R.id.iv_imgMenu_Back:
                finish();
                break;
            case R.id.bt_signup:
                if (checkValidation()) {
                    callAPI();
                }
                break;
        }

    }

    private boolean checkValidation() {

        strName = addFriendBinding.etEdtName.getText().toString().trim();
        strPhone = addFriendBinding.etEdtPhone.getText().toString().trim();

        if (TextUtils.isEmpty(strName)) {
            CustomDialog.getInstance().showAlert(this, MessageHelper.getInstance().getAppMessage(getString(R.string.str_enter_name)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_validation_dialog_header)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_ok)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_dismiss)), false);
            return false;
        } else if (TextUtils.isEmpty(strPhone)) {
            CustomDialog.getInstance().showAlert(this, MessageHelper.getInstance().getAppMessage(getString(R.string.str_enter_phone)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_validation_dialog_header)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_ok)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_dismiss)), false);
            return false;
        } else if (!ValidationClass.isValidPhoneNumber(strPhone)) {
            CustomDialog.getInstance().showAlert(this, MessageHelper.getInstance().getAppMessage(getString(R.string.str_valid_phone)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_validation_dialog_header)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_ok)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_dismiss)), false);
            return false;
        } else {
            return true;
        }

    }


}
