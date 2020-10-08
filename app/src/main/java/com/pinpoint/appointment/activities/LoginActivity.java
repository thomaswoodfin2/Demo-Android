package com.pinpoint.appointment.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;

import androidx.annotation.NonNull;
import com.google.android.material.textfield.TextInputLayout;

import android.os.Handler;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.auth.api.credentials.Credential;
import com.pinpoint.appointment.BaseApplication;
import com.pinpoint.appointment.BaseConstants;
import com.pinpoint.appointment.R;
import com.pinpoint.appointment.api.ApiList;
import com.pinpoint.appointment.api.RequestCode;
import com.pinpoint.appointment.customviews.CustomDialog;

import com.pinpoint.appointment.customviews.GenericView;
import com.pinpoint.appointment.databinding.ActivityLoginBinding;
import com.pinpoint.appointment.helper.MessageHelper;
import com.pinpoint.appointment.helper.ToastHelper;
import com.pinpoint.appointment.interfaces.DataObserver;
import com.pinpoint.appointment.models.CustomerDetails;
import com.pinpoint.appointment.permissionUtils.PermissionClass;
import com.pinpoint.appointment.service.BackgroundIntentService;
import com.pinpoint.appointment.utils.Constants;
import com.pinpoint.appointment.utils.Util;
import com.pinpoint.appointment.validator.ValidationClass;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LoginActivity extends GlobalActivity {


    CustomerDetails customerDetails;
    String strEmail, strPassword;
    Dialog dialog;
    Button btn_login;
    //    FacebookIntegration facebookIntegration;
    ActivityLoginBinding activityLoginBinding;
    public ImageView iv_back, iv_settings;
    public TextView tv_header, btnSignup;
    boolean isPwdVisible = false;
    private int againLogin = 0;
    boolean isNewPwdVisible = false, isCnfPwdVisible = false;

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        List<String> shouldPermit = new ArrayList<>();

        if (requestCode == PermissionClass.REQUEST_CODE_RUNTIME_PERMISSION) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                for (int i = 0; i < grantResults.length; i++) {
                    //  permissions[i] = Manifest.permission.CAMERA; //for specific permission check
                    grantResults[i] = PackageManager.PERMISSION_DENIED;
                    shouldPermit.add(permissions[i]);
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        FacebookIntegration.getInstance(this).initFacebookSdk();
        activityLoginBinding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        customerDetails = new CustomerDetails();
        if (Util.isTabletDevice(LoginActivity.this)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        BackgroundIntentService.Companion.stopLocationTracking(getApplicationContext());
    }

    private void init() {

        iv_back = (ImageView) findViewById(R.id.iv_imgMenu_Back);
        iv_settings = (ImageView) findViewById(R.id.iv_imgSetting);
        tv_header = (TextView) findViewById(R.id.tv_txtTitle);
        btnSignup = (Button) findViewById(R.id.btnSignup);
        tv_header.setText(MessageHelper.getInstance().getAppMessage(getString(R.string.str_login)));
        tv_header.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_SEMIBOLD));
        activityLoginBinding.edtPassword.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));
        activityLoginBinding.inputLayoutEmail.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));
        activityLoginBinding.inputLayoutPassword.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));
        activityLoginBinding.edtEmail.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));
        activityLoginBinding.btnLogin.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_SEMIBOLD));
        activityLoginBinding.tvForgotPassword.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));
        activityLoginBinding.tvtxtDntHveAcc.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));
        activityLoginBinding.tvtxtSignUp.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));
        btnSignup.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_SEMIBOLD));
//        activityLoginBinding.btnSignUp.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));

        activityLoginBinding.tvForgotPassword.setAllCaps(true);
        activityLoginBinding.tvtxtDntHveAcc.setAllCaps(true);
        iv_back.setVisibility(View.INVISIBLE);
        iv_settings.setVisibility(View.GONE);
        activityLoginBinding.inputLayoutPassword.setHint(MessageHelper.getInstance().getAppMessage(getString(R.string.str_password)));
        activityLoginBinding.inputLayoutEmail.setHint(MessageHelper.getInstance().getAppMessage(getString(R.string.str_email)));
        activityLoginBinding.btnLogin.setText(MessageHelper.getInstance().getAppMessage(getString(R.string.str_login)));
        activityLoginBinding.tvForgotPassword.setText(MessageHelper.getInstance().getAppMessage(getString(R.string.str_forgot_password)));
        activityLoginBinding.tvtxtDntHveAcc.setText(MessageHelper.getInstance().getAppMessage(getString(R.string.str_dnt_have_ac)));
        activityLoginBinding.tvtxtSignUp.setText(MessageHelper.getInstance().getAppMessage(getString(R.string.str_signup)));
        btnSignup.setText(MessageHelper.getInstance().getAppMessage(getString(R.string.str_signup)));

        activityLoginBinding.edtPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
//                    Utils.getInstance().hideKeyboard(mEdtPassword);

                    if (checkValidation()) {

                        callLoginApi();
                    }
                    return true;
                }
                return false;
            }
        });

        Util.setupOutSideTouchHideKeyboard(GenericView.findViewById(this, R.id.parent));
        if (PermissionClass.checkPermission(LoginActivity.this, PermissionClass.REQUEST_CODE_RUNTIME_PERMISSION,
                Arrays.asList(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CALL_PHONE))) {

        }
    }


//    private boolean checkPermissions() {
//
//    }

    private boolean checkValidation() {

        strEmail = activityLoginBinding.edtEmail.getText().toString();
        strPassword = activityLoginBinding.edtPassword.getText().toString();

        if (TextUtils.isEmpty(strEmail)) {
            CustomDialog.getInstance().showAlert(this, MessageHelper.getInstance().getAppMessage(getString(R.string.str_enter_email)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_validation_dialog_header)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_ok)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_dismiss)), false);

            return false;
        } else if (!ValidationClass.isValidEmail(strEmail)) {
            CustomDialog.getInstance().showAlert(this, MessageHelper.getInstance().getAppMessage(getString(R.string.str_valid_email)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_validation_dialog_header)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_ok)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_dismiss)), false);

            return false;
        } else if (TextUtils.isEmpty(strPassword)) {
            CustomDialog.getInstance().showAlert(this, MessageHelper.getInstance().getAppMessage(getString(R.string.str_enter_pwd)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_validation_dialog_header)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_ok)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_dismiss)), false);

            return false;
        } else if (!ValidationClass.checkMinLength(strPassword, Constants.PASSWORD_LENGTH)) {
            CustomDialog.getInstance().showAlert(this, MessageHelper.getInstance().getAppMessage(getString(R.string.str_error_validpwd)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_validation_dialog_header)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_ok)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_dismiss)), false);

            return false;
        } else {
            return true;
        }
    }

    private void callLoginApi() {
        if (Util.checkInternetConnection()) {
            customerDetails.userLogin(this, strEmail, strPassword, againLogin, dataObserver);
        } else {
            CustomDialog.getInstance().showAlert(this, MessageHelper.getInstance().getAppMessage(getString(R.string.str_internet_availability)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_app_name)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_ok)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_dismiss)), false);
        }

    }

    @Override
    public void onClickEvent(View view) {
        // super.onClickEvent(view);

        switch (view.getId()) {
            case R.id.btnLogin:

                if (checkValidation()) {
                    callLoginApi();
                }
                break;

            case R.id.txtYes:
                if (CustomDialog.getInstance().isDialogShowing()) {
                    CustomDialog.getInstance().hide();
                }
//                checkValidation();
                break;
            case R.id.btnFacebookLogin:
//                FacebookIntegration.getInstance(this).loginToFaceBook(this, this);
                break;
            case R.id.tvForgotPassword:
                forgottPasswordDialog(LoginActivity.this, "", MessageHelper.getInstance().getAppMessage(getString(R.string.str_send)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_forgot_password)));
                break;
            case R.id.btnSignup:
            case R.id.tvtxtSignUp:
                Intent ina = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(ina);
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
                break;
            case R.id.tv_firebaseAuth:

                Intent inf = new Intent(LoginActivity.this, FirebasePhoneNumAuthActivity.class);
                startActivity(inf);

                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
                break;
            case R.id.tv_locationtracking:

                Intent inl = new Intent(LoginActivity.this, LocationTracking.class);
                startActivity(inl);

                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
                break;
            default:
                break;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        FacebookIntegration.getInstance(this).callbackManager.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Credential credential = data.getParcelableExtra(Credential.EXTRA_KEY);
                // credential.getId(); <-- E.164 format phone number on 10.2.+ devices
                Log.e("credential", "cc" + credential.getAccountType());
                Log.e("credential2", "cc2" + credential.getFamilyName());
                Log.e("credential3", "cc3" + credential.getName());
                Log.e("credential4", "cc4" + credential.getId());

            }
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    public void forgottPasswordDialog(final Context context, String msg, String buttonText, String header) {

        dialog = new Dialog(context, R.style.DialogTheme);
        @SuppressLint("InflateParams") final View view = LayoutInflater.from(context).inflate(R.layout.dialog_forgott_password, null);
        final EditText edtEmailAddress = (EditText) view.findViewById(R.id.edtEmail);
        final TextInputLayout input_layout_email = (TextInputLayout) view.findViewById(R.id.input_layout_email);

        TextView tv_txtHeader = (TextView) view.findViewById(R.id.tv_txtHeader);
        Button btn_submit = (Button) view.findViewById(R.id.btn_submit);
        Button btn_cancel = (Button) view.findViewById(R.id.btn_cancel);

        tv_txtHeader.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_SEMIBOLD));
        edtEmailAddress.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));
        input_layout_email.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));

        btn_submit.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_SEMIBOLD));
        btn_cancel.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_SEMIBOLD));

        tv_txtHeader.setText(header);
        btn_submit.setText(buttonText);
        btn_cancel.setText(MessageHelper.getInstance().getAppMessage(getString(R.string.str_cancel)));
        input_layout_email.setHint(MessageHelper.getInstance().getAppMessage(getString(R.string.str_email)));
//        mTxtYes.setTag(buttonText);*/
        btn_submit.setOnClickListener(view1 -> {

            if (ValidationClass.isValidEmail(edtEmailAddress.getText().toString())) {
                CustomerDetails.forgottPassword(LoginActivity.this, dataObserver, edtEmailAddress.getText().toString());
            } else {
                ToastHelper.displayCustomToast(MessageHelper.getInstance().getAppMessage(getString(R.string.str_valid_email)));
            }

        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.hide();
            }
        });
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(view);
        try {
            if (dialog != null) {
                if (!dialog.isShowing()) {
                    dialog.show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    DataObserver dataObserver = new DataObserver() {
        @Override
        public void OnSuccess(RequestCode requestCode, Object object) {
            switch (requestCode) {
                case loginCustomer:
                    CustomDialog.getInstance().hide();
//                String response=object.toString();
                    try {

                        Intent i = new Intent(LoginActivity.this, HomeActivity.class);
                        i.putExtra("login", true);
                        startActivity(i);
                        finish();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    break;
                case forgotpassword:
                    CustomDialog.getInstance().hide();
                    try {
                        String response = object.toString();
                        JSONObject mJobjResponse = new JSONObject(response);

                        String status = mJobjResponse.getString(ApiList.KEY_STATUS);
                        String message = mJobjResponse.getString(ApiList.KEY_MESSAGE);
                        ToastHelper.displayCustomToast(message);
                        if (dialog != null && dialog.isShowing())
                            dialog.dismiss();

                        showVerifyOTPDialog();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    break;

                case verifypassword:
                    CustomDialog.getInstance().hide();
                    try {
                        String response = object.toString();
                        JSONObject mJobjResponse = new JSONObject(response);

                        String status = mJobjResponse.getString(ApiList.KEY_STATUS);
                        String message = mJobjResponse.getString(ApiList.KEY_MESSAGE);
                        CustomDialog.getInstance().showAlertWithButtonClick(LoginActivity.this, message, MessageHelper.getInstance().getAppMessage(getString(R.string.str_app_name)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_ok)), new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                CustomDialog.getInstance().hide();
                                dialog.dismiss();
                            }
                        }, MessageHelper.getInstance().getAppMessage(getString(R.string.str_cancel)), new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                CustomDialog.getInstance().hide();
                                dialog.dismiss();
                            }
                        }, false);
//                    }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    break;
            }
        }

        @Override
        public void OnFailure(RequestCode requestCode, String errorCode, String error) {
            switch (requestCode) {
                case loginCustomer:
                    CustomDialog.getInstance().hide();
//                String response=object.toString();
                    try {

                        showAlert(LoginActivity.this, error, MessageHelper.getInstance().getAppMessage(getString(R.string.str_fail_dialog_header)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_ok)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_ok)), false);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    break;

                case forgotpassword:
                    CustomDialog.getInstance().hide();

                    CustomDialog.getInstance().showAlertWithButtonClick(LoginActivity.this, errorCode, error, MessageHelper.getInstance().getAppMessage(getString(R.string.str_ok)), new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            CustomDialog.getInstance().hide();

                        }
                    }, MessageHelper.getInstance().getAppMessage(getString(R.string.str_cancel)), new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            CustomDialog.getInstance().hide();
                        }
                    }, false);
//                    }


                    break;

                case verifypassword:
                    CustomDialog.getInstance().hide();

                    CustomDialog.getInstance().showAlertWithButtonClick(LoginActivity.this, errorCode, error, MessageHelper.getInstance().getAppMessage(getString(R.string.str_ok)), new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            CustomDialog.getInstance().hide();

                        }
                    }, MessageHelper.getInstance().getAppMessage(getString(R.string.str_cancel)), new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            CustomDialog.getInstance().hide();
                        }
                    }, false);
//                    }


                    break;
            }
        }

        @Override
        public void onOtherStatus(RequestCode requestCode, Object object) {
            switch (requestCode) {
                case loginCustomer:
                    String response = (String) object;

                    try {
                        JSONObject mJobjResponse = new JSONObject(response);

                        String status = mJobjResponse.getString(ApiList.KEY_STATUS);
                        String message = mJobjResponse.getString(ApiList.KEY_MESSAGE);

                        CustomDialog.getInstance().showAlertWithButtonClick(LoginActivity.this, message, MessageHelper.getInstance().getAppMessage(getString(R.string.str_app_name)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_ok)), new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                CustomDialog.getInstance().hide();
                                againLogin = 1;
                                callLoginApi();
                            }
                        }, MessageHelper.getInstance().getAppMessage(getString(R.string.str_cancel)), new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                CustomDialog.getInstance().hide();
                            }
                        }, true);
//                    }
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

    private void showVerifyOTPDialog() {
        dialog = new Dialog(LoginActivity.this
                , R.style.DialogTheme);
        final View dialogView = LayoutInflater.from(LoginActivity.this).inflate(R.layout.dialog_verify_otp, null);


        final EditText mEdtPreviousPassword = GenericView.findViewById(dialogView, R.id.et_edtOtp);
        mEdtPreviousPassword.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));


        final TextInputLayout inputOTP = GenericView.findViewById(dialogView, R.id.input_layout_Phone);
        inputOTP.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));
        final TextInputLayout inputNewPassword = GenericView.findViewById(dialogView, R.id.input_layout_new_password);
        inputNewPassword.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));
        final TextInputLayout inputOldPassword = GenericView.findViewById(dialogView, R.id.input_layout_confirm_password);
        inputOldPassword.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));
//        mEdtPreviousPassword.setHint(MessageHelper.getInstance().getAppMessage(getString(R.string.str_previous_password)));
        inputOTP.setHint(MessageHelper.getInstance().getAppMessage(getString(R.string.str_enter_verificationcode)));
        inputNewPassword.setHint(MessageHelper.getInstance().getAppMessage(getString(R.string.str_new_password)));
        inputOldPassword.setHint(MessageHelper.getInstance().getAppMessage(getString(R.string.str_confirm_passowrd)));
        final EditText mEdtNewPassword = GenericView.findViewById(dialogView, R.id.et_newPassword);
        mEdtNewPassword.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));
//        mEdtNewPassword.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstant.OPENSANS_REGULAR));
//        mEdtNewPassword.setHint(MessageHelper.getInstance().getAppMessage(getString(R.string.str_new_password)));

        final EditText mEdtConfirmPassword = GenericView.findViewById(dialogView, R.id.edt_confirmPassword);
        mEdtConfirmPassword.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));
//        mEdtConfirmPassword.setHint(MessageHelper.getInstance().getAppMessage(getString(R.string.str_confirm_password)));

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
                if (TextUtils.isEmpty(mEdtNewPassword.getText().toString())) {
                    CustomDialog.getInstance().showAlert(LoginActivity.this, MessageHelper.getInstance().getAppMessage(getString(R.string.str_enter_new_password)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_validation_dialog_header)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_ok)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_dismiss)), false);
//                    CustomDialog.getInstance().showAlert(LoginActivity.this,Util.getAppKeyValue(LoginActivity.this, R.string.str_enter_new_password),Util.getAppKeyValue(LoginActivity.this, R.string.str_error), Util.getAppKeyValue(LoginActivity.this, R.string.lblOk), Util.getAppKeyValue(LoginActivity.this, R.string.str_dismiss),false);
                    return false;
                } else if (TextUtils.isEmpty(mEdtPreviousPassword.getText().toString())) {
                    CustomDialog.getInstance().showAlert(LoginActivity.this, MessageHelper.getInstance().getAppMessage(getString(R.string.str_enter_otp)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_validation_dialog_header)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_ok)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_dismiss)), false);
//                    CustomDialog.getInstance().showAlert(LoginActivity.this,Util.getAppKeyValue(LoginActivity.this, R.string.str_enter_new_password),Util.getAppKeyValue(LoginActivity.this, R.string.str_error), Util.getAppKeyValue(LoginActivity.this, R.string.lblOk), Util.getAppKeyValue(LoginActivity.this, R.string.str_dismiss),false);
                    return false;
                } else if (!mEdtNewPassword.getText().toString().equalsIgnoreCase(mEdtConfirmPassword.getText().toString())) {
                    CustomDialog.getInstance().showAlert(LoginActivity.this, MessageHelper.getInstance().getAppMessage(getString(R.string.str_confirmpassword_validation)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_validation_dialog_header)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_ok)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_dismiss)), false);
//            CustomDialog.getInstance().showAlert(this, Util.getAppKeyValue(this, R.string.str_confirmpassword_validation), Util.getAppKeyValue(this, R.string.lblOk));
                    return false;
                }
                return true;
            }

            @Override
            public void onClick(View view) {
                if (checkValidation()) {
                    CustomerDetails.verifyPassword(LoginActivity.this, dataObserver, mEdtPreviousPassword.getText().toString(), mEdtNewPassword.getText().toString());
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

    public void showAlert(final Context context, String msg, String header, String positiveButton, String negativeButton, boolean isShowNegative) {

        final Dialog dialog = new Dialog(context, R.style.DialogTheme);
        @SuppressLint("InflateParams") final View view = LayoutInflater.from(context).inflate(R.layout.dialog_alert_new, null);
        TextView txtHeader = view.findViewById(R.id.txtHeader);
        TextView txtMessage = view.findViewById(R.id.tv_txtMessage);
        TextView txtNegative = view.findViewById(R.id.tv_txtNegative);
        Button btn_positive = view.findViewById(R.id.btn_positive);
        txtHeader.setText(header);
        txtMessage.setText(msg);
        btn_positive.setText(positiveButton);
        txtNegative.setText(negativeButton);
        if (isShowNegative) {
            txtNegative.setVisibility(View.VISIBLE);
        }
//        mTxtYes.setTag(buttonText);*/
        btn_positive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        txtNegative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(view);
        try {
            if (dialog != null) {
                if (!dialog.isShowing()) {
                    dialog.show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
