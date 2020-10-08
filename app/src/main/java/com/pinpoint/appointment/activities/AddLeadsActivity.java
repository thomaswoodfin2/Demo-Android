package com.pinpoint.appointment.activities;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import androidx.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;


import com.pinpoint.appointment.BaseActivity;
import com.pinpoint.appointment.BaseApplication;
import com.pinpoint.appointment.BaseConstants;
import com.pinpoint.appointment.R;
import com.pinpoint.appointment.api.ApiList;
import com.pinpoint.appointment.api.RequestCode;
import com.pinpoint.appointment.customviews.CustomDialog;
import com.pinpoint.appointment.customviews.GenericView;
import com.pinpoint.appointment.databinding.FragmentAddLeadBinding;
import com.pinpoint.appointment.graphicsUtils.GraphicsUtil;
import com.pinpoint.appointment.helper.MessageHelper;
import com.pinpoint.appointment.helper.PrefHelper;
import com.pinpoint.appointment.helper.ToastHelper;
import com.pinpoint.appointment.interfaces.ClickEvent;
import com.pinpoint.appointment.interfaces.DataObserver;
import com.pinpoint.appointment.models.LeadDetails;
import com.pinpoint.appointment.permissionUtils.PermissionClass;
import com.pinpoint.appointment.utils.Debug;
import com.pinpoint.appointment.utils.Timer;
import com.pinpoint.appointment.utils.Util;
import com.pinpoint.appointment.validator.ValidationClass;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;


/**
 * A simple {@link Fragment} subclass.
 */
public class AddLeadsActivity extends BaseActivity implements ClickEvent {
    private AddLeadsActivity parent;
    private Bundle bundle;
    FragmentAddLeadBinding addLeadBinding;
    String strName, strPhone, strEmail, strCompany;
    int selectedTimeframe = 1;
    private static final int GOOGLE_API_CLIENT_ID = 0;
    LeadDetails leadDetails = new LeadDetails();
    int propertyId = 0;
    Preview preview;
    private static  final int SETTINGS_DIALOG = 1235;
    private int[] screenWH;
    private final int REQUEST_CAMERA = 0;
    private final int REQUEST_GALLERY = 1;
    private final int PICK_FILE_RESULT_CODE = 3;
    private final int REQUEST_CROPPED_IMAGE = 2;
    private String selectedImagePath;

    public static final int DONE = 1;
    public static final int NEXT = 2;
    public static final int PERIOD = 10;
    private Camera camera;
    private int cameraId = 0;
    private ImageView display;
    private Timer timer;


    public AddLeadsActivity() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addLeadBinding = DataBindingUtil.setContentView(this, R.layout.fragment_add_lead);

        ImageView imgBack = GenericView.findViewById(this, R.id.iv_imgMenu_Back);
        ImageView imgSettings = GenericView.findViewById(this, R.id.iv_imgSetting);
        TextView tvHeader = GenericView.findViewById(this, R.id.tv_txtTitle);
        tvHeader.setText(MessageHelper.getInstance().getAppMessage(getString(R.string.str_open_house_registry)));
        screenWH = GraphicsUtil.getScreenWidthHeight();
        imgBack.setImageResource(R.mipmap.ic_back);
        imgSettings.setVisibility(View.INVISIBLE);
        Util.setupOutSideTouchHideKeyboard(GenericView.findViewById(this, R.id.scrl_container));
        tvHeader.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_SEMIBOLD));
        addLeadBinding.etEdtPhone.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));
        addLeadBinding.etEdtName.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));
        addLeadBinding.edtEmail.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));
        addLeadBinding.etEdtCompanyName.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));
//      addLeadBinding.etEdtPhone.setRawInputType(Configuration.KEYBOARD_12KEY);
        addLeadBinding.etEdtPhone.setTransformationMethod(null);
        addLeadBinding.inputLayoutName.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));
        addLeadBinding.inputLayoutEmail.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));
        addLeadBinding.inputLayoutCompanyName.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));
        addLeadBinding.inputLayoutPhone.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));
        addLeadBinding.tvBuyingtimeframe.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));
        addLeadBinding.tvAnd.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));
        addLeadBinding.tvPrivacy.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));
        addLeadBinding.tvRealEstate.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));
        addLeadBinding.tvTncBlue.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));
        addLeadBinding.tvTnc.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));

        addLeadBinding.inputLayoutName.setHint(MessageHelper.getInstance().getAppMessage(getString(R.string.str_name)));
        addLeadBinding.inputLayoutPhone.setHint(MessageHelper.getInstance().getAppMessage(getString(R.string.str_phone)));
        addLeadBinding.inputLayoutCompanyName.setHint(MessageHelper.getInstance().getAppMessage(getString(R.string.str_name_or_company)));
        addLeadBinding.inputLayoutEmail.setHint(MessageHelper.getInstance().getAppMessage(getString(R.string.str_email)));

        addLeadBinding.tvAnd.setText(MessageHelper.getInstance().getAppMessage(getString(R.string.str_and)));
        addLeadBinding.tvBuyingtimeframe.setText(MessageHelper.getInstance().getAppMessage(getString(R.string.str_buying_timeframe)));
        addLeadBinding.tvTncBlue.setText(MessageHelper.getInstance().getAppMessage(getString(R.string.str_terms_of_service)));
        addLeadBinding.tvTnc.setText(MessageHelper.getInstance().getAppMessage(getString(R.string.str_tnc_line)));
        addLeadBinding.tvTnc.setVisibility(View.GONE);
        addLeadBinding.tvPrivacy.setText(MessageHelper.getInstance().getAppMessage(getString(R.string.str_privacy_policy)));
        addLeadBinding.tvRealEstate.setText(MessageHelper.getInstance().getAppMessage(getString(R.string.str_real_estate)));
        addLeadBinding.btSignup.setText(MessageHelper.getInstance().getAppMessage(getString(R.string.str_submit)));
        addLeadBinding.btSignup.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_SEMIBOLD));
        addLeadBinding.tvClickingStart.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));
        addLeadBinding.tvClickingStart.setText(MessageHelper.getInstance().getAppMessage(getString(R.string.str_clicking_start)));
        addLeadBinding.tvWelcome.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_SEMIBOLD));
        addLeadBinding.tvWelcome.setText(MessageHelper.getInstance().getAppMessage(getString(R.string.str_welcome)));
        parent = this;
//      addLeadBinding.rlCompanyName.setVisibility(View.GONE);
        if(getIntent()!=null&&getIntent().hasExtra(BaseConstants.KEY_PROPERTY_ID)) {
            propertyId = getIntent().getIntExtra(BaseConstants.KEY_PROPERTY_ID, 0);
        }
        addLeadBinding.etEdtPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String text = addLeadBinding.etEdtPhone.getText().toString();
                int textLength = addLeadBinding.etEdtPhone.getText().length();

                if (text.endsWith("-") || text.endsWith(" ") || text.endsWith(" "))
                    return;

                if (textLength == 1) {
                    if (!text.contains("(")) {
                        addLeadBinding.etEdtPhone.setText(new StringBuilder(text).insert(text.length() - 1, "(").toString());
                        addLeadBinding.etEdtPhone.setSelection(addLeadBinding.etEdtPhone.getText().length());
                    }

                } else if (textLength == 5) {

                    if (!text.contains(")")) {
                        addLeadBinding.etEdtPhone.setText(new StringBuilder(text).insert(text.length() - 1, ")").toString());
                        addLeadBinding.etEdtPhone.setSelection(addLeadBinding.etEdtPhone.getText().length());
                    }

                } else if (textLength == 6) {
                    addLeadBinding.etEdtPhone.setText(new StringBuilder(text).insert(text.length() - 1, " ").toString());
                    addLeadBinding.etEdtPhone.setSelection(addLeadBinding.etEdtPhone.getText().length());

                } else if (textLength == 10) {
                    if (!text.contains("-")) {
                        addLeadBinding.etEdtPhone.setText(new StringBuilder(text).insert(text.length() - 1, "-").toString());
                        addLeadBinding.etEdtPhone.setSelection(addLeadBinding.etEdtPhone.getText().length());
                    }
                } else if (textLength == 15) {
                    if (text.contains("-")) {
                        addLeadBinding.etEdtPhone.setText(new StringBuilder(text).insert(text.length() - 1, "-").toString());
                        addLeadBinding.etEdtPhone.setSelection(addLeadBinding.etEdtPhone.getText().length());
                    }
                }else if (textLength == 18) {
                    if (text.contains("-")) {
                        addLeadBinding.etEdtPhone.setText(new StringBuilder(text).insert(text.length() - 1, "-").toString());
                        addLeadBinding.etEdtPhone.setSelection(addLeadBinding.etEdtPhone.getText().length());
                    }
                }
//                else if (textLength == 20) {
//                    Intent i = new Intent(MainActivity.this, Activity2.class);
//                    startActivity(i);
//
//                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        try {
            addLeadBinding.rgYesNo.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup radioGroup, int i) {
                    if (addLeadBinding.rbYes.isChecked()) {
                        addLeadBinding.rlCompanyName.setVisibility(View.VISIBLE);
                    } else {
                        addLeadBinding.rlCompanyName.setVisibility(View.GONE);
                    }
                }
            });

//            addLeadBinding.switchRealestate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//                @Override
//                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//                    if(b)
//                    {
//                        addLeadBinding.rlCompanyName.setVisibility(View.VISIBLE);
//                    }
//                    else
//                    {
//                        addLeadBinding.rlCompanyName.setVisibility(View.GONE);
//                    }
//                }
//            });
        } catch (NullPointerException ex) {
        }
        selectedImagePath="";

//        addLeadBinding.etEdtPhone.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
//                if (actionId == EditorInfo.IME_ACTION_DONE) {
////                    Utils.getInstance().hideKeyboard(mEdtPassword);
//                    callAPI();
//
//                    return true;
//                }
//                return false;
//            }
//        });


//        setupcamera();

    }

    private void setupcamera() {
//        CustomDialog.getInstance().showProgressBar(AddLeadsActivity.this);
//        display=(ImageView)findViewById(R.id.iv_imgProeprt);
        BaseConstants.CAPTUREDFILEURI = GraphicsUtil.getOutputMediaFileUri(GraphicsUtil.CAPTURED_DIRECTORY_NAME,
                GraphicsUtil.MEDIA_IMAGE);
        // do we have a camera?
        if (!getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            ToastHelper.displayCustomToast(MessageHelper.getInstance().getAppMessage(getString(R.string.no_camera_found)));
        } else {
            cameraId = findFrontFacingCamera();
            if (cameraId < 0) {
//                Toast.makeText(this, "No front facing camera found.",
//                        Toast.LENGTH_LONG).show();
//                safeCameraOpen(0);
            } else {
                try {
                    ToastHelper.displayCustomToast("Camera Id" + cameraId);
                    safeCameraOpen(cameraId);
                } catch (Exception ex) {
                    CustomDialog.getInstance().hide();
                    CustomDialog.getInstance().showAlert(AddLeadsActivity.this, ex.toString(), ex.toString(), Util.getAppKeyValue(AddLeadsActivity.this, R.string.lblOk), Util.getAppKeyValue(AddLeadsActivity.this, R.string.str_dismiss), false);

                    replaceStartView();
                }
            }
        }
        // THIS IS JUST A FAKE SURFACE TO TRICK THE CAMERA PREVIEW
        // http://stackoverflow.com/questions/17859777/how-to-take-pictures-in-android-
        // application-without-the-user-interface
        SurfaceView view = new SurfaceView(this);
        try {
            camera.setPreviewDisplay(view.getHolder());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        camera.startPreview();
        Camera.Parameters params = camera.getParameters();
        params.setJpegQuality(100);
        camera.setParameters(params);
        // We need something to trigger periodically the capture of a
        // picture to be processed
        timer = new Timer(getApplicationContext(), threadHandler);
        timer.execute();
    }

    private Handler threadHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            try {
                switch (msg.what) {
                    case DONE:
                        // Trigger camera callback to take pic
                        camera.takePicture(null, null, mCall);
                        break;
                    case NEXT:
                        timer = new Timer(getApplicationContext(), threadHandler);
                        timer.execute();
                        break;
                }
            } catch (Exception ex) {
                CustomDialog.getInstance().hide();
                CustomDialog.getInstance().showAlert(AddLeadsActivity.this, ex.toString(), ex.toString(), Util.getAppKeyValue(AddLeadsActivity.this, R.string.lblOk), Util.getAppKeyValue(AddLeadsActivity.this, R.string.str_dismiss), false);
                replaceStartView();
            }
        }
    };

    private Bitmap scaleDownBitmapImage(Bitmap bitmap, int newWidth, int newHeight) {
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
        return resizedBitmap;
    }

    Camera.PictureCallback mCall = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            //decode the data obtained by the camera into a Bitmap
            //display.setImageBitmap(photo);
            FileOutputStream out = null;
            try {
                Bitmap bitmapPicture
                        = BitmapFactory.decodeByteArray(data, 0, data.length);
                Bitmap finalBitmap = scaleDownBitmapImage(bitmapPicture, 500, 500);


//            try
//            {
//                File docsFolder = new File(Environment.getExternalStorageDirectory() + "/Documents");
//                boolean isPresent = true;
//                if (!docsFolder.exists()) {
//                    isPresent = docsFolder.mkdir();
//                }
//                if (isPresent)
//                {
//                    if(file==null)
//                    file = new File(docsFolder.getAbsolutePath(),"test.jpg");
//                }
//                else
//                {
//                    // Failure
//                }
                File file1 = new File(BaseConstants.CAPTUREDFILEURI.getPath());
                out = new FileOutputStream(file1);
                finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out); // bmp is your Bitmap instance
                selectedImagePath = file1.getAbsolutePath();
//                addLeadBinding.ivImgProeprt.setImageBitmap(BitmapFactory.decodeFile(selectedImagePath));
                CustomDialog.getInstance().hide();
                replaceStartView();
                // PNG is a lossless format, the compression factor (100) is ignored
            } catch (Exception e) {
                CustomDialog.getInstance().hide();
                CustomDialog.getInstance().showAlert(AddLeadsActivity.this, e.toString(), e.toString(), Util.getAppKeyValue(AddLeadsActivity.this, R.string.lblOk), Util.getAppKeyValue(AddLeadsActivity.this, R.string.str_dismiss), false);
//                FileOutputStream out = null;
                e.printStackTrace();
            } finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
//            display.setImageBitmap(bitmapPicture);
//            Message.obtain(threadHandler, AddLeadsActivity.NEXT, "").sendToTarget();
            //Log.v("MyActivity","Length: "+data.length);
        }
    };

    private int findFrontFacingCamera() {
        int cameraId = 0;
        // Search for the front facing camera
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                Log.v("MyActivity", "Camera found");
                cameraId = i;
                break;
            }
        }
        return cameraId;
    }

    @Override
    protected void onPause() {
        if (timer != null) {
            timer.cancel(true);
        }
        releaseCamera();
        super.onPause();
    }

    private boolean safeCameraOpen(int id) {
        boolean qOpened = false;
        try {
            releaseCamera();
            camera = Camera.open(id);
            qOpened = (camera != null);
        } catch (Exception e) {
            replaceStartView();

            CustomDialog.getInstance().hide();
            CustomDialog.getInstance().showAlert(AddLeadsActivity.this, e.toString(), e.toString(), Util.getAppKeyValue(AddLeadsActivity.this, R.string.lblOk), Util.getAppKeyValue(AddLeadsActivity.this, R.string.str_dismiss), false);
        }
        return qOpened;
    }

    private void releaseCamera() {
        if (camera != null) {
            camera.lock();
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    //    private void releaseCameraAndPreview() {
//        myCameraPreview.setCamera(null);
//        if (mCamera != null) {
//            mCamera.release();
//            mCamera = null;
//        }
//    }

    private void callAPI() {
        if (checkValidation()) {
            if (addLeadBinding.rbMonth1.isChecked()) {
                selectedTimeframe = 1;
            } else if (addLeadBinding.rbMonth2.isChecked()) {
                selectedTimeframe = 2;
            } else {
                selectedTimeframe = 3;
            }
            leadDetails.setDuration(String.valueOf(selectedTimeframe));
            leadDetails.setPId(String.valueOf(propertyId));
//            String phonenumber=leadDetails.getContact().toString().replaceAll("[^\\d]", "");
            String orignalPhoneNumber=leadDetails.getContact().toString();
            String phonenumber=orignalPhoneNumber.replace("(","");
            phonenumber=phonenumber.replace(")","");
            phonenumber=phonenumber.replace(" ","");
            phonenumber=phonenumber.replace("-","");
            leadDetails.setContact(phonenumber);
            LeadDetails.addLead(AddLeadsActivity.this, leadDetails, selectedImagePath, true, dataObserver);
        }
    }

    DataObserver dataObserver = new DataObserver() {
        @Override
        public void OnSuccess(RequestCode requestCode, Object object) {
            switch (requestCode) {
                case addlead:
                    CustomDialog.getInstance().hide();
                    String response = object.toString();
                    try {
                        JSONObject jsonObject = new JSONObject(response);
//                      JSONObject jsonObject1=jsonObject.getJSONObject(ApiList.KEY_RESPONSE);
                        String message = jsonObject.getString(ApiList.KEY_MESSAGE);
//                        ToastHelper.displayCustomToast(message);
                        final Handler handler = new Handler();
                        CustomDialog.getInstance().showAlertWithButtonClick(AddLeadsActivity.this, message
                                , MessageHelper.getInstance().getAppMessage(getString(R.string.str_app_name))
                                , MessageHelper.getInstance().getAppMessage(getString(R.string.str_ok))
                                ,
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        if(handler!=null)
                                        {
                                            handler.removeCallbacksAndMessages(null);
                                        }
                                        CustomDialog.getInstance().hide();
                                        BaseConstants.REFRESH_PROPERTIES = true;
                                        restartActivity(AddLeadsActivity.this);
//                                        CustomDialog.getInstance().hide();
//                                        PrefHelper.setBoolean("isnotification", false);
//                                        BaseConstants.REFRESH_PROPERTIES = true;
//                                        finish();
                                    }
                                }, MessageHelper.getInstance().getAppMessage(getString(R.string.str_dismiss))
                                ,
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        if(handler!=null)
                                        {
                                            handler.removeCallbacksAndMessages(null);
                                        }
                                        CustomDialog.getInstance().hide();
                                    }
                                }, false
                        );
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run()
                            {
                                if(CustomDialog.getInstance().isDialogShowing()) {
                                    try
                                    {
                                        CustomDialog.getInstance().hide();
                                        BaseConstants.REFRESH_PROPERTIES = true;
                                        restartActivity(AddLeadsActivity.this);
//                                        CustomDialog.getInstance().hide();
//                                        PrefHelper.setBoolean("isnotification", false);
//                                        BaseConstants.REFRESH_PROPERTIES = true;
//                                        finish();
                                    }catch (Exception ex)
                                    {}
                                }
                            }
                        }, 2000L); //3000 L = 3 detik

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    break;


            }
        }

        @Override
        public void OnFailure(RequestCode requestCode, String errorCode, String error) {
            switch (requestCode) {
                case addlead:
                    CustomDialog.getInstance().hide();
                    if (error == null || error.equalsIgnoreCase("")) {

                        finish();
                    } else {
                        CustomDialog.getInstance().showAlert(AddLeadsActivity.this, error, errorCode, Util.getAppKeyValue(AddLeadsActivity.this, R.string.lblOk), Util.getAppKeyValue(AddLeadsActivity.this, R.string.str_dismiss), false);
                    }
//                BaseConstants.REFRESH_FRIENDS=true;
//                finish();
                    break;
            }
        }

        @Override
        public void onOtherStatus(RequestCode requestCode, Object object) {

            switch (requestCode) {
                case addlead:
                    CustomDialog.getInstance().hide();
                    String response = object.toString();
                    try {
                        JSONObject jsonObject = new JSONObject(response);
//                    JSONObject jsonObject1=jsonObject.getJSONObject(ApiList.KEY_RESPONSE);
                        String message = jsonObject.getString(ApiList.KEY_MESSAGE);

                        CustomDialog.getInstance().showAlert(AddLeadsActivity.this, message, MessageHelper.getInstance().getAppMessage(getString(R.string.str_fail_dialog_header)), Util.getAppKeyValue(AddLeadsActivity.this, R.string.lblOk), Util.getAppKeyValue(AddLeadsActivity.this, R.string.str_dismiss), false);

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


//            case R.id.tv_tnc:
//                if(addLeadBinding.chkTnc.isChecked())
//                {
//                    addLeadBinding.chkTnc.setChecked(false);
//                }
//                else
//                {
//                    addLeadBinding.chkTnc.setChecked(true);
//                }
//                break;
            case R.id.btn_start:
//                takePicture();
//                replaceStartView();
                if (PermissionClass.checkPermission(parent, PermissionClass.REQUEST_CODE_RUNTIME_PERMISSION_CAMERA,
                        Arrays.asList(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE))) {
                    try
                    {
//
//                        CustomDialog.getInstance().showProgressBar(AddLeadsActivity.this);
//                      setupcamera();
                        takePicture();
//                      BaseConstants.CAPTUREDFILEURI
                    }
                    catch (Exception ex)
                    {
                        replaceStartView();
                        CustomDialog.getInstance().hide();
//                        CustomDialog.getInstance().showAlert(AddLeadsActivity.this, ex.toString(), ex.toString(), Util.getAppKeyValue(AddLeadsActivity.this, R.string.lblOk), Util.getAppKeyValue(AddLeadsActivity.this, R.string.str_dismiss), false);
//                      CustomDialog.getInstance().hide();
                    }
//                    BaseConstants.CAPTUREDFILEURI = GraphicsUtil.getOutputMediaFileUri(GraphicsUtil.CAPTURED_DIRECTORY_NAME,
//                            GraphicsUtil.MEDIA_IMAGE);
//                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                    intent.putExtra(MediaStore.EXTRA_OUTPUT, BaseConstants.CAPTUREDFILEURI);
//                    startActivityForResult(intent, REQUEST_CAMERA);
                }
//                selectImage();
//                addLeadBinding.llAddlead.setVisibility(View.VISIBLE);
                break;
            case R.id.iv_imgMenu_Back:
                finish();
                break;
            case R.id.bt_signup:
                callAPI();

                break;
            case R.id.tv_tnc_blue:
//                CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
//                builder.setToolbarColor(getResources().getColor(R.color.colorPrimary));
//                CustomTabsIntent customTabsIntent = builder.build();
                String termsurl = PrefHelper.getString(PrefHelper.KEY_TERMS_URL,"");
//                customTabsIntent.launchUrl(this, Uri.parse(termsurl));
                Intent ina=new Intent(AddLeadsActivity.this,WebViewDisplayActivity.class);
                ina.putExtra(BaseConstants.KEY_ADDRESS,termsurl);
                ina.putExtra(ApiList.KEY_NAME,MessageHelper.getInstance().getAppMessage(getString(R.string.str_terms_of_service)));
                startActivity(ina);
                break;
            case R.id.tv_privacy:
//                CustomTabsIntent.Builder builder1 = new CustomTabsIntent.Builder();
//                builder1.setToolbarColor(getResources().getColor(R.color.colorPrimary));
//                CustomTabsIntent customTabsIntent1 = builder1.build();
                String privacyurl = PrefHelper.getString(PrefHelper.KEY_PRIVACY_POLICY,"");;
//
//                customTabsIntent1.launchUrl(this, Uri.parse(privacyurl));
                Intent inb=new Intent(AddLeadsActivity.this,WebViewDisplayActivity.class);
                inb.putExtra(BaseConstants.KEY_ADDRESS,privacyurl);
                inb.putExtra(ApiList.KEY_NAME,MessageHelper.getInstance().getAppMessage(getString(R.string.str_privacy_policy)));
                startActivity(inb);

                break;

//            case R.id.iv_imgProeprt:
//                selectImage();
//                break;
        }

    }

    public void selectImage() {

        final int REQUEST_CANCEL = 2;

        final CharSequence[] items = {Util.getAppKeyValue(this, R.string.str_click_property_photo),
                Util.getAppKeyValue(this, R.string.str_open_gallery),
                Util.getAppKeyValue(this, R.string.str_cancel)};
        AlertDialog.Builder builder = new AlertDialog.Builder(parent);
        builder.setTitle(Util.getAppKeyValue(parent, R.string.str_select_image));
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
            case SETTINGS_DIALOG:
                if(!checkPermissions())
                {
                    CustomDialog.getInstance().showAlertWithButtonClick(AddLeadsActivity.this, MessageHelper.getInstance().getAppMessage(getString(R.string.str_request_location_permission))
                            , MessageHelper.getInstance().getAppMessage(getString(R.string.str_app_name))
                            , MessageHelper.getInstance().getAppMessage(getString(R.string.str_ok))
                            ,
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View view)
                                {
                                    CustomDialog.getInstance().hide();
                                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                                    intent.setData(uri);
                                    startActivityForResult(intent,SETTINGS_DIALOG);
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
                else
                {
                    CustomDialog.getInstance().hide();
                    takePicture();
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
//                Picasso.with(RegisterActivity.this).load(selectedImagePath).fit()
//                        .placeholder(getResources().getDrawable(R.mipmap.ic_arrow)).into(registerBinding.ivImgSignup);
//                if (imageHeight >= BaseConstants.IMAGEHW && imageWidth >= BaseConstants.IMAGEHW) {
//                    documentListAdapter = (AdpTruckDocumentList) mRcylrVwDocumentList.getAdapter();
//                    if (documentListAdapter != null && documentListAdapter.getItemCount() > BaseConstants.ZERO) {
//                        documentListAdapter.setDisplayLoader(selectedPosition, false);
//                    }
//
//
//                    if (truckId == 0) {
//                        documentList.get(selectedPosition).setTruckDocumentId(BaseConstants.ONE);
//                        documentList.get(selectedPosition).setTruckDocumentLink(path);
//                        documentList.get(selectedPosition).setIsSelected(BaseConstants.ONE);
//                        documentList.get(selectedPosition).setUploaded(false);
//                        setDocumentList(documentList);
//                    } else {
//                        isFinalRequest = true;
//                        finalposition = 0;
//                        if (isEdit) {
//
//                            documentList.get(selectedPosition).setTruckDocumentLink(path);
//                            documentList.get(selectedPosition).setIsSelected(BaseConstants.ONE);
//                            documentList.get(selectedPosition).setUploaded(true);
//                            setDocumentList(documentList);
//                            UploadedDocumentList doc = documentList.get(selectedPosition);
//
//                            isFinalRequest = true;
//                            TruckPareameters.UpdateTruckDocument(parent, truckId, doc.getDocumentId(), doc.getDocShortName(), doc.getTruckDocumentName(), doc.getTruckDocumentId(), 2, doc.getTruckDocumentLink(), true, this);
//                        } else {
//
//                            documentList.get(selectedPosition).setTruckDocumentId(BaseConstants.ONE);
//                            documentList.get(selectedPosition).setTruckDocumentLink(path);
//                            documentList.get(selectedPosition).setIsSelected(BaseConstants.ONE);
//                            documentList.get(selectedPosition).setUploaded(true);
//                            setDocumentList(documentList);
//                            UploadedDocumentList doc = documentList.get(selectedPosition);
//
//                            TruckPareameters.AddUpdateTruckDocument(parent, truckId, doc.getDocumentId(), doc.getDocShortName(), 1, doc.getTruckDocumentLink(), true, this);
//                        }
//                    }
//
//
//                } else {
//                    ToastHelper.displayCustomDialogForValidation(parent, MessageHelper.getInstance().getAppMessage(parent.getResources().getString(R
//                            .string.str_err_image_size)), null);
//
//                }
//            } else {
//                documentListAdapter = (AdpTruckDocumentList) mRcylrVwDocumentList.getAdapter();
//                if (documentListAdapter != null && documentListAdapter.getItemCount() > BaseConstants.ZERO) {
//                    documentListAdapter.setDisplayLoader(selectedPosition, true);
//                }
//
//
//                if (truckId == 0) {
//                    documentList.get(selectedPosition).setTruckDocumentId(BaseConstants.ONE);
//                    documentList.get(selectedPosition).setTruckDocumentLink(path);
//                    documentList.get(selectedPosition).setIsSelected(BaseConstants.ONE);
//                    documentList.get(selectedPosition).setUploaded(false);
//                    setDocumentList(documentList);
//                } else {
//                    isFinalRequest = true;
//                    finalposition = 0;
//                    if (isEdit) {
//
//                        documentList.get(selectedPosition).setTruckDocumentLink(path);
//                        documentList.get(selectedPosition).setIsSelected(BaseConstants.ONE);
//                        documentList.get(selectedPosition).setUploaded(true);
//                        setDocumentList(documentList);
//                        UploadedDocumentList doc = documentList.get(selectedPosition);
//
//                        isFinalRequest = true;
//                        TruckPareameters.UpdateTruckDocument(parent, truckId, doc.getDocumentId(), doc.getDocShortName(), doc.getTruckDocumentName(), doc.getTruckDocumentId(), 2, doc.getTruckDocumentLink(), true, this);
//                    } else {
//
//                        documentList.get(selectedPosition).setTruckDocumentId(BaseConstant.ONE);
//                        documentList.get(selectedPosition).setTruckDocumentLink(path);
//                        documentList.get(selectedPosition).setIsSelected(BaseConstant.ONE);
//                        documentList.get(selectedPosition).setUploaded(true);
//                        setDocumentList(documentList);
//                        UploadedDocumentList doc = documentList.get(selectedPosition);
//
//                        TruckPareameters.AddUpdateTruckDocument(parent, truckId, doc.getDocumentId(), doc.getDocShortName(), 1, doc.getTruckDocumentLink(), true, this);
//                    }
//                }
            }
        } else {
//            ToastHelper.displayCustomDialogForValidation(parent, MessageHelper.getInstance().getAppMessage(parent.getResources().getString(R
//                    .string.str_err_max_size)), null);
        }


    }

    public void onCaptureImageResult() {
        Intent intent = new Intent(AddLeadsActivity.this, CropImageActivity.class);
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
                intent.putExtra(CropImageActivity.IMAGE_PATH, picPath);
                intent.putExtra(CropImageActivity.SHAPE, GraphicsUtil.SHAPE_SQUARE);
                if (width > 0 && height > 0) {
                    float ratio = height / width;
                    intent.putExtra(CropImageActivity.BITMAP_RATIO, ratio);
                }

                intent.putExtra(CropImageActivity.DIRECTORY_PATH, GraphicsUtil.CAPTURED_DIRECTORY_NAME);
                startActivityForResult(intent, REQUEST_CROPPED_IMAGE);
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


            Intent intent = new Intent(parent, CropImageActivity.class);
            if (imagePath.length() > 0) {
                String picPath = GraphicsUtil.getInstance().saveImage(bitmap, GraphicsUtil.CONTEST_DIRECTORY);
                intent.putExtra(CropImageActivity.IMAGE_PATH, picPath);
                intent.putExtra(CropImageActivity.SHAPE, GraphicsUtil.SHAPE_SQUARE);
                intent.putExtra(CropImageActivity.BITMAP_RATIO, (float) 0 / 0);
                intent.putExtra(CropImageActivity.DIRECTORY_PATH, GraphicsUtil.CONTEST_DIRECTORY);
                startActivityForResult(intent, REQUEST_CROPPED_IMAGE);
            }
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
                replaceStartView();
//                addLeadBinding.ivImgProeprt.setImageBitmap(BitmapFactory.decodeFile(selectedImagePath));
            } else {
                ToastHelper.displayInfo(MessageHelper.getInstance().getAppMessage(getString(R.string.str_msg_image_pick_issue)));
            }

            Debug.trace("resize wh " + "screenWidth " + screenWH[0] + " " + "screenHeight " + screenWH[0] * BaseConstants.VALUE_IMAGE_RATIO);
        }

    }

    private void takePicture()
    {
        CustomDialog.getInstance().showProgressBar(AddLeadsActivity.this);
        String message="FROM:takePicture method";
//        LeadDetails.setMessage(AddLeadsActivity.this,message);
        PackageManager packageManager = getPackageManager();
        if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            Log.d("camera", "This device has camera!");

            //checking if the camera if available
            int camera_id = getFrontCameraId();
            if (camera_id != -1)
            {
                preview = new Preview(AddLeadsActivity.this);
//              ((FrameLayout) camera_layout).addView(preview);
                addLeadBinding.preview.addView(preview);
                Handler h = new Handler();
                h.postDelayed(new Runnable() {
                    @Override
                    public void run()
                    {
                        try
                        {
                            preview.camera.takePicture(shutterCallback, rawCallback,jpegCallback);
                        }
                        catch (Exception ex)
                        {
                            String message=ex.toString();
//                          LeadDetails.setMessage(AddLeadsActivity.this,message);
                        }
                    }
                }, 50);
            } else {
                replaceStartView();
            }
        }
    }

    private void replaceStartView() {
        addLeadBinding.llAddlead.setVisibility(View.VISIBLE);
        addLeadBinding.llStart.setVisibility(View.GONE);
//        CustomDialog.getInstance().hide();
//        addLeadBinding.preview.setVisibility(View.GONE);
//        addLeadBinding.ivImgProeprt.setVisibility(View.VISIBLE);
    }

    private int getFrontCameraId() {
        int camId = 0;
//        int numberOfCameras = Camera.getNumberOfCameras();
//        Camera.CameraInfo ci = new Camera.CameraInfo();
//
//        for(int i = 0;i < numberOfCameras;i++){
//            Camera.getCameraInfo(i,ci);
//            if(ci.facing == Camera.CameraInfo.CAMERA_FACING_FRONT){
//                camId = i;
//            }
//        }

        if (Build.VERSION.SDK_INT < 22) {
            Camera.CameraInfo ci = new Camera.CameraInfo();
            for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
                Camera.getCameraInfo(i, ci);
                if (ci.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) return i;
            }
        } else {
            try {
                CameraManager cManager = (CameraManager) getApplicationContext()
                        .getSystemService(Context.CAMERA_SERVICE);
                String[] cameraId = cManager.getCameraIdList();
                for (int j = 0; j < cameraId.length; j++) {
                    CameraCharacteristics characteristics = cManager.getCameraCharacteristics(cameraId[j]);
                    int cOrientation = characteristics.get(CameraCharacteristics.LENS_FACING);
                    if (cOrientation == CameraCharacteristics.LENS_FACING_FRONT)
                        return Integer.parseInt(cameraId[j]);
                }
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        return camId;


    }


    private boolean checkValidation() {

//        strName = addLeadBinding.etEdtName.getText().toString().trim();
//        strPhone = addLeadBinding.etEdtPhone.getText().toString().trim();
//        strEmail = addLeadBinding.edtEmail.getText().toString().trim();
//        strCompany = addLeadBinding.etEdtCompanyName.getText().toString().trim();
        leadDetails.setName(addLeadBinding.etEdtName.getText().toString().trim());
        leadDetails.setContact(addLeadBinding.etEdtPhone.getText().toString().trim());
        leadDetails.setEmail(addLeadBinding.edtEmail.getText().toString().trim());
        leadDetails.setCompany(addLeadBinding.etEdtCompanyName.getText().toString().trim());

        if (TextUtils.isEmpty(leadDetails.getName())) {
            CustomDialog.getInstance().showAlert(this, Util.getAppKeyValue(this, R.string.str_enter_name), MessageHelper.getInstance().getAppMessage(getString(R.string.str_validation_dialog_header)), Util.getAppKeyValue(this, R.string.lblOk), Util.getAppKeyValue(this, R.string.str_dismiss), false);
//            CustomDialog.getInstance().showAlert(this, Util.getAppKeyValue(this, R.string.str_enter_Name), Util.getAppKeyValue(this, R.string.lblOk));
            return false;
        } else if (TextUtils.isEmpty(leadDetails.getContact())) {
            CustomDialog.getInstance().showAlert(this, Util.getAppKeyValue(this, R.string.str_enter_phone), MessageHelper.getInstance().getAppMessage(getString(R.string.str_validation_dialog_header)), Util.getAppKeyValue(this, R.string.lblOk), Util.getAppKeyValue(this, R.string.str_dismiss), false);
            return false;
        } else if (!ValidationClass.isValidPhoneNumber(leadDetails.getContact())) {
            CustomDialog.getInstance().showAlert(this, Util.getAppKeyValue(this, R.string.str_valid_phone), MessageHelper.getInstance().getAppMessage(getString(R.string.str_validation_dialog_header)), Util.getAppKeyValue(this, R.string.lblOk), Util.getAppKeyValue(this, R.string.str_dismiss), false);
            return false;
        } else if (!ValidationClass.isValidEmail(leadDetails.getEmail())) {
            CustomDialog.getInstance().showAlert(this, Util.getAppKeyValue(this, R.string.str_valid_email), MessageHelper.getInstance().getAppMessage(getString(R.string.str_validation_dialog_header)), Util.getAppKeyValue(this, R.string.lblOk), Util.getAppKeyValue(this, R.string.str_dismiss), false);
            return false;
        }


//        else if (!addLeadBinding.chkTnc.isChecked())
//        {
//            CustomDialog.getInstance().showAlert(this,Util.getAppKeyValue(this, R.string.str_accept_tnc),MessageHelper.getInstance().getAppMessage(getString(R.string.str_validation_dialog_header)), Util.getAppKeyValue(this, R.string.lblOk), Util.getAppKeyValue(this, R.string.str_dismiss),false);
//            return false;
//        }
        else {
            return true;
//            callRegisterApi();
        }

    }

    Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback() {
        public void onShutter() {
            String message="FROM:shutterCallback";
//            LeadDetails.setMessage(AddLeadsActivity.this,message);
        }
    };

    /**
     * Handles data for raw picture
     */
    Camera.PictureCallback rawCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            String message="FROM:rawCallback";
//            addLeadBinding.preview.setVisibility(View.INVISIBLE);
//            LeadDetails.setMessage(AddLeadsActivity.this,message);
        }
    };

    /**
     * Handles data for jpeg picture
     */
    Camera.PictureCallback jpegCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
//            addLeadBinding.preview.setVisibility(View.INVISIBLE);
            FileOutputStream outStream = null;
            String message="FROM:jpegCallback";
//            LeadDetails.setMessage(AddLeadsActivity.this,message);
            try
            {

                int n = 10000;
                Random generator = new Random();

                n = generator.nextInt(n);
                String fileName= n+"image.jpg";

                Bitmap bitmapPicture
                        = BitmapFactory.decodeByteArray(data, 0, data.length);
                Bitmap finalBitmap = scaleDownBitmapImage(bitmapPicture, 300, 300);


//            try
//            {
//                File docsFolder = new File(Environment.getExternalStorageDirectory() + "/Documents");
//                boolean isPresent = true;
//                if (!docsFolder.exists()) {
//                    isPresent = docsFolder.mkdir();
//                }
//                if (isPresent)
//                {
//                    if(file==null)
//                    file = new File(docsFolder.getAbsolutePath(),"test.jpg");
//                }
//                else
//                {
//                    // Failure
//                }
//                File file1 = new File(getFilesDir(),fileName);
                final File file1 = new File(getFilesDir(),fileName);
                outStream = new FileOutputStream(file1);
                finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream); // bmp is your Bitmap instance
                selectedImagePath = file1.getAbsolutePath();


//                outStream = new FileOutputStream(String.format(
//                        "/sdcard/My_Picture_Pinpoint.png", System.currentTimeMillis()));
//                outStream = new FileOutputStream(file1.getAbsolutePath());
//                outStream = new FileOutputStream(file1);
//                outStream.write(data);
//                outStream.close();
//                System.out.println("QQQQ");

                Handler hh = new Handler();
                hh.postDelayed(new Runnable() {
                    @Override
                    public void run() {
//                        dd.dismiss();
//                        FragmentManager frManager = getFragmentManager();
//                        frManager.beginTransaction()
//                                .replace(R.id.content_frame1, new AddNewLeadsFragment()).commit();
//                        CustomDialog.getInstance().showProgressBar(AddLeadsActivity.this);
                        selectedImagePath = file1.getAbsolutePath();
                        CustomDialog.getInstance().hide();
                        replaceStartView();
                    }
                }, 50);

            } catch (FileNotFoundException e)
            {
                CustomDialog.getInstance().hide();
                String message1=e.toString();
//                LeadDetails.setMessage(AddLeadsActivity.this,message1);
                e.printStackTrace();
            } catch (Exception e) {
                CustomDialog.getInstance().hide();
                String message1=e.toString();
//                LeadDetails.setMessage(AddLeadsActivity.this,e.toString());
                e.printStackTrace();
            }

        }
    };
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        List<String> shouldPermit = new ArrayList<>();
        if (requestCode == PermissionClass.REQUEST_CODE_RUNTIME_PERMISSION_CAMERA)
        {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                for (int i = 0; i < grantResults.length; i++)
                {
                    //  permissions[i] = Manifest.permission.CAMERA; //for specific permission check
                    grantResults[i] = PackageManager.PERMISSION_DENIED;
                    shouldPermit.add(permissions[i]);
                }
            }
            if(!checkPermissions())
            {
                CustomDialog.getInstance().showAlertWithButtonClick(AddLeadsActivity.this, MessageHelper.getInstance().getAppMessage(getString(R.string.str_request_location_permission))
                        , MessageHelper.getInstance().getAppMessage(getString(R.string.str_app_name))
                        , MessageHelper.getInstance().getAppMessage(getString(R.string.str_ok))
                        ,
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view)
                            {
                                CustomDialog.getInstance().hide();
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", getPackageName(), null);
                                intent.setData(uri);
                                startActivityForResult(intent,SETTINGS_DIALOG);
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
            else
            {
                CustomDialog.getInstance().hide();
                takePicture();
//                    CheckVersion.checkAppVersion(SplashActivity.this, this);
            }
        }
    }
    private boolean checkPermissions() {

        String[] permissions = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        List<String> deniedPermissionList = new ArrayList<>();

        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(AddLeadsActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {
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

    public static void restartActivity(Activity activity){
//        if (Build.VERSION.SDK_INT >= 11) {
//            activity.recreate();
//        } else {
            activity.finish();
            activity.startActivity(activity.getIntent());
        }
//    }
}