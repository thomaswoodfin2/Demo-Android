package com.pinpoint.appointment.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;

import androidx.databinding.DataBindingUtil;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AlertDialog;

import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.pinpoint.appointment.BaseApplication;
import com.pinpoint.appointment.BaseConstants;
import com.pinpoint.appointment.R;
import com.pinpoint.appointment.api.RequestCode;
import com.pinpoint.appointment.customviews.CustomDialog;
import com.pinpoint.appointment.customviews.GenericView;
import com.pinpoint.appointment.databinding.ActivitySplashBinding;
import com.pinpoint.appointment.helper.MessageHelper;
import com.pinpoint.appointment.helper.PrefHelper;
import com.pinpoint.appointment.interfaces.DataObserver;
import com.pinpoint.appointment.models.AppMessages;
import com.pinpoint.appointment.models.CheckVersion;
import com.pinpoint.appointment.models.LoginHelper;
import com.pinpoint.appointment.permissionUtils.PermissionClass;
import com.pinpoint.appointment.utils.Debug;
import com.pinpoint.appointment.utils.Util;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SplashActivity extends GlobalActivity implements DataObserver {

    ActivitySplashBinding activitySplashBinding;
    Util util = new Util();
    private static int SPLASH_TIME_OUT = 1500;
    private final int APP_UNDER_CONSTRUCTION = 0;
    private final int NO_UPDATE = 1;
    private final int OPTIONAL_UPDATE = 2;
    private final int COMPULSORY_UPDATE = 3;
    private static int SETTINGS_DIALOG = 1235;
    private boolean firsttime = true;
    private ProgressBar mProgress;
    Handler handler;
    Dialog dialog;

    boolean gotoNotification = false;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {


        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SETTINGS_DIALOG) {
            if (!checkPermissions()) {
                if (checkPermission(SplashActivity.this, PermissionClass.REQUEST_CODE_RUNTIME_PERMISSION,
                        Arrays.asList(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CALL_PHONE))) {

                }
//                CustomDialog.getInstance().showAlertWithButtonClick(SplashActivity.this, MessageHelper.getInstance().getAppMessage(getString(R.string.str_request_location_permission))
//                        , MessageHelper.getInstance().getAppMessage(getString(R.string.str_app_name))
//                        , MessageHelper.getInstance().getAppMessage(getString(R.string.str_settings))
//                        ,
//                        new View.OnClickListener() {
//                            @Override
//                            public void onClick(View view)
//                            {
//                                CustomDialog.getInstance().hide();
//                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//                                Uri uri = Uri.fromParts("package", getPackageName(), null);
//                                intent.setData(uri);
//                                startActivityForResult(intent,SETTINGS_DIALOG);
//                            }
//                        }, MessageHelper.getInstance().getAppMessage(getString(R.string.str_dismiss))
//                        ,
//                        new View.OnClickListener() {
//                            @Override
//                            public void onClick(View view) {
//                                CustomDialog.getInstance().hide();
//                            }
//                        }, false
//                );
            } else {
                CustomDialog.getInstance().hide();
                redirectUser();
//              CheckVersion.checkAppVersion(SplashActivity.this, this);
            }
        }
    }


    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        List<String> shouldPermit = new ArrayList<>();

        if (requestCode == PermissionClass.REQUEST_CODE_RUNTIME_PERMISSION) {

            boolean isShouldProvidePermissionRationale = false;
            if (grantResults.length > 0) {
                for (int i = 0; i < grantResults.length; i++) {
                    //  permissions[i] = Manifest.permission.CAMERA; //for specific permission check
//                    grantResults[i] = PackageManager.PERMISSION_DENIED;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
//                            if(!PrefHelper.getBoolean("firsttime",true)) {
                            if (ActivityCompat.shouldShowRequestPermissionRationale(SplashActivity.this, permissions[i])) {
                                isShouldProvidePermissionRationale = true;
//                                }
//                            }
                            }
                        }
                    }
                    shouldPermit.add(permissions[i]);
                }
            }


            if (!checkPermissions()) {
//                    if(isShouldProvidePermissionRationale) {
//                        CustomDialog.getInstance().showAlertWithButtonClick(SplashActivity.this, MessageHelper.getInstance().getAppMessage(getString(R.string.str_request_location_permission))
//                                , MessageHelper.getInstance().getAppMessage(getString(R.string.str_app_name))
//                                , MessageHelper.getInstance().getAppMessage(getString(R.string.str_ok))
//                                ,
//                                new View.OnClickListener() {
//                                    @Override
//                                    public void onClick(View view) {
//                                        CustomDialog.getInstance().hide();
////                                      Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//                                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//                                        Uri uri = Uri.fromParts("package", getPackageName(), null);
////                                        intent.addCategory(Intent.CATEGORY_DEFAULT);
////                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                                        intent.setData(uri);
//                                        startActivityForResult(intent, SETTINGS_DIALOG);
//                                    }
//                                }, MessageHelper.getInstance().getAppMessage(getString(R.string.str_dismiss))
//                                ,
//                                new View.OnClickListener() {
//                                    @Override
//                                    public void onClick(View view) {
//                                        CustomDialog.getInstance().hide();
//                                    }
//                                }, false
//                        );
//                    }else
//                        {

                if (checkPermission(SplashActivity.this, PermissionClass.REQUEST_CODE_RUNTIME_PERMISSION,
                        Arrays.asList(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CALL_PHONE))) {

                }
//                        }
            } else {
                CustomDialog.getInstance().hide();
                redirectUser();
//                  CheckVersion.checkAppVersion(SplashActivity.this, this);
            }
            PrefHelper.setBoolean("firsttime", false);
        }
    }


    private boolean checkPermissions() {

        String[] permissions = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CALL_PHONE};
        List<String> deniedPermissionList = new ArrayList<>();

        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(SplashActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {
                deniedPermissionList.add(permission);
            }
        }
        if (deniedPermissionList.size() > 0) {
            return false;
        } else {
            return true;
        }
//        return  PermissionClass.checkPermission(HomeActivity.this, PermissionClass.REQUEST_CODE_RUNTIME_PERMISSION,
//                Arrays.asList(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.CALL_PHONE));
    }

    public boolean checkPermission(final Context context, final int requestCode, List<String> permissionList) {

        boolean isshouldshowpermissionRationale = true;
        final List<String> deniedPermissionList = new ArrayList<>();

        for (String permission : permissionList) {
            if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                deniedPermissionList.add(permission);
                if (ActivityCompat.shouldShowRequestPermissionRationale(SplashActivity.this, permission)) {
                    isshouldshowpermissionRationale = false;
                }
                if (PrefHelper.getBoolean("firsttime", true)) {
                    isshouldshowpermissionRationale = false;
                }
            }
        }

        if (deniedPermissionList.size() > 0) {
            if (isshouldshowpermissionRationale) {
                showAlertWithButtonClick(SplashActivity.this, MessageHelper.getInstance().getAppMessage(getString(R.string.str_request_location_permission))
                        , MessageHelper.getInstance().getAppMessage(getString(R.string.str_app_name))
                        , MessageHelper.getInstance().getAppMessage(getString(R.string.str_settings))
                        ,
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
//                                CustomDialog.getInstance().hide();
                                dialog.dismiss();
//                                      Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", getPackageName(), null);
//                                        intent.addCategory(Intent.CATEGORY_DEFAULT);
//                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.setData(uri);
                                startActivityForResult(intent, SETTINGS_DIALOG);
                            }
                        }, MessageHelper.getInstance().getAppMessage(getString(R.string.str_dismiss))
                        ,
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
//                                CustomDialog.getInstance().hide();
                                dialog.dismiss();
                            }
                        }, false
                );
            } else {
                String[] deniedPermissionArray = new String[deniedPermissionList.size()];
                deniedPermissionArray = deniedPermissionList.toArray(deniedPermissionArray);
                makeRequest(context, requestCode, deniedPermissionArray);
//                    showAlertWithButtonClick(SplashActivity.this, MessageHelper.getInstance().getAppMessage(getString(R.string.str_request_location_permission))
//                            , MessageHelper.getInstance().getAppMessage(getString(R.string.str_app_name))
//                            , MessageHelper.getInstance().getAppMessage(getString(R.string.str_ok))
//                            ,
//                            new View.OnClickListener() {
//                                @Override
//                                public void onClick(View view) {
////                                CustomDialog.getInstance().hide();
//                                    dialog.dismiss();
////                                  Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//
//                                }
//                            }, MessageHelper.getInstance().getAppMessage(getString(R.string.str_dismiss))
//                            ,
//                            new View.OnClickListener() {
//                                @Override
//                                public void onClick(View view) {
////                                CustomDialog.getInstance().hide();
//                                    dialog.dismiss();
//                                }
//                            }, false
//                    );

            }
            PrefHelper.setBoolean("firsttime", false);
            return false;
        } else {
            PrefHelper.setBoolean("firsttime", false);
            return true;
        }
    }

    /**
     * This method shows permission dialog whose permission is not granted.
     * Calling this method brings up a standard Android dialog,
     * which you cannot customize.
     *
     * @param context     (Context)     : application context
     * @param requestCode (int)     : request code to be identify the request e.g. 151
     * @param permission  (String[]) : list of permission that need to be granted by user
     * @see android.Manifest.permission
     * @see #
     */
    private static void makeRequest(Context context, int requestCode, String[] permission) {

        ActivityCompat.requestPermissions((Activity) context,
                permission,
                requestCode);
    }

    @Override
    public void onNewIntent(Intent newIntent) {
        this.setIntent(newIntent);

        // Now getIntent() returns the updated Intent
        if (getIntent() != null && getIntent().getBooleanExtra(BaseConstants.NOTIFICATION_CLICK, false)) {
            gotoNotification = getIntent().getBooleanExtra(BaseConstants.NOTIFICATION_CLICK, false);
            Debug.trace("Tag:gotonotification");
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activitySplashBinding = DataBindingUtil.setContentView(this, R.layout.activity_splash);
        String md5String = BaseConstants.AUTH_STRING + "0";
        md5String = "0_" + Util.stringToMD5(md5String);
//      String msg=getIntent().getExtras().getString("msg", "0");
        if (getIntent() != null && getIntent().hasExtra(BaseConstants.NOTIFICATION_CLICK)) {
            gotoNotification = getIntent().getExtras().getBoolean(BaseConstants.NOTIFICATION_CLICK, false);
        }

        if (Util.isTabletDevice(SplashActivity.this)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }

        byte[] data = new byte[0];
        try {
            data = md5String.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String base64 = Base64.encodeToString(data, Base64.DEFAULT);
        PrefHelper.setString(PrefHelper.KEY_AUTHENTICATIONKEY, base64);

        activitySplashBinding.tvAppVersion.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_SEMIBOLD));
        activitySplashBinding.tvAppName.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));
        activitySplashBinding.tvAppVersion.setText("V".concat(Util.getAppVersionName()));
        activitySplashBinding.tvAppName.setText(MessageHelper.getInstance().getAppMessage(getString(R.string.app_name)));
//        if (PermissionClass.checkPermission(SplashActivity.this, PermissionClass.REQUEST_CODE_RUNTIME_PERMISSION,
//                Arrays.asList(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.CALL_PHONE)))
//        {
        //comment by Sarfaraj
        CheckVersion.checkAppVersion(SplashActivity.this, this);
//        }

    }

    @Override
    public void onResume() {
        super.onResume();

    }

    public void underConstruction(String maintenanceMsg) {
        activitySplashBinding.txtMaintenanceAlert.setVisibility(View.VISIBLE);
        activitySplashBinding.txtMaintenanceAlert.setText(maintenanceMsg);
    }

    @Override
    public void OnSuccess(RequestCode requestCode, Object object) {

        System.out.println("OnSuccess requestCode " + requestCode + ", " + object);

        switch (requestCode) {
            case checkVersion:
                CustomDialog.getInstance().hide();
                activitySplashBinding.pbProgress.setVisibility(View.GONE);
                BaseConstants.NOTIFICATION_COUNT = CheckVersion.getCheckVersionModel().getBadgcount();
                PrefHelper.setInt(BaseConstants.COUNT, BaseConstants.NOTIFICATION_COUNT);
//              BaseConstants.PAYMENT_STATUS=CheckVersion.getCheckVersionModel().getPaymentStatus();
                PrefHelper.setInt(BaseConstants.PAYMENT_STATUS, CheckVersion.getCheckVersionModel().getPaymentStatus());
                switch (CheckVersion.getCheckVersionModel().getIsUpdateType()) {

                    //case APP_UNDER_CONSTRUCTION:
                    //break;
                    //case OPTIONAL_UPDATE:
                    //break;
                    case COMPULSORY_UPDATE:
                        //Util.showToast(getCurrContext(), "CheckVersion.getCheckVersionModel()" + CheckVersion.getCheckVersionModel().getUpdateMessage());
                        displayCustom(CheckVersion.getCheckVersionModel().getUpdateMessage());
//                      redirectUser();
                        break;

                    //case NO_UPDATE:
                    //break;
                    default:
                        switch (CheckVersion.getCheckVersionModel().getIsMessageUpdate()) {
                            case 0:
                                redirectUser();
                                break;
                            case 1:
//                                mProgress.setVisibility(View.VISIBLE);
                                AppMessages.getAppMessages(SplashActivity.this, this);
//                                redirectUser();
                                break;
                        }
                        break;

                }
                break;

            case messages:
                CustomDialog.getInstance().hide();
                redirectUser();
                break;
        }
    }

    private void redirectUser() {
        if (checkPermission(SplashActivity.this, PermissionClass.REQUEST_CODE_RUNTIME_PERMISSION,
                Arrays.asList(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CALL_PHONE))) {


            if (LoginHelper.getInstance().isLoggedIn())
//      if(PrefHelper.getBoolean("isLoggedin",false))
            {
                Intent i = new Intent(SplashActivity.this, HomeActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                BaseConstants.SELECT_SENT_FRIEND = false;
                i.putExtra(BaseConstants.NOTIFICATION_CLICK, gotoNotification);
                startActivity(i);
                finish();
            } else {
                Intent i = new Intent(SplashActivity.this, LoginActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(i);
                finish();
            }

        }
    }

    @Override
    public void OnFailure(RequestCode requestCode, String errorCode, String error) {
//        Util.showToast(getCurrContext(), "OnFailure " + errorCode + ", " + error);

        CustomDialog.getInstance().hide();

        if (error != null)
            CustomDialog.getInstance().showAlertWithButtonClick(this, errorCode, error, MessageHelper.getInstance().getAppMessage(getString(R.string.str_ok)), new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    CustomDialog.getInstance().hide();
                    finish();
                }
            }, MessageHelper.getInstance().getAppMessage(getString(R.string.str_cancel)), new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    CustomDialog.getInstance().hide();
                }
            }, false);
    }

    @Override
    public void onOtherStatus(RequestCode requestCode, Object object) {
        CustomDialog.getInstance().hide();
    }

    @Override
    public void onRetryRequest(RequestCode requestCode) {
        CustomDialog.getInstance().hide();
    }

    private void displayCustom(String message) {


        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.diag_alert_compulsary, null);
        alertDialog.setView(dialogView);
        alertDialog.setCancelable(true);

        TextView mTxtTitle = GenericView.findViewById(dialogView, R.id.tv_txtTitle);
        TextView mTxtMessage = GenericView.findViewById(dialogView, R.id.tv_txtMessage);

        Button mBtnButton1 = GenericView.findViewById(dialogView, R.id.bt_btnButton1);
        Button mBtnButton2 = GenericView.findViewById(dialogView, R.id.bt_btnButton2);


        mTxtTitle.setText(MessageHelper.getInstance().getAppMessage(getString(R.string.str_app_name)));

        mTxtMessage.setText(message);
        mBtnButton1.setText(MessageHelper.getInstance().getAppMessage(getString(R.string.str_cancel)));
        mBtnButton2.setText(MessageHelper.getInstance().getAppMessage(getString(R.string.str_ok)));

        if (CheckVersion.getCheckVersionModel().getIsUpdateType() == COMPULSORY_UPDATE
                || CheckVersion.getCheckVersionModel().getIsUpdateType() == APP_UNDER_CONSTRUCTION)
            mBtnButton1.setVisibility(View.GONE);


        final AlertDialog mAlertDialog = alertDialog.show();

        mBtnButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAlertDialog.cancel();
//                mProgress.setVisibility(View.VISIBLE);
//
//                AppMessages.getAppMessages(SplashActivity.this, dataObserver);
            }
        });

        mBtnButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mAlertDialog.cancel();

                if (CheckVersion.getCheckVersionModel().getIsUpdateType() == COMPULSORY_UPDATE
                        || CheckVersion.getCheckVersionModel().getIsUpdateType() == OPTIONAL_UPDATE) {

                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(CheckVersion.getCheckVersionModel().getUrl()));
                    startActivity(i);
                    finish();

                } else if (CheckVersion.getCheckVersionModel().getIsUpdateType() == APP_UNDER_CONSTRUCTION) {
                    finish();
                } else {
//                    mProgress.setVisibility(View.VISIBLE);
//                    AppMessages.getAppMessages(SplashActivity.this, dataObserver);
                }

            }
        });

    }

    public void showAlertWithButtonClick(final Context context, String msg, String header, String positiveButton, View.OnClickListener onClickListener1, String negativeButton, View.OnClickListener onClickListener2, boolean isShowNegative) {
        try {
            dialog = new Dialog(context, R.style.DialogTheme);
            @SuppressLint("InflateParams") final View view = LayoutInflater.from(context).inflate(R.layout.dialog_alert_new, null);
            TextView txtHeader = view.findViewById(R.id.txtHeader);
            TextView txtMessage = view.findViewById(R.id.tv_txtMessage);
            TextView txtNegative = view.findViewById(R.id.tv_txtNegative);
            ImageView iv_header = (ImageView) view.findViewById(R.id.iv_header);

            txtHeader.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_SEMIBOLD));
            txtMessage.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));
            txtNegative.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));

            if (header.equalsIgnoreCase("Alert")) {
                iv_header.setImageResource(R.drawable.ic_call_black_24dp);
            } else if (header.equalsIgnoreCase("Logout")) {
                iv_header.setImageResource(R.drawable.ic_info_outline_black_24dp);
            }
            Button btn_positive = view.findViewById(R.id.btn_positive);
            btn_positive.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_SEMIBOLD));
            txtHeader.setText(header);
            txtMessage.setText(msg);
            btn_positive.setText(positiveButton);
            txtNegative.setText(negativeButton);
            if (isShowNegative) {
                txtNegative.setVisibility(View.VISIBLE);
            }
//          mTxtYes.setTag(buttonText);*/
            btn_positive.setOnClickListener(onClickListener1);
            txtNegative.setOnClickListener(onClickListener2);

            dialog.setCanceledOnTouchOutside(false);
            dialog.setCancelable(false);
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
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
