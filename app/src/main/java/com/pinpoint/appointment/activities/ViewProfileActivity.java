package com.pinpoint.appointment.activities;


import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;

import androidx.databinding.DataBindingUtil;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.pinpoint.appointment.BaseActivity;
import com.pinpoint.appointment.BaseApplication;
import com.pinpoint.appointment.BaseConstants;
import com.pinpoint.appointment.R;
import com.pinpoint.appointment.api.ApiList;
import com.pinpoint.appointment.api.RequestCode;
import com.pinpoint.appointment.api.ResponseStatus;
import com.pinpoint.appointment.customviews.CustomDialog;
import com.pinpoint.appointment.customviews.GenericView;
import com.pinpoint.appointment.databinding.ActivityViewProfileBinding;
import com.pinpoint.appointment.graphicsUtils.GraphicsUtil;
import com.pinpoint.appointment.helper.MessageHelper;
import com.pinpoint.appointment.helper.PrefHelper;
import com.pinpoint.appointment.helper.ToastHelper;
import com.pinpoint.appointment.interfaces.ClickEvent;
import com.pinpoint.appointment.interfaces.DataObserver;
import com.pinpoint.appointment.models.CustomerDetails;
import com.pinpoint.appointment.permissionUtils.PermissionClass;
import com.pinpoint.appointment.utils.Debug;
import com.pinpoint.appointment.utils.Util;
import com.pinpoint.appointment.validator.ValidationClass;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class ViewProfileActivity extends BaseActivity implements ClickEvent {


    private ViewProfileActivity parent;
    private Bundle bundle;
    ActivityViewProfileBinding viewProfileBinding;
    String strName, strEmail, strCompany;
    private static final int GOOGLE_API_CLIENT_ID = 0;
    CustomerDetails customerDetails;
    private int[] screenWH;
    public static final int REQUEST_CAMERA = 0;
    public static final int REQUEST_GALLERY = 1;
    public static final int PICK_FILE_RESULT_CODE = 3;
    private final int REQUEST_CROPPED_IMAGE = 2;
    private String imageUrl = "", selectedImagePath = "";
    private static final int SETTINGS_DIALOG = 1235;
    Dialog dialog;
    private boolean isVisible = false;
    private boolean isStarted = false;

    public ViewProfileActivity() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewProfileBinding = DataBindingUtil.setContentView(this, R.layout.activity_view_profile);
        ImageView imgBack = GenericView.findViewById(this, R.id.iv_imgMenu_Back);
        ImageView imgSettings = GenericView.findViewById(this, R.id.iv_imgSetting);
        TextView tvHeader = GenericView.findViewById(this, R.id.tv_txtTitle);
        tvHeader.setText(MessageHelper.getInstance().getAppMessage(getString(R.string.str_view_profile)));
        parent = ViewProfileActivity.this;

        screenWH = GraphicsUtil.getScreenWidthHeight();
        if (Util.isTabletDevice(this)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        imgBack.setImageResource(R.mipmap.ic_back);
        imgSettings.setVisibility(View.INVISIBLE);
        tvHeader.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_SEMIBOLD));
        viewProfileBinding.tvFriends.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));
        viewProfileBinding.tvAppointment.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));
        viewProfileBinding.tvFriendsCount.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_SEMIBOLD));
        viewProfileBinding.tvAppointmentCount.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_SEMIBOLD));

        viewProfileBinding.etEdtName.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));
        viewProfileBinding.etEdtCompanyName.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));
        viewProfileBinding.etEdtEmail.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));
        viewProfileBinding.etEdtPhone.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));
        viewProfileBinding.btEditProfile.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_SEMIBOLD));

        viewProfileBinding.inputLayoutName.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));
        viewProfileBinding.inputLayoutCompanyName.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));
        viewProfileBinding.inputLayoutEmail.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));
        viewProfileBinding.inputLayoutPhone.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));

        viewProfileBinding.inputLayoutPhone.setHint(MessageHelper.getInstance().getAppMessage(getString(R.string.str_phone)));
        viewProfileBinding.inputLayoutName.setHint(MessageHelper.getInstance().getAppMessage(getString(R.string.str_name)));
        viewProfileBinding.inputLayoutCompanyName.setHint(MessageHelper.getInstance().getAppMessage(getString(R.string.str_company_name)));
        viewProfileBinding.inputLayoutEmail.setHint(MessageHelper.getInstance().getAppMessage(getString(R.string.str_email)));
        viewProfileBinding.btEditProfile.setText(MessageHelper.getInstance().getAppMessage(getString(R.string.str_update)));
        viewProfileBinding.tvFriends.setText(MessageHelper.getInstance().getAppMessage(getString(R.string.str_friends)));
        viewProfileBinding.tvAppointment.setText(MessageHelper.getInstance().getAppMessage(getString(R.string.str_appointments)));
        showProgressBar(this);
        CustomerDetails.getUserInfo(parent, dataObserver);

        Util.setupOutSideTouchHideKeyboard(GenericView.findViewById(this, R.id.parent));
    }

    private void callAPI() {
        if (checkValidation()) {
            CustomerDetails details = new CustomerDetails();
            details.setEmail(strEmail);
            details.setName(strName);
            details.setCompany(strCompany);
            details.setImagePath(selectedImagePath);
            CustomerDetails.updateUser(parent, details, dataObserver);
//          FriendDetails.addFriendsData(ViewProfileActivity.this,addFriendBinding.etEdtPhone.getText().toString(),addFriendBinding.etEdtName.getText().toString(),1,dataObserver);
        }
    }

    DataObserver dataObserver = new DataObserver() {
        @Override
        public void OnSuccess(RequestCode requestCode, Object object) {
            switch (requestCode) {
                case updateuserinfo:
                    CustomDialog.getInstance().hide();
                    String response = object.toString();
                    try {
                        JSONObject jsonObject = new JSONObject(response);
//                    JSONObject jsonObject1=jsonObject.getJSONObject(ApiList.KEY_RESPONSE);
                        String message = jsonObject.getString(ApiList.KEY_MESSAGE);
//                    CustomDialog.getInstance().showAlert(ViewProfileActivity.this,message,Util.getAppKeyValue(parent,R.string.str_app_name), Util.getAppKeyValue(ViewProfileActivity.this, R.string.lblOk), Util.getAppKeyValue(ViewProfileActivity.this, R.string.str_dismiss),false);

                        CustomDialog.getInstance().showAlertWithButtonClick(ViewProfileActivity.this, message
                                , MessageHelper.getInstance().getAppMessage(getString(R.string.str_app_name))
                                , MessageHelper.getInstance().getAppMessage(getString(R.string.str_ok))
                                ,
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        CustomDialog.getInstance().hide();
                                        PrefHelper.setBoolean("isnotification", false);
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

//                    ToastHelper.displayCustomToast(message);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    break;
                case getuserinfo:

                    List<CustomerDetails> userInfos = (List<CustomerDetails>) object;
                    try {

                        if (userInfos != null && userInfos.size() > 0) {
                            setUserInfo(userInfos.get(0));
                            customerDetails = userInfos.get(0);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (dialog.isShowing())
                        dialog.dismiss();
//                CustomDialog.getInstance().hide();
                    break;
                case deleteprofileimage:

                    CustomDialog.getInstance().hide();
                    try {
                        String response1 = object.toString();
//                    BaseConstants.REFRESH_PROPERTIES=true;
                        JSONObject jsonObject = new JSONObject(response1);
//                    JSONObject jsonObject1=jsonObject.getJSONObject(ApiList.KEY_RESPONSE);
                        String message = jsonObject.getString(ApiList.KEY_MESSAGE);
                        ToastHelper.displayCustomToast(message);
                        customerDetails.setProfileimageurl("http://pinpointappointment.com/admin/images/no-available-image.png");
                        Picasso.get().load(customerDetails.getProfileimageurl()).noFade().placeholder(R.drawable.user1)
                                .into(viewProfileBinding.ivImgSignup);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    break;
            }
        }

        @Override
        public void OnFailure(RequestCode requestCode, String errorCode, String error) {
            if (dialog != null && dialog.isShowing())
                dialog.dismiss();
            switch (requestCode) {
                case getuserinfo:


                    CustomDialog.getInstance().hide();
                    if (error.equalsIgnoreCase(ResponseStatus.STATUS_CRASH) || errorCode.equalsIgnoreCase(ResponseStatus.STATUS_CRASH)) {
                        CustomDialog.getInstance().showAlert(ViewProfileActivity.this, errorCode, Util.getAppKeyValue(ViewProfileActivity.this, R.string.str_server_error), Util.getAppKeyValue(ViewProfileActivity.this, R.string.lblOk), Util.getAppKeyValue(ViewProfileActivity.this, R.string.str_dismiss), false);
                    } else {
                        CustomDialog.getInstance().showAlert(ViewProfileActivity.this, error, errorCode, Util.getAppKeyValue(ViewProfileActivity.this, R.string.lblOk), Util.getAppKeyValue(ViewProfileActivity.this, R.string.str_dismiss), false);
                    }
                    break;
                case updateuserinfo:
                    CustomDialog.getInstance().hide();
                    CustomDialog.getInstance().showAlert(ViewProfileActivity.this, error, errorCode, Util.getAppKeyValue(ViewProfileActivity.this, R.string.lblOk), Util.getAppKeyValue(ViewProfileActivity.this, R.string.str_dismiss), false);
                    break;
                case deleteprofileimage:
                    CustomDialog.getInstance().hide();
                    if (error == null || error.equalsIgnoreCase("")) {
                        finish();
                    } else {
                        CustomDialog.getInstance().showAlert(ViewProfileActivity.this, error, errorCode, Util.getAppKeyValue(ViewProfileActivity.this, R.string.lblOk), Util.getAppKeyValue(ViewProfileActivity.this, R.string.str_dismiss), false);
                    }
                    break;
            }
        }

        @Override
        public void onOtherStatus(RequestCode requestCode, Object object) {

            switch (requestCode) {
                case getuserinfo:
//                CustomDialog.getInstance().hide();
                    CustomDialog.getInstance().hide();
                    String response = object.toString();
                    try {
                        JSONObject jsonObject = new JSONObject(response);
//                    JSONObject jsonObject1=jsonObject.getJSONObject(ApiList.KEY_RESPONSE);
                        String message = jsonObject.getString(ApiList.KEY_MESSAGE);

                        CustomDialog.getInstance().showAlert(ViewProfileActivity.this, message, MessageHelper.getInstance().getAppMessage(getString(R.string.str_fail_dialog_header)), Util.getAppKeyValue(ViewProfileActivity.this, R.string.lblOk), Util.getAppKeyValue(ViewProfileActivity.this, R.string.str_dismiss), false);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    break;
            }
        }

        @Override
        public void onRetryRequest(RequestCode requestCode) {
            CustomDialog.getInstance().hide();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
//        CustomDialog.getInstance().showProgressBar(ViewProfileActivity.this);
    }

    private void setUserInfo(CustomerDetails userInfo) {
        viewProfileBinding.etEdtName.setText(userInfo.getName());
        viewProfileBinding.etEdtCompanyName.setText(userInfo.getCompany());
        viewProfileBinding.etEdtEmail.setText(userInfo.getEmail());
        viewProfileBinding.etEdtPhone.setText(userInfo.getContact());
        viewProfileBinding.etEdtPhone.setEnabled(false);
        viewProfileBinding.tvAppointmentCount.setText(String.valueOf(userInfo.getTotalAppointmentscount()));
        viewProfileBinding.tvFriendsCount.setText(String.valueOf(userInfo.getTotalfriendcount()));
        Picasso.get().load(userInfo.getProfileimageurl()).noFade().placeholder(R.drawable.user1)
                .into(viewProfileBinding.ivImgSignup);
    }


    @Override
    public void onClickEvent(View view) {
        switch (view.getId()) {


            case R.id.iv_imgMenu_Back:
                finish();
                break;
            case R.id.ll_friends:
                BaseConstants.REFRESH_FRIENDS = true;
                finish();
                break;
            case R.id.ll_appointment:
                BaseConstants.REFRESH_APPOINTMENT = true;
                finish();
                break;
            case R.id.bt_editProfile:
                callAPI();
                break;
            case R.id.iv_imgSignup:
                selectImage();
//                }
                break;
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

//            selectImage();
        }
        if (checkPermissions()) {
            selectImage();
        } else {
            CustomDialog.getInstance().showAlertWithButtonClick(ViewProfileActivity.this, MessageHelper.getInstance().getAppMessage(getString(R.string.str_request_location_permission))
                    , MessageHelper.getInstance().getAppMessage(getString(R.string.str_app_name))
                    , MessageHelper.getInstance().getAppMessage(getString(R.string.str_ok))
                    ,
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            CustomDialog.getInstance().hide();
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", getPackageName(), null);
                            intent.setData(uri);
                            startActivityForResult(intent, SETTINGS_DIALOG);
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
        }
    }

    private boolean checkPermissions() {

        String[] permissions = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        List<String> deniedPermissionList = new ArrayList<>();

        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(ViewProfileActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {
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

    public void selectImage() {

        final int REQUEST_CANCEL = 2;
        final int REQUEST_DELETE = 3;
//        final CharSequence[] items = {Util.getAppKeyValue(this,R.string.str_click_photo),
//                Util.getAppKeyValue(this,R.string.str_open_gallery),
//                Util.getAppKeyValue(this,R.string.str_cancel)};
        CharSequence[] items = null;
        if (selectedImagePath.equalsIgnoreCase("") && customerDetails == null) {
            items = new CharSequence[]{Util.getAppKeyValue(this, R.string.str_click_photo),
                    Util.getAppKeyValue(this, R.string.str_open_gallery),
                    Util.getAppKeyValue(this, R.string.str_cancel)};
        } else {
            if (customerDetails != null && !customerDetails.getProfileimageurl().equalsIgnoreCase("http://pinpointappointment.com/admin/images/no-available-image.png")) {
                items = new CharSequence[]{Util.getAppKeyValue(this, R.string.str_click_photo),
                        Util.getAppKeyValue(this, R.string.str_open_gallery),
                        Util.getAppKeyValue(this, R.string.str_cancel), Util.getAppKeyValue(this, R.string.str_delete)};
            } else if (!selectedImagePath.equalsIgnoreCase("")) {
                items = new CharSequence[]{Util.getAppKeyValue(this, R.string.str_click_photo),
                        Util.getAppKeyValue(this, R.string.str_open_gallery),
                        Util.getAppKeyValue(this, R.string.str_cancel), Util.getAppKeyValue(this, R.string.str_delete)};
            } else {
                items = new CharSequence[]{Util.getAppKeyValue(this, R.string.str_click_photo),
                        Util.getAppKeyValue(this, R.string.str_open_gallery),
                        Util.getAppKeyValue(this, R.string.str_cancel)};
            }
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(parent);
        builder.setTitle(Util.getAppKeyValue(parent, R.string.str_select_profile_pic));
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {

                switch (item) {
                    case REQUEST_CAMERA:
                        if (PermissionClass.checkPermission(parent, PermissionClass.REQUEST_CODE_RUNTIME_PERMISSION_CAMERA,
                                Arrays.asList(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE))) {

                            BaseConstants.CAPTUREDFILEURI = GraphicsUtil.getOutputMediaFileUri(GraphicsUtil.CAPTURED_DIRECTORY_NAME,
                                    GraphicsUtil.MEDIA_IMAGE);
                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, BaseConstants.CAPTUREDFILEURI);
                            startActivityForResult(intent, REQUEST_CAMERA);

                        }
                        break;
                    case REQUEST_GALLERY:
                        if (PermissionClass.checkPermission(parent, PermissionClass.REQUEST_CODE_RUNTIME_PERMISSION_STORAGE,
                                Collections.singletonList(Manifest.permission.WRITE_EXTERNAL_STORAGE))) {

                            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            intent.setType(BaseConstants.GALLERY_FILE_TYPE);
                            startActivityForResult(Intent.createChooser(intent,
                                    MessageHelper.getInstance().getAppMessage(getString(R.string.str_select_file))), REQUEST_GALLERY);

                        }
                        break;
                    case REQUEST_CANCEL:
                        dialog.dismiss();
                        break;
                    case REQUEST_DELETE:
                        if (!selectedImagePath.equalsIgnoreCase("")) {
                            selectedImagePath = "";
                            viewProfileBinding.ivImgSignup.setImageResource(R.drawable.user1);
                        }
                        if (customerDetails != null && !customerDetails.getProfileimageurl().equalsIgnoreCase("http://pinpointappointment.com/admin/images/no-available-image.png")) {
                            selectedImagePath = "";
                            viewProfileBinding.ivImgSignup.setImageResource(R.drawable.user1);
                            CustomerDetails.deleteProfileImage(ViewProfileActivity.this, customerDetails, dataObserver);
                        }
                        break;
                }
            }
        });
        builder.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CAMERA:
                if (resultCode == Activity.RESULT_OK) {
                    onCaptureImageResult();
                }
                break;

            case REQUEST_GALLERY:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    onSelectFromGalleryResult(data);
                }
                break;

            case REQUEST_CROPPED_IMAGE:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    afterCroppedImageResult(data);
                }
                break;

            case PICK_FILE_RESULT_CODE:

                if (data != null) {
                    if (Util.getPath(this, data.getData()).contains(BaseConstants.EXTENSION_DOC)
                            || Util.getPath(this, data.getData()).contains(BaseConstants.EXTENSION_PDF)
                            || Util.getPath(this, data.getData()).contains(BaseConstants.EXTENSION_TXT)
                            || Util.getPath(this, data.getData()).contains(BaseConstants.EXTENSION_PNG)
                            || Util.getPath(this, data.getData()).contains(BaseConstants.EXTENSION_JPG)
                            || Util.getPath(this, data.getData()).contains(BaseConstants.EXTENSION_JPEG)) {

                        setFileData(Util.getPath(this, data.getData()));

                    } else {
                        ToastHelper.displayInfo(MessageHelper.getInstance().getAppMessage(getString(R
                                .string.str_err_valid_file_format)));
                    }
                }
                break;
            case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                if (resultCode == RESULT_OK) {
                    String Message = "From: ViewProfileActivity , After Image cropped : Successfully";
//                    LeadDetails.setMessage(ViewProfileActivity.this, Message);
                    Uri resultUri = result.getUri();
                    File cropFile = new File(resultUri.getPath());

                    if (!TextUtils.isEmpty(cropFile.toString())) {

                        if (cropFile.exists())
                            selectedImagePath = cropFile.getAbsolutePath();

                        if (selectedImagePath != null) {

                            viewProfileBinding.ivImgSignup.setImageBitmap(BitmapFactory.decodeFile(selectedImagePath));
                        } else {
                            ToastHelper.displayInfo(MessageHelper.getInstance().getAppMessage(getString(R.string.str_msg_image_pick_issue)));
                        }
                    } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                        String Messagea = "From: ViewProfileActivity , After Image cropped : " + result.getError().toString();
//                        LeadDetails.setMessage(ViewProfileActivity.this, Messagea);
                        Exception error = result.getError();
                    }
                }
                break;

            case SETTINGS_DIALOG:
                if (!checkPermissions()) {
                    CustomDialog.getInstance().showAlertWithButtonClick(ViewProfileActivity.this, MessageHelper.getInstance().getAppMessage(getString(R.string.str_request_location_permission))
                            , MessageHelper.getInstance().getAppMessage(getString(R.string.str_app_name))
                            , MessageHelper.getInstance().getAppMessage(getString(R.string.str_ok))
                            ,
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    CustomDialog.getInstance().hide();
                                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                                    intent.setData(uri);
                                    startActivityForResult(intent, SETTINGS_DIALOG);
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
                } else {
                    CustomDialog.getInstance().hide();
                    selectImage();
//              CheckVersion.checkAppVersion(SplashActivity.this, this);
                }
                break;
        }
    }

    private void setFileData(String path) {


        File file = new File(path);
        int file_size = Integer.parseInt(String.valueOf(file.length()));
        Log.v("Size", String.valueOf(file_size));


        /*  public static final int FileSize = 2 * 1024 * 1024;*/
        if (file_size <= BaseConstants.FileSize) {

            if (path.contains(BaseConstants.EXTENSION_PNG)
                    || path.contains(BaseConstants.EXTENSION_JPG)
                    || path.contains(BaseConstants.EXTENSION_JPEG)) {

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(new File(path).getAbsolutePath(), options);
                int imageHeight = options.outHeight;
                int imageWidth = options.outWidth;

            }
        } else {
//            ToastHelper.displayCustomDialogForValidation(parent, MessageHelper.getInstance().getAppMessage(parent.getResources().getString(R
//                    .string.str_err_max_size)), null);
        }


    }

    public void onCaptureImageResult() {
        Intent intent = new Intent(ViewProfileActivity.this, CropImageActivity.class);
        File file;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            file = new File(BaseConstants.CURRENTPHOTOPATH);
            BaseConstants.CAPTUREDFILEURI = Uri.parse(BaseConstants.CURRENTPHOTOPATH);
        } else {
            file = new File(BaseConstants.CAPTUREDFILEURI.getPath());
        }
        if (file.length() > 0) {

            BitmapFactory.Options options1 = new BitmapFactory.Options();
            options1.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(BaseConstants.CAPTUREDFILEURI.getPath(), options1);

            // Calculate inSampleSize
            options1.inSampleSize = GraphicsUtil.getInstance().calculateInSampleSize(options1, screenWH[0], (int) (screenWH[0] * 1.26));
            options1.inJustDecodeBounds = false;

            options1.inPreferredConfig = Bitmap.Config.ARGB_8888;

            Bitmap mBitmap = BitmapFactory.decodeFile(new File(BaseConstants.CAPTUREDFILEURI.getPath()).getAbsolutePath(), options1);
            Bitmap finalBitmap = null;


            ExifInterface ei = null;
            try {
                ei = new ExifInterface(BaseConstants.CAPTUREDFILEURI.getPath());
            } catch (IOException e) {
                e.printStackTrace();
            }

            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED);

            switch (orientation) {

                case ExifInterface.ORIENTATION_ROTATE_90:
                    finalBitmap = Util.rotateImage(mBitmap, 90);
                    break;

                case ExifInterface.ORIENTATION_ROTATE_180:
                    finalBitmap = Util.rotateImage(mBitmap, 180);
                    break;

                case ExifInterface.ORIENTATION_ROTATE_270:
                    finalBitmap = Util.rotateImage(mBitmap, 270);
                    break;

                case ExifInterface.ORIENTATION_NORMAL:
                    finalBitmap = mBitmap;
                    break;
                default:
                    finalBitmap = mBitmap;
                    break;
            }

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(new File(BaseConstants.CAPTUREDFILEURI.getPath()).getAbsolutePath(), options);
            int height = options.outHeight;
            int width = options.outWidth;
            if (finalBitmap != null) {
                String picPath = GraphicsUtil.getInstance().saveImage(finalBitmap, GraphicsUtil.CAPTURED_DIRECTORY_NAME);
                File newfile = new File(picPath);
                Uri imagPathh = Uri.fromFile(newfile);
                int length = 500;
                if (width < height) {
                    length = width - 100;
                } else {
                    length = height - 100;
                }
//            CropImage.activity(selectedImage).setRequestedSize(length,length).setMaxZoom(1).setMaxCropResultSize(length,length).setMinCropResultSize(length,length).setAutoZoomEnabled(false)
//                    .start(this);
                CropImage.activity(imagPathh).setRequestedSize(400, 400).setMaxZoom(1).setCropMenuCropButtonTitle(MessageHelper.getInstance().getAppMessage(getString(R.string.str_done))).setAutoZoomEnabled(false).setMaxCropResultSize(length, length).setMinCropResultSize(length, length)
                        .start(this);

            }

        }
    }

    private void onSelectFromGalleryResult(Intent data) {
        String imagePath;
        Uri selectedImage = data.getData();
        String[] path = {MediaStore.Images.Media.DATA};

        if (path != null && path.length > 0) {
            assert selectedImage != null;
            Cursor c = getContentResolver().query(selectedImage, path, null, null, null);
            if (c != null) {
                c.moveToFirst();
                int columnindex = c.getColumnIndex(path[0]);
                imagePath = c.getString(columnindex);
                c.close();
            } else {
                // In some device we can get selected image path directly like e.g. REDMI Note 3
                imagePath = selectedImage.getPath();
            }
        } else {
            // In some device we can get selected image path directly like e.g. REDMI Note 3
            assert selectedImage != null;
            imagePath = selectedImage.getPath();
        }


        int[] screenWH = GraphicsUtil.getScreenWidthHeight();

        Debug.trace("galleryImagePath", imagePath);
        Debug.trace("device screen: " + "Width " + screenWH[0] + "Height " + screenWH[1]);

        BitmapFactory.Options options1 = new BitmapFactory.Options();
        options1.inJustDecodeBounds = true;
        options1.inSampleSize = GraphicsUtil.getInstance().calculateInSampleSize(options1, screenWH[0], (int) (screenWH[0] * 1.26));
        options1.inJustDecodeBounds = false;
        options1.inPreferredConfig = Bitmap.Config.ARGB_8888;

        Bitmap bitmap = BitmapFactory.decodeFile(imagePath, options1);
        int imageHeight = options1.outHeight;
        int imageWidth = options1.outWidth;
        Debug.trace("galleryImagePath", String.valueOf(imageHeight));
        Debug.trace("galleryImagePath", String.valueOf(imageWidth));

        if (imageHeight >= BaseConstants.IMAGEHW && imageWidth >= BaseConstants.IMAGEHW) {


//            Intent intent = new Intent(parent, CropImageActivity.class);
//            if (imagePath.length() > 0) {
//                String picPath = GraphicsUtil.getInstance().saveImage(bitmap, GraphicsUtil.CONTEST_DIRECTORY);
//                intent.putExtra(CropImageActivity.IMAGE_PATH, picPath);
//                intent.putExtra(CropImageActivity.SHAPE, GraphicsUtil.SHAPE_SQUARE);
//                intent.putExtra(CropImageActivity.BITMAP_RATIO, (float) 0 / 0);
//                intent.putExtra(CropImageActivity.DIRECTORY_PATH, GraphicsUtil.CONTEST_DIRECTORY);
//                startActivityForResult(intent, REQUEST_CROPPED_IMAGE);
//            }
            int length = 500;
            if (imageWidth < imageHeight) {
                length = imageWidth - 100;
            } else {
                length = imageHeight - 100;
            }
            String Message = "From: ViewProfileActivity , On image selected from gallery";
//            LeadDetails.setMessage(ViewProfileActivity.this, Message);
            CropImage.activity(selectedImage).setRequestedSize(400, 400).setMaxZoom(1).setAutoZoomEnabled(false).setMaxCropResultSize(length, length).setCropMenuCropButtonTitle(MessageHelper.getInstance().getAppMessage(getString(R.string.str_done))).setMinCropResultSize(length, length)
                    .start(this);

        } else {
//            CustomDialog.getInstance().showAlert(, MessageHelper.getInstance().getAppMessage(parent.getResources().getString(R
//                    .string.str_err_image_size)), null);

        }

    }

    public void afterCroppedImageResult(Intent data) {
        File cropFile = new File(data.getStringExtra(CropImageActivity.IMAGE_PATH));

        if (!TextUtils.isEmpty(cropFile.toString())) {

            if (cropFile.exists())
                selectedImagePath = cropFile.getAbsolutePath();

            if (selectedImagePath != null) {
//                setFileData(selectedImagePath);
                viewProfileBinding.ivImgSignup.setImageBitmap(BitmapFactory.decodeFile(selectedImagePath));
//                Picasso.with(RegisterActivity.this).load(selectedImagePath).transform(new CircleTransform()).into(ImageView);
//                Picasso.with(RegisterActivity.this).load(new File(selectedImagePath))
//                .centerCrop().into(registerBinding.ivImgSignup);
//                Picasso.with(RegisterActivity.this).load(new File(selectedImagePath))
//                        .resize(80, 80).centerCrop().into(registerBinding.ivImgSignup);

            } else {
                ToastHelper.displayInfo(MessageHelper.getInstance().getAppMessage(getString(R.string.str_msg_image_pick_issue)));
            }

            Debug.trace("resize wh " + "screenWidth " + screenWH[0] + " " + "screenHeight " + screenWH[0] * BaseConstants.VALUE_IMAGE_RATIO);
        }

    }


    private boolean checkValidation() {

        strEmail = viewProfileBinding.etEdtEmail.getText().toString().trim();
        strName = viewProfileBinding.etEdtName.getText().toString().trim();
        strCompany = viewProfileBinding.etEdtCompanyName.getText().toString().trim();
        if (TextUtils.isEmpty(strName)) {
            CustomDialog.getInstance().showAlert(this, Util.getAppKeyValue(this, R.string.str_enter_name), MessageHelper.getInstance().getAppMessage(getString(R.string.str_validation_dialog_header)), Util.getAppKeyValue(this, R.string.lblOk), Util.getAppKeyValue(this, R.string.str_dismiss), false);
//            CustomDialog.getInstance().showAlert(this, Util.getAppKeyValue(this, R.string.str_enter_Name), Util.getAppKeyValue(this, R.string.lblOk));
            return false;
        } else if (TextUtils.isEmpty(strEmail)) {
            CustomDialog.getInstance().showAlert(this, Util.getAppKeyValue(this, R.string.str_enter_email), MessageHelper.getInstance().getAppMessage(getString(R.string.str_validation_dialog_header)), Util.getAppKeyValue(this, R.string.lblOk), Util.getAppKeyValue(this, R.string.str_dismiss), false);
            return false;
        } else if (!ValidationClass.isValidEmail(strEmail)) {
            CustomDialog.getInstance().showAlert(this, Util.getAppKeyValue(this, R.string.str_valid_email), MessageHelper.getInstance().getAppMessage(getString(R.string.str_validation_dialog_header)), Util.getAppKeyValue(this, R.string.lblOk), Util.getAppKeyValue(this, R.string.str_dismiss), false);
            return false;
        }
//        else if (TextUtils.isEmpty(strCompany)) {
//            CustomDialog.getInstance().showAlert(this,Util.getAppKeyValue(this, R.string.str_enter_company),"Error", Util.getAppKeyValue(this, R.string.lblOk), Util.getAppKeyValue(this, R.string.str_dismiss),false);
//            return false;
//        }
        else {
            return true;
//            callRegisterApi();
        }

    }


    public void showProgressBar(Context context) {
        dialog = new Dialog(context, R.style.DialogTheme);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setContentView(R.layout.custom_progressbar);

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
