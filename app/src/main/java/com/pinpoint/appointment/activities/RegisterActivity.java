package com.pinpoint.appointment.activities;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;

import androidx.databinding.DataBindingUtil;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;

import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.pinpoint.appointment.BaseApplication;
import com.pinpoint.appointment.BaseConstants;
import com.pinpoint.appointment.R;
import com.pinpoint.appointment.adapter.AdpPlacesNew;
import com.pinpoint.appointment.adapter.PlaceArrayAdapter;
import com.pinpoint.appointment.api.ApiList;
import com.pinpoint.appointment.api.RequestCode;
import com.pinpoint.appointment.customviews.CustomDialog;
import com.pinpoint.appointment.customviews.GenericView;
import com.pinpoint.appointment.graphicsUtils.GraphicsUtil;
import com.pinpoint.appointment.helper.MessageHelper;
import com.pinpoint.appointment.helper.PrefHelper;
import com.pinpoint.appointment.helper.ToastHelper;
import com.pinpoint.appointment.interfaces.DataObserver;
import com.pinpoint.appointment.models.CustomerDetails;
//import com.pinpoint.appointment.models.Places;
import com.pinpoint.appointment.permissionUtils.PermissionClass;
import com.pinpoint.appointment.utils.Debug;
import com.pinpoint.appointment.utils.Util;
import com.pinpoint.appointment.databinding.ActivityRegisterBinding;
import com.pinpoint.appointment.validator.ValidationClass;
import com.theartofdev.edmodo.cropper.CropImage;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

public class RegisterActivity extends GlobalActivity implements GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks {

    public static final int REQUEST_CAMERA = 0;
    public static final int REQUEST_GALLERY = 1;
    public static final int PICK_FILE_RESULT_CODE = 3;
    private final int REQUEST_CROPPED_IMAGE = 2;
    private String imageUrl = "", selectedImagePath;

    public ImageView iv_back, iv_settings;
    public TextView tv_header;
    boolean iscnfPwdVisible = false, isPwdVisible = false;
    private static final String LOG_TAG = "MainActivity";
    private static final int GOOGLE_API_CLIENT_ID = 0;
    private GoogleApiClient mGoogleApiClient;
    private PlaceArrayAdapter mPlaceArrayAdapter;
    private static final LatLngBounds BOUNDS_MOUNTAIN_VIEW = new LatLngBounds(
            new LatLng(37.398160, -122.180831), new LatLng(37.430610, -121.972090));

    ActivityRegisterBinding registerBinding;
    CustomerDetails customerDetails;
    String strName, strEmail, strPassword, strConfirmPassWord, strCompanyName;
    private AutoCompleteTextView mAcTxtCityState;
    AdpPlacesNew placeAdapter;
    private int[] screenWH;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        registerBinding = DataBindingUtil.setContentView(this, R.layout.activity_register);
        customerDetails = new CustomerDetails();


        if (Util.isTabletDevice(RegisterActivity.this)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        Util.setupOutSideTouchHideKeyboard(GenericView.findViewById(this, R.id.rl_parent));
        mAcTxtCityState = registerBinding.acTxtCityState;
        screenWH = GraphicsUtil.getScreenWidthHeight();
        mGoogleApiClient = new GoogleApiClient.Builder(RegisterActivity.this)
                .addApi(Places.GEO_DATA_API)
                .enableAutoManage(this, GOOGLE_API_CLIENT_ID, this)
                .addConnectionCallbacks(this)
                .build();

        setupLocationAutocomplete();

        iv_back = findViewById(R.id.iv_imgMenu_Back);
        iv_settings = findViewById(R.id.iv_imgSetting);
        tv_header = findViewById(R.id.tv_txtTitle);
        tv_header.setText(MessageHelper.getInstance().getAppMessage(getString(R.string.str_signup_header)));
        tv_header.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_SEMIBOLD));

        iv_back.setImageResource(R.mipmap.ic_back);
        iv_back.setVisibility(View.VISIBLE);
        iv_settings.setVisibility(View.GONE);

        registerBinding.etEdtName.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));
        registerBinding.etEdtConfirmPassword.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));
        registerBinding.etEdtPassword.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));
        registerBinding.etEdtCompanyName.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));
        registerBinding.etEdtEmail.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));

        registerBinding.inputLayoutName.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));
        registerBinding.inputLayoutConfirmPassword.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));
        registerBinding.inputLayoutCompanyName.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));
        registerBinding.inputLayoutEmail.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));
        registerBinding.inputLayoutPassword.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));

        registerBinding.inputLayoutName.setHint(MessageHelper.getInstance().getAppMessage(getString(R.string.str_name)));
        registerBinding.inputLayoutConfirmPassword.setHint(MessageHelper.getInstance().getAppMessage(getString(R.string.str_confirm_passowrd)));
        registerBinding.inputLayoutCompanyName.setHint(MessageHelper.getInstance().getAppMessage(getString(R.string.str_company_name)));
        registerBinding.inputLayoutEmail.setHint(MessageHelper.getInstance().getAppMessage(getString(R.string.str_email)));
        registerBinding.inputLayoutPassword.setHint(MessageHelper.getInstance().getAppMessage(getString(R.string.str_password)));

        registerBinding.btSignup.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_SEMIBOLD));
        registerBinding.btSignup.setText(MessageHelper.getInstance().getAppMessage(getString(R.string.str_signup)));

        registerBinding.tvTxtTermsLine.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));
        registerBinding.tvTxtTermsLine.setText(MessageHelper.getInstance().getAppMessage(getString(R.string.str_tnc_line)));

        registerBinding.tvTermsOfService.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));
        registerBinding.tvTermsOfService.setText(MessageHelper.getInstance().getAppMessage(getString(R.string.str_terms_of_service)));

        registerBinding.tvAnd.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));
        registerBinding.tvAnd.setText(MessageHelper.getInstance().getAppMessage(getString(R.string.str_and)));

        registerBinding.tvTxtPolicy.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));
        registerBinding.tvTxtPolicy.setText(MessageHelper.getInstance().getAppMessage(getString(R.string.str_privacy_policy)));

        registerBinding.tvTxtBackToLogin.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));
        registerBinding.tvTxtBackToLogin.setText(MessageHelper.getInstance().getAppMessage(getString(R.string.str_login)));

        registerBinding.tvTxtTermsLine.setAllCaps(true);
        registerBinding.tvAnd.setAllCaps(true);
        registerBinding.tvTxtBackToLogin.setAllCaps(true);

        registerBinding.etEdtCompanyName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
//                  Utils.getInstance().hideKeyboard(mEdtPassword);
                    if (checkValidation()) {
                        CustomerDetails.chekEmailExist(RegisterActivity.this, dataObserver, strEmail);
                    }
                    return true;
                }
                return false;
            }
        });
    }

    DataObserver dataObserver = new DataObserver() {
        @Override
        public void OnSuccess(RequestCode requestCode, Object object) {
            switch (requestCode) {
                case loginCustomer:
                    CustomDialog.getInstance().hide();
                    String response = object.toString();
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        JSONObject jsonObject1 = jsonObject.getJSONObject(ApiList.KEY_RESPONSE);
                        String message = jsonObject1.getString(ApiList.KEY_MESSAGE);
                        ToastHelper.displayCustomToast(message);
                        Intent i = new Intent(RegisterActivity.this, HomeActivity.class);
                        startActivity(i);
                        finish();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    break;
                case checkemailexist:
                    CustomDialog.getInstance().hide();
                    String response1 = object.toString();
                    try {
                        JSONObject responseJson = new JSONObject(object.toString());
                        String message = responseJson.getString(ApiList.KEY_MESSAGE);
                        //                    CustomDialog.getInstance().showAlert();
                        CustomDialog.getInstance().showAlert(RegisterActivity.this, message, MessageHelper.getInstance().getAppMessage(getString(R.string.str_fail_dialog_header))
                                , MessageHelper.getInstance().getAppMessage(getString(R.string.str_ok))
                                , MessageHelper.getInstance().getAppMessage(getString(R.string.str_dismiss))
                                , false);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    //                ToastHelper.displayCustomToast(response.);
                    break;
            }
        }

        @Override
        public void OnFailure(RequestCode requestCode, String errorCode, String error) {
            CustomDialog.getInstance().hide();
        }

        @Override
        public void onOtherStatus(RequestCode requestCode, Object object) {

            switch (requestCode) {
                case checkemailexist:
                    CustomDialog.getInstance().hide();
//            {"response":{"result":[],"status":"notexist","message":"Email not exist"}}
                    String response = object.toString();
                    try {
                        JSONObject responseJson = new JSONObject(object.toString());
                        redirectToPhoneAuthentication();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
//                ToastHelper.displayCustomToast(response.);
                    break;
            }
        }

        @Override
        public void onRetryRequest(RequestCode requestCode) {

        }
    };

    private void redirectToPhoneAuthentication() {
        Intent ina = new Intent(RegisterActivity.this, FirebasePhoneNumAuthActivity.class);
        ina.putExtra(BaseConstants.KEY_FROM, "register");
        ina.putExtra(ApiList.KEY_NAME, strName);
        ina.putExtra(ApiList.KEY_EMAIL, strEmail);
        ina.putExtra(ApiList.KEY_PASSWORD, strPassword);
        ina.putExtra(ApiList.KEY_COMPANY, strCompanyName);
        ina.putExtra(ApiList.KEY_IMAGE, selectedImagePath);
        startActivity(ina);
    }

    private void setupLocationAutocomplete() {
        mAcTxtCityState = registerBinding.acTxtCityState;
        mAcTxtCityState.setThreshold(1);
        AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS)
                .setCountry("US")
                .build();
        mPlaceArrayAdapter = new PlaceArrayAdapter(this, android.R.layout.simple_list_item_1,
                BOUNDS_MOUNTAIN_VIEW, typeFilter);
        mAcTxtCityState.setAdapter(mPlaceArrayAdapter);


        try {

        } catch (Exception Ex) {
            Ex.printStackTrace();
        }
    }

    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback
            = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                Log.e(LOG_TAG, "Place query did not complete. Error: " +
                        places.getStatus().toString());
                return;
            }
            // Selecting the first object buffer.
            final Place place = places.get(0);
            CharSequence attributions = places.getAttributions();


        }
    };


    @Override
    public void onConnected(Bundle bundle) {
        mPlaceArrayAdapter.setGoogleApiClient(mGoogleApiClient);
        Log.i(LOG_TAG, "Google Places API connected.");

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(LOG_TAG, "Google Places API connection failed with error code: "
                + connectionResult.getErrorCode());

        Toast.makeText(this,
                "Google Places API connection failed with error code:" +
                        connectionResult.getErrorCode(),
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mPlaceArrayAdapter.setGoogleApiClient(null);
        Log.e(LOG_TAG, "Google Places API connection suspended.");
    }

    private boolean checkValidation() {
        strName = registerBinding.etEdtName.getText().toString().trim();
        strEmail = registerBinding.etEdtEmail.getText().toString().trim();
        strPassword = registerBinding.etEdtPassword.getText().toString().trim();
        strConfirmPassWord = registerBinding.etEdtConfirmPassword.getText().toString().trim();
        strCompanyName = registerBinding.etEdtCompanyName.getText().toString().trim();

        if (TextUtils.isEmpty(strName)) {
            CustomDialog.getInstance().showAlert(this, MessageHelper.getInstance().getAppMessage(getString(R.string.str_enter_name)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_validation_dialog_header)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_ok)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_dismiss)), false);
            return false;
        } else if (strName.length() < 3) {
            CustomDialog.getInstance().showAlert(this, "Name should have minimum 3 characters long", MessageHelper.getInstance().getAppMessage(getString(R.string.str_validation_dialog_header)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_ok)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_dismiss)), false);
            return false;
        } else if (strName.length() > 32) {
            CustomDialog.getInstance().showAlert(this, "Name should not exceed 32 characters long", MessageHelper.getInstance().getAppMessage(getString(R.string.str_validation_dialog_header)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_ok)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_dismiss)), false);
            return false;
        } else if (TextUtils.isEmpty(strEmail)) {
            CustomDialog.getInstance().showAlert(this, MessageHelper.getInstance().getAppMessage(getString(R.string.str_enter_email)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_validation_dialog_header)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_ok)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_dismiss)), false);

            return false;
        } else if (!ValidationClass.isValidEmail(strEmail)) {
            CustomDialog.getInstance().showAlert(this, MessageHelper.getInstance().getAppMessage(getString(R.string.str_valid_email)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_validation_dialog_header)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_ok)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_dismiss)), false);

            return false;
        } else if (TextUtils.isEmpty(strPassword)) {
            CustomDialog.getInstance().showAlert(this, MessageHelper.getInstance().getAppMessage(getString(R.string.str_enter_pwd)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_validation_dialog_header)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_ok)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_dismiss)), false);

            return false;
        } else if (strPassword.length() < 6) {
            CustomDialog.getInstance().showAlert(this, "Please enter minimum 6 character long password", MessageHelper.getInstance().getAppMessage(getString(R.string.str_validation_dialog_header)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_ok)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_dismiss)), false);
            return false;
        } else if (!strPassword.equalsIgnoreCase(strConfirmPassWord)) {
            CustomDialog.getInstance().showAlert(this, MessageHelper.getInstance().getAppMessage(getString(R.string.str_confirmpassword_validation)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_validation_dialog_header)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_ok)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_dismiss)), false);

            return false;
        } else if (!registerBinding.chkTerms.isChecked()) {
            CustomDialog.getInstance().showAlert(this, "Please accept the terms of service and privacy policy to proceed.", MessageHelper.getInstance().getAppMessage(getString(R.string.str_validation_dialog_header)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_ok)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_dismiss)), false);
            return false;
        } else {
            return true;
        }

    }


    @Override
    public void onClickEvent(View view) {
//        super.onClickEvent(view);
        switch (view.getId()) {
            case R.id.tv_txtBackToLogin:
                redirectToLogin();

                break;
            case R.id.lnr_tnc:
                if (registerBinding.chkTerms.isChecked()) {
                    registerBinding.chkTerms.setChecked(false);
                } else {
                    registerBinding.chkTerms.setChecked(true);
                }
                break;
            case R.id.tv_txtTermsLine:
                if (registerBinding.chkTerms.isChecked()) {
                    registerBinding.chkTerms.setChecked(false);
                } else {
                    registerBinding.chkTerms.setChecked(true);
                }

                break;
            case R.id.bt_signup:
                if (checkValidation()) {
//                    redirectToPhoneAuthentication();
                    CustomerDetails.chekEmailExist(RegisterActivity.this, dataObserver, strEmail);
                }
                break;

            case R.id.iv_imgMenu_Back:
                onBackPressed();
                break;

            case R.id.iv_imgVisible:
                if (isPwdVisible) {
                    isPwdVisible = false;
                    registerBinding.etEdtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    registerBinding.ivImgVisible.setImageResource(R.mipmap.ic_eye);

                } else {
                    isPwdVisible = true;
                    registerBinding.etEdtPassword.setInputType(InputType.TYPE_CLASS_TEXT);
                    registerBinding.ivImgVisible.setImageResource(R.mipmap.ic_hide);
                }
                break;
            case R.id.iv_imgcnfVisible:
                if (iscnfPwdVisible) {
                    iscnfPwdVisible = false;
                    registerBinding.etEdtConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    registerBinding.ivImgcnfVisible.setImageResource(R.mipmap.ic_eye);
                } else {
                    iscnfPwdVisible = true;
                    registerBinding.etEdtConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT);
                    registerBinding.ivImgcnfVisible.setImageResource(R.mipmap.ic_hide);
                }

                break;

            case R.id.iv_imgSignup:

                if (PermissionClass.checkPermission(RegisterActivity.this, PermissionClass.REQUEST_CODE_RUNTIME_PERMISSION_DOCUMENT,
                        Collections.singletonList(Manifest.permission.WRITE_EXTERNAL_STORAGE))) {
//                    openFileTypeDialog();
                    selectImage();
                }
                break;

            case R.id.tv_termsOfService:
                String termsurl = PrefHelper.getString(PrefHelper.KEY_TERMS_URL, "");
                Intent ina = new Intent(RegisterActivity.this, WebViewDisplayActivity.class);
                ina.putExtra(BaseConstants.KEY_ADDRESS, termsurl);
                ina.putExtra(ApiList.KEY_NAME, MessageHelper.getInstance().getAppMessage(getString(R.string.str_terms_of_service)));
                startActivity(ina);
                break;

            case R.id.tv_txtPolicy:
                String privacyurl = PrefHelper.getString(PrefHelper.KEY_PRIVACY_POLICY, "");
                Intent inb = new Intent(RegisterActivity.this, WebViewDisplayActivity.class);
                inb.putExtra(BaseConstants.KEY_ADDRESS, privacyurl);
                inb.putExtra(ApiList.KEY_NAME, MessageHelper.getInstance().getAppMessage(getString(R.string.str_privacy_policy)));
                startActivity(inb);
                break;


        }
    }


    private void redirectToLogin() {
        Intent ina = new Intent(this, LoginActivity.class);
        startActivity(ina);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
        finish();
    }

    public void selectImage() {

        final int REQUEST_CANCEL = 2;

        final CharSequence[] items = {MessageHelper.getInstance().getAppMessage(getString(R.string.str_click_photo))
                ,
                MessageHelper.getInstance().getAppMessage(getString(R.string.str_open_gallery))
                ,
                MessageHelper.getInstance().getAppMessage(getString(R.string.str_cancel))
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
        builder.setTitle(MessageHelper.getInstance().getAppMessage(getString(R.string.str_select_profile_pic)));
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {

                switch (item) {
                    case REQUEST_CAMERA:
                        if (PermissionClass.checkPermission(RegisterActivity.this, PermissionClass.REQUEST_CODE_RUNTIME_PERMISSION_CAMERA,
                                Arrays.asList(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE))) {

                            BaseConstants.CAPTUREDFILEURI = GraphicsUtil.getOutputMediaFileUri(GraphicsUtil.CAPTURED_DIRECTORY_NAME,
                                    GraphicsUtil.MEDIA_IMAGE);
                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, BaseConstants.CAPTUREDFILEURI);
                            startActivityForResult(intent, REQUEST_CAMERA);

                        }
                        break;
                    case REQUEST_GALLERY:
                        if (PermissionClass.checkPermission(RegisterActivity.this, PermissionClass.REQUEST_CODE_RUNTIME_PERMISSION_STORAGE,
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
                    String Message = "From: RegisterActivity , After Image cropped : Successfully";
//                    LeadDetails.setMessage(RegisterActivity.this, Message);
                    Uri resultUri = result.getUri();
                    File cropFile = new File(resultUri.getPath());

                    if (!TextUtils.isEmpty(cropFile.toString())) {

                        if (cropFile.exists())
                            selectedImagePath = cropFile.getAbsolutePath();

                        if (selectedImagePath != null) {

                            registerBinding.ivImgSignup.setImageBitmap(BitmapFactory.decodeFile(selectedImagePath));
                        } else {
                            ToastHelper.displayInfo(MessageHelper.getInstance().getAppMessage(getString(R.string.str_msg_image_pick_issue)));
                        }
                    } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                        String Messagea = "From: RegisterActivity , After Image cropped : " + result.getError().toString();
//                        LeadDetails.setMessage(RegisterActivity.this, Messagea);
                        Exception error = result.getError();
                    }
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
        Intent intent = new Intent(this, CropImageActivity.class);
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
            int length = 500;
            if (imageWidth < imageHeight) {
                length = imageWidth - 100;
            } else {
                length = imageHeight - 100;
            }
            String Message = "From: Registration Activity , On image selected from gallery";
//            LeadDetails.setMessage(RegisterActivity.this, Message);
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
                registerBinding.ivImgSignup.setImageBitmap(BitmapFactory.decodeFile(selectedImagePath));


            } else {
                ToastHelper.displayInfo(MessageHelper.getInstance().getAppMessage(getString(R.string.str_msg_image_pick_issue)));
            }

            Debug.trace("resize wh " + "screenWidth " + screenWH[0] + " " + "screenHeight " + screenWH[0] * BaseConstants.VALUE_IMAGE_RATIO);
        }

    }


    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        redirectToLogin();
//        finish();
    }


}
