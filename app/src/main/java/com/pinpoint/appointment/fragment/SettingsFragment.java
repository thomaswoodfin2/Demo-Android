package com.pinpoint.appointment.fragment;


import android.app.ActivityManager;
import android.app.Dialog;
import android.content.Intent;

import androidx.databinding.DataBindingUtil;

import android.os.Bundle;

import androidx.annotation.Nullable;

import androidx.fragment.app.Fragment;

import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.orhanobut.hawk.Hawk;
import com.pinpoint.appointment.BaseApplication;
import com.pinpoint.appointment.BaseConstants;
import com.pinpoint.appointment.R;
import com.pinpoint.appointment.activities.ExtendSubscriptionActivity;
import com.pinpoint.appointment.activities.FirebasePhoneNumAuthActivity;
import com.pinpoint.appointment.activities.HomeActivity;
import com.pinpoint.appointment.activities.LoginActivity;
import com.pinpoint.appointment.activities.LogsActivity;
import com.pinpoint.appointment.activities.ViewProfileActivity;
import com.pinpoint.appointment.api.ApiList;
import com.pinpoint.appointment.api.RequestCode;
import com.pinpoint.appointment.customviews.CustomDialog;
import com.pinpoint.appointment.customviews.GenericView;
import com.pinpoint.appointment.databinding.FragmentSettingsBinding;
import com.pinpoint.appointment.helper.MessageHelper;
import com.pinpoint.appointment.helper.PrefHelper;
import com.pinpoint.appointment.helper.ToastHelper;
import com.pinpoint.appointment.interfaces.ClickEvent;
import com.pinpoint.appointment.interfaces.DataObserver;
import com.pinpoint.appointment.models.CustomerDetails;
import com.pinpoint.appointment.models.LoginHelper;
import com.pinpoint.appointment.service.BackgroundIntentService;
import com.pinpoint.appointment.utils.Constants;
import com.pinpoint.appointment.utils.Util;

import org.json.JSONException;
import org.json.JSONObject;

import me.leolin.shortcutbadger.ShortcutBadger;

import static android.content.Context.ACTIVITY_SERVICE;


/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends Fragment implements ClickEvent {

    public static final String VIEW_PAGER_POSITION = "viewPagerPosition";
    private int selectedPosition;
    private HomeActivity parent;
    FragmentSettingsBinding settingsBinding;
    Dialog dialog;
    boolean isPwdVisible = false, isNewPwdVisible = false, isCnfPwdVisible = false;


    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        settingsBinding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_settings, container, false);
        View rootView = settingsBinding.getRoot();

//      parent.tv_header.setAllCaps(true);

        return rootView;

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        parent = (HomeActivity) getActivity();
        parent.iv_back.setVisibility(View.GONE);
        parent.iv_settings.setVisibility(View.GONE);
        parent.tv_header.setText(MessageHelper.getInstance().getAppMessage(getString(R.string.str_settings_header)));

        settingsBinding.tvViewProfile.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_SEMIBOLD));
        settingsBinding.tvChangePassword.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_SEMIBOLD));
        settingsBinding.tvEditPhone.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_SEMIBOLD));
        settingsBinding.tvExtendSubscription.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_SEMIBOLD));
        settingsBinding.tvLogout.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_SEMIBOLD));

        settingsBinding.tvViewProfile.setText(MessageHelper.getInstance().getAppMessage(getString(R.string.str_view_profile)));
        settingsBinding.tvChangePassword.setText(MessageHelper.getInstance().getAppMessage(getString(R.string.str_change_password)));
        settingsBinding.tvEditPhone.setText(MessageHelper.getInstance().getAppMessage(getString(R.string.str_edit_phone)));
        settingsBinding.tvExtendSubscription.setText(MessageHelper.getInstance().getAppMessage(getString(R.string.str_extend_subscription)));
        settingsBinding.tvLogout.setText(MessageHelper.getInstance().getAppMessage(getString(R.string.str_logout)));

        if (Util.isTabletDevice(parent)) {
            settingsBinding.rlEditPhoneNumber.setVisibility(View.GONE);
        } else {
            settingsBinding.rlEditPhoneNumber.setVisibility(View.VISIBLE);
        }
    }


    private void showLogoutDialog() {
        CustomDialog.getInstance().showAlertWithButtonClick(parent, Util.getAppKeyValue(parent, R.string.str_ask_logout), MessageHelper.getInstance().getAppMessage(getString(R.string.str_logout)), Util.getAppKeyValue(parent, R.string.lblOk), new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                CustomDialog.getInstance().hide();
                CustomerDetails.callLogoutUser(parent, dataObserver);


            }
        }, Util.getAppKeyValue(parent, R.string.str_dismiss), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomDialog.getInstance().hide();
            }
        }, true);
//      CustomDialog.getInstance().showAlert(this,Util.getAppKeyValue(this, R.string.str_enter_Name),"Error", Util.getAppKeyValue(this, R.string.lblOk), Util.getAppKeyValue(this, R.string.str_dismiss),false);
    }

    private void showChangePasswordDialog() {
        dialog = new Dialog(parent
                , R.style.DialogTheme);
        dialog.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        final View dialogView = LayoutInflater.from(parent).inflate(R.layout.dialog_change_password, null);
        final EditText mEdtPreviousPassword = GenericView.findViewById(dialogView, R.id.et_previousPassword);
        mEdtPreviousPassword.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));

        final EditText mEdtNewPassword = GenericView.findViewById(dialogView, R.id.et_newPassword);
        mEdtNewPassword.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));

        final EditText mEdtConfirmPassword = GenericView.findViewById(dialogView, R.id.edt_confirmPassword);
        mEdtConfirmPassword.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));

       // final TextInputLayout inputPreviousPassword = GenericView.findViewById(dialogView, R.id.input_layout_previouspass);
      //  inputPreviousPassword.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));
      //  inputPreviousPassword.setHint(MessageHelper.getInstance().getAppMessage(getString(R.string.str_oldpwd)));

      /*  final TextInputLayout inputNewPassword = GenericView.findViewById(dialogView, R.id.input_layout_new_password);
        inputNewPassword.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));
        inputNewPassword.setHint(MessageHelper.getInstance().getAppMessage(getString(R.string.str_new_password)));*/

       /* final TextInputLayout inputConfirmPasword = GenericView.findViewById(dialogView, R.id.input_layout_confirm_password);
        inputConfirmPasword.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));
        inputConfirmPasword.setHint(MessageHelper.getInstance().getAppMessage(getString(R.string.str_confirm_passowrd)));*/

        TextView mTxtHeader = GenericView.findViewById(dialogView, R.id.tv_txtHeader);
        TextView mTxtForgotPassword = GenericView.findViewById(dialogView, R.id.tv_txtForgotPwd);
        mTxtHeader.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_SEMIBOLD));
        mTxtForgotPassword.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));
        mTxtHeader.setText(MessageHelper.getInstance().getAppMessage(getString(R.string.str_change_password)));
        mTxtHeader.setAllCaps(false);


        final Button mTxtSend = GenericView.findViewById(dialogView, R.id.bt_btnButton2);
        mTxtSend.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_SEMIBOLD));
//        mTxtSend.setText(MessageHelper.getInstance().getAppMessage(getString(R.string.str_Submit)));
        mTxtSend.setText(MessageHelper.getInstance().getAppMessage(getString(R.string.str_submit)));
        mTxtSend.setAllCaps(true);

        mTxtSend.setEnabled(true);

        Button mTxtCancel = GenericView.findViewById(dialogView, R.id.bt_btnButton1);
        mTxtCancel.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_SEMIBOLD));
//        mTxtCancel.setText(MessageHelper.getInstance().getAppMessage(getString(R.string.str_cancel)));
        mTxtCancel.setText(MessageHelper.getInstance().getAppMessage(getString(R.string.str_cancel)));
        mTxtCancel.setAllCaps(true);
        final ImageView mImgPreviousPassword = GenericView.findViewById(dialogView, R.id.iv_view_pass);
        final ImageView mImgNewPassword = GenericView.findViewById(dialogView, R.id.iv_imgNewPwd);
        final ImageView mImgConfirmPassword = GenericView.findViewById(dialogView, R.id.iv_imgConfirmPwd);
        ImageView iv_ImgClose = GenericView.findViewById(dialogView, R.id.iv_ImgClose);
        iv_ImgClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        mImgPreviousPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isPwdVisible) {
                    isPwdVisible = false;
                    mEdtPreviousPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    mImgPreviousPassword.setImageResource(R.mipmap.ic_eye);
                } else {
                    isPwdVisible = true;
                    mEdtPreviousPassword.setInputType(InputType.TYPE_CLASS_TEXT);
                    mImgPreviousPassword.setImageResource(R.mipmap.ic_hide);
                }
            }
        });
        mImgNewPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isNewPwdVisible) {
                    isNewPwdVisible = false;
                    mEdtNewPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    mImgNewPassword.setImageResource(R.mipmap.ic_eye);

                } else {
                    isNewPwdVisible = true;
                    mEdtNewPassword.setInputType(InputType.TYPE_CLASS_TEXT);
                    mImgNewPassword.setImageResource(R.mipmap.ic_hide);

                }
            }
        });
        mImgConfirmPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isCnfPwdVisible) {
                    isCnfPwdVisible = false;
                    mEdtConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    mImgConfirmPassword.setImageResource(R.mipmap.ic_eye);
                } else {
                    isCnfPwdVisible = true;
                    mEdtConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT);
                    mImgConfirmPassword.setImageResource(R.mipmap.ic_hide);
                }
            }
        });


        mTxtSend.setOnClickListener(new View.OnClickListener() {
            boolean checkValidation() {
                if (TextUtils.isEmpty(mEdtPreviousPassword.getText().toString())) {
                    CustomDialog.getInstance().showAlert(parent, Util.getAppKeyValue(parent, R.string.str_enter_currentpassword), MessageHelper.getInstance().getAppMessage(getString(R.string.str_validation_dialog_header)), Util.getAppKeyValue(parent, R.string.lblOk), Util.getAppKeyValue(parent, R.string.str_dismiss), false);
//            CustomDialog.getInstance().showAlert(this, Util.getAppKeyValue(this, R.string.str_enter_Pwd), Util.getAppKeyValue(this, R.string.lblOk));
                    return false;
                } else if (TextUtils.isEmpty(mEdtNewPassword.getText().toString())) {
                    CustomDialog.getInstance().showAlert(parent, Util.getAppKeyValue(parent, R.string.str_enter_new_password), MessageHelper.getInstance().getAppMessage(getString(R.string.str_validation_dialog_header)), Util.getAppKeyValue(parent, R.string.lblOk), Util.getAppKeyValue(parent, R.string.str_dismiss), false);
                } else if (!mEdtNewPassword.getText().toString().equalsIgnoreCase(mEdtConfirmPassword.getText().toString())) {
                    CustomDialog.getInstance().showAlert(parent, Util.getAppKeyValue(parent, R.string.str_confirmpassword_validation), MessageHelper.getInstance().getAppMessage(getString(R.string.str_validation_dialog_header)), Util.getAppKeyValue(parent, R.string.lblOk), Util.getAppKeyValue(parent, R.string.str_dismiss), false);
//            CustomDialog.getInstance().showAlert(this, Util.getAppKeyValue(this, R.string.str_confirmpassword_validation), Util.getAppKeyValue(this, R.string.lblOk));
                    return false;
                }
                return true;
            }

            @Override
            public void onClick(View view) {
                if (checkValidation()) {
                    CustomerDetails.updatePassword(parent, dataObserver, mEdtPreviousPassword.getText().toString(), mEdtNewPassword.getText().toString());
//                    changePassword(mEdtPreviousPassword.getText().toString(), mEdtNewPassword.getText().toString(), mEdtConfirmPassword.getText().toString());
//                    dialog.dismiss();
                }
            }


        });

        mTxtCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(dialogView);
        dialog.show();
    }

    private void changePassword(String previousPswd, String newPswd, String confirmPswd) {
        if (validateChangePassword(previousPswd, newPswd, confirmPswd)) {

        }
//            CustomerDetails.getInstance().changePassword(parent,previousPswd,newPswd,this );

    }

    private boolean validateChangePassword(String previousPswd, String newPswd, String confirmPswd) {

        return true;

    }

    @Override
    public void onClickEvent(View view) {
        switch (view.getId()) {
            case R.id.rl_logout:
                showLogoutDialog();
                break;

            case R.id.rl_changePassword:
                showChangePasswordDialog();
                break;

            case R.id.rl_viewProfile:
                parent.isHomeActivity = false;
                Intent ina = new Intent(parent, ViewProfileActivity.class);
                parent.startActivity(ina);

                //showLogsPage();
                break;

            case R.id.rl_editPhoneNumber:
                parent.isHomeActivity = false;
                Intent inc = new Intent(parent, FirebasePhoneNumAuthActivity.class);
                inc.putExtra(BaseConstants.KEY_FROM, "settings");
                parent.startActivity(inc);
                break;

            case R.id.rl_extendSubscription:
                parent.isHomeActivity = false;
                Intent ine = new Intent(parent, ExtendSubscriptionActivity.class);
                ine.putExtra(BaseConstants.KEY_FROM, "settings");

                parent.startActivity(ine);
                break;


        }

    }

    DataObserver dataObserver = new DataObserver() {
        @Override
        public void OnSuccess(RequestCode requestCode, Object object) {
            switch (requestCode) {
                case updatepassword:
                    CustomDialog.getInstance().hide();
                    String response = object.toString();
                    try {
                        JSONObject jsonObject = new JSONObject(response);
//                    JSONObject jsonObject1=jsonObject.getJSONObject(ApiList.KEY_RESPONSE);
                        String message = jsonObject.getString(ApiList.KEY_MESSAGE);
//                    CustomDialog.getInstance().showAlert(parent,message,Util.getAppKeyValue(parent,R.string.str_app_name), Util.getAppKeyValue(ViewProfileActivity.this, R.string.lblOk), Util.getAppKeyValue(ViewProfileActivity.this, R.string.str_dismiss),false);
                        ToastHelper.displayCustomToast(message);
                        dialog.dismiss();
                        String deviceToken = PrefHelper.getString(PrefHelper.KEY_DEVICE_TOKEN, "");
//                    PrefHelper.deleteAllPreferences();
                        LoginHelper.getInstance().logoutUser();
                        PrefHelper.setString(PrefHelper.KEY_DEVICE_TOKEN, deviceToken);

                        CustomDialog.getInstance().hide();
                        BaseConstants.NOTIFICATION_COUNT = 0;
                        ShortcutBadger.applyCount(parent, BaseConstants.NOTIFICATION_COUNT);
                        PrefHelper.deletePreference(BaseConstants.PAYMENT_STATUS);
                        parent.clearNotifications();
                        Intent ina = new Intent(parent, LoginActivity.class);
                        ina.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                        /*
                        try {
                            if (checkServiceRunning()) {
                                parent.stopService(new Intent(parent, LocationUpdateServiceBackground.class));
                            }
//                            parent.stopService(new Intent(parent, StatusService.class));
                        } catch (Exception ex) {
                        }
                        */

                        //BackgroundIntentService.Companion.stopLocationTracking(parent.getApplicationContext());

                        if(getActivity() != null)
                            ((BaseApplication) getActivity().getApplication()).bus().send(Constants.BUS_ACTION_STOP_LOCATION_TRACKING);

                        startActivity(ina);
                        parent.finish();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    break;

                case logoutCustomer:
                    String deviceToken = PrefHelper.getString(PrefHelper.KEY_DEVICE_TOKEN, "");
//                    PrefHelper.deleteAllPreferences();
                    LoginHelper.getInstance().logoutUser();
                    PrefHelper.setString(PrefHelper.KEY_DEVICE_TOKEN, deviceToken);
                    PrefHelper.setInt(BaseConstants.COUNT, 0);
                    CustomDialog.getInstance().hide();
                    BaseConstants.NOTIFICATION_COUNT = 0;
                    ShortcutBadger.applyCount(parent, BaseConstants.NOTIFICATION_COUNT);
                    PrefHelper.deletePreference(BaseConstants.PAYMENT_STATUS);
                    parent.clearNotifications();
                    parent.stopScheduleService();
                    Intent ina = new Intent(parent, LoginActivity.class);
                    ina.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    CustomDialog.getInstance().hide();
                    Hawk.deleteAll();
                    /*
                    try {
                        if (checkServiceRunning()) {
                            parent.stopService(new Intent(parent, LocationUpdateServiceBackground.class));
                        }
//                        parent.stopService(new Intent(parent, StatusService.class));
                    } catch (Exception ex) {
                    }
                    */

                    /* 02-10-2020 - Removed by TBL TODO for status update. Change to a new class
                    try {
                        Intent intent = new Intent(parent, LocationUpdatesService.class);
                        intent.putExtra(ApiList.KEY_STOP, true);
                        parent.startService(intent);
                    } catch (Exception ex) {
                    }
                    */

                    if(getActivity() != null) {
                        ((BaseApplication) getActivity().getApplication()).bus().send(Constants.BUS_ACTION_STOP_LOCATION_TRACKING);
                        ((BaseApplication)  getActivity().getApplication()).bus().send(Constants.BUS_ACTION_STOP_STATUS_UPDATE);
                    }

                    startActivity(ina);
                    parent.finish();
                    break;
            }
        }

        public boolean checkServiceRunning() {
            ActivityManager manager = (ActivityManager) parent.getSystemService(ACTIVITY_SERVICE);
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if ("com.pinpoint.appointment.location.LocationUpdateServiceBackground"
                        .equals(service.service.getClassName())) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public void OnFailure(RequestCode requestCode, String errorCode, String error) {
            CustomDialog.getInstance().hide();
            if (requestCode == RequestCode.updatepassword) {
                CustomDialog.getInstance().hide();
                CustomDialog.getInstance().showAlert(parent, errorCode, error, Util.getAppKeyValue(parent, R.string.lblOk), Util.getAppKeyValue(parent, R.string.str_dismiss), false);
            }
        }

        @Override
        public void onOtherStatus(RequestCode requestCode, Object object) {

            switch (requestCode) {
                case updatepassword:
                    CustomDialog.getInstance().hide();
                    String response = object.toString();
                    try {
                        JSONObject jsonObject = new JSONObject(response);
//                    JSONObject jsonObject1=jsonObject.getJSONObject(ApiList.KEY_RESPONSE);
                        String message = jsonObject.getString(ApiList.KEY_MESSAGE);

                        CustomDialog.getInstance().showAlert(parent, message, MessageHelper.getInstance().getAppMessage(getString(R.string.str_fail_dialog_header)), Util.getAppKeyValue(parent, R.string.lblOk), Util.getAppKeyValue(parent, R.string.str_dismiss), false);

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


    //show logs
    private void showLogsPage() {
        Intent ina = new Intent(parent, LogsActivity.class);
        parent.startActivity(ina);
    }
}
