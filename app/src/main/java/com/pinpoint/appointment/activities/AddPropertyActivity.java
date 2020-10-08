package com.pinpoint.appointment.activities;


import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Geocoder;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.pinpoint.appointment.BaseActivity;
import com.pinpoint.appointment.BaseApplication;
import com.pinpoint.appointment.BaseConstants;
import com.pinpoint.appointment.R;
import com.pinpoint.appointment.adapter.AdpPlacesNew;
import com.pinpoint.appointment.adapter.PlaceArrayAdapter;
import com.pinpoint.appointment.api.ApiList;
import com.pinpoint.appointment.api.RequestCode;
import com.pinpoint.appointment.api.ServerConfig;
import com.pinpoint.appointment.customviews.CustomDialog;
import com.pinpoint.appointment.customviews.GenericView;
import com.pinpoint.appointment.databinding.ActivityAddPropertyBinding;
import com.pinpoint.appointment.graphicsUtils.GraphicsUtil;
import com.pinpoint.appointment.helper.MessageHelper;
import com.pinpoint.appointment.helper.PrefHelper;
import com.pinpoint.appointment.helper.ToastHelper;
import com.pinpoint.appointment.interfaces.ClickEvent;
import com.pinpoint.appointment.interfaces.DataObserver;
import com.pinpoint.appointment.models.PropertyData;
import com.pinpoint.appointment.permissionUtils.PermissionClass;
import com.pinpoint.appointment.utils.Debug;
import com.pinpoint.appointment.utils.GPSTracker;
import com.pinpoint.appointment.utils.Util;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class AddPropertyActivity extends BaseActivity implements ClickEvent, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {


    private AddPropertyActivity parent;
    private Bundle bundle;
    ActivityAddPropertyBinding propertyBindng;
    String selectedAddress = "";
    String strName, strPhone;
    private static final int GOOGLE_API_CLIENT_ID = 0;
    private GoogleApiClient mGoogleApiClient;

    private int[] screenWH;
    private final int REQUEST_CAMERA = 0;
    private final int REQUEST_GALLERY = 1;
    private final int PICK_FILE_RESULT_CODE = 3;
    private final int REQUEST_CROPPED_IMAGE = 2;
    private String selectedImagePath = "";
    PropertyData propertyData = null;

    private static final int CROP_PIC_REQUEST_CODE = 1234;
    private PlaceArrayAdapter mPlaceArrayAdapter;
    TextView tvLabel;
    AdpPlacesNew placeAdapter;
    private CircleImageView iv_imgProeprty;


    public AddPropertyActivity() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        propertyBindng = DataBindingUtil.setContentView(this, R.layout.activity_add_property);
        if (Util.isTabletDevice(this)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        Util.setupOutSideTouchHideKeyboard(GenericView.findViewById(this, R.id.ll_parent));
        iv_imgProeprty = (CircleImageView) findViewById(R.id.iv_imgProeprty);
        ImageView imgBack = GenericView.findViewById(this, R.id.iv_imgMenu_Back);
        ImageView imgSettings = GenericView.findViewById(this, R.id.iv_imgSetting);
        TextView tvHeader = GenericView.findViewById(this, R.id.tv_txtTitle);
        tvHeader.setText(Util.getAppKeyValue(this, R.string.str_open_house_registry));
        screenWH = GraphicsUtil.getScreenWidthHeight();
        imgBack.setImageResource(R.mipmap.ic_back);
        imgSettings.setVisibility(View.INVISIBLE);
        tvHeader.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_SEMIBOLD));
        propertyBindng.acTxtCityState.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));
        tvLabel = findViewById(R.id.tv_label);
        propertyBindng.btSignup.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_SEMIBOLD));
        tvLabel.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));
        propertyBindng.btSignup.setAllCaps(true);
        propertyBindng.btSignup.setText(MessageHelper.getInstance().getAppMessage(getString(R.string.str_create)));

        parent = this;
        if (getIntent() != null && getIntent().hasExtra(BaseConstants.KEY_OBJECT) && getIntent().getSerializableExtra(BaseConstants.KEY_OBJECT) != null) {
            iv_imgProeprty.setVisibility(View.VISIBLE);
            tvLabel.setVisibility(View.VISIBLE);
            propertyData = (PropertyData) getIntent().getSerializableExtra(BaseConstants.KEY_OBJECT);
            propertyBindng.acTxtCityState.setText(propertyData.getAddress());
            propertyBindng.acTxtCityState.setText(propertyData.getAddress());
            propertyBindng.btSignup.setText(Util.getAppKeyValue(this, R.string.str_update));
            Picasso.get().load(propertyData.getPropertyImage()).noFade().placeholder(R.mipmap.app_icon)
                    .into(propertyBindng.ivImgProeprty);
        }/* else if (getIntent() != null && getIntent().hasExtra(BaseConstants.KEY_OBJECT) && getIntent().getSerializableExtra(BaseConstants.KEY_OBJECT) == null) {
            iv_imgProeprty.setVisibility(View.INVISIBLE);
            tvLabel.setVisibility(View.INVISIBLE);
        }*/
    }

    private void callAPI() {
        if (checkValidation()) {
            if (selectedAddress.equalsIgnoreCase("")) {
                selectedAddress = propertyBindng.acTxtCityState.getText().toString();
            }
            if (propertyData == null) {
                propertyData = new PropertyData();
                propertyData.setAddress(propertyBindng.acTxtCityState.getText().toString());
                // propertyData.setPropertyImage(selectedImagePath);
                PropertyData.addPropertty(AddPropertyActivity.this, propertyData, selectedAddress, dataObserver);
            } else {
                propertyData.setAddress(propertyBindng.acTxtCityState.getText().toString());
                propertyData.setPropertyImage(selectedImagePath);
                PropertyData.editProperty(AddPropertyActivity.this, propertyData, selectedAddress, dataObserver);
            }
        }
    }

    DataObserver dataObserver = new DataObserver() {
        @Override
        public void OnSuccess(RequestCode requestCode, Object object) {
            switch (requestCode) {
                case addproperty:
                    iv_imgProeprty.setVisibility(View.INVISIBLE);
                    tvLabel.setVisibility(View.INVISIBLE);
                    CustomDialog.getInstance().hide();
                    try {
                        String response = object.toString();
                        JSONObject jsonObject = new JSONObject(response);
//                  JSONObject jsonObject1=jsonObject.getJSONObject(ApiList.KEY_RESPONSE);
                        String message = jsonObject.getString(ApiList.KEY_MESSAGE);
                        JSONArray result = jsonObject.getJSONArray(ApiList.KEY_RESULT);
                        JSONObject propertyData = result.getJSONObject(0);
                        final int propertyId = Integer.parseInt(propertyData.getString(ApiList.KEY_ID));
                        final Handler handler = new Handler();
                        CustomDialog.getInstance().showAlertWithButtonClick(AddPropertyActivity.this, message
                                , MessageHelper.getInstance().getAppMessage(getString(R.string.str_app_name))
                                , MessageHelper.getInstance().getAppMessage(getString(R.string.str_ok))
                                ,
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        if (handler != null) {
                                            handler.removeCallbacksAndMessages(null);
                                        }
                                        CustomDialog.getInstance().hide();
                                        BaseConstants.REFRESH_PROPERTIES = true;
//                                    Intent inb = new Intent(parent, AddLeadsActivity.class);
//                                    inb.putExtra(BaseConstants.KEY_PROPERTY_ID,propertyId);
//                                    startActivity(inb);
                                        finish();
//                                    BaseConstants.REFRESH_PROPERTIES=true;
//                                    PrefHelper.setBoolean("isnotification", false);
//                                    Intent ina=new Intent(AddPropertyActivity.this,HomeActivity.class);
//                                    ina.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                                    startActivity(ina);
//                                    finish();

                                    }
                                }, MessageHelper.getInstance().getAppMessage(getString(R.string.str_dismiss))
                                ,
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        if (handler != null) {
                                            handler.removeCallbacksAndMessages(null);
                                        }
                                        CustomDialog.getInstance().hide();
                                    }
                                }, false
                        );

                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (CustomDialog.getInstance().isDialogShowing()) {
                                    try {
                                        CustomDialog.getInstance().hide();
                                        BaseConstants.REFRESH_PROPERTIES = true;
//                                    Intent inb = new Intent(parent, AddLeadsActivity.class);
//                                    inb.putExtra(BaseConstants.KEY_PROPERTY_ID,propertyId);
//                                    startActivity(inb);
                                        finish();
                                    } catch (Exception ex) {
                                    }
                                }
                            }
                        }, 2000L); //3000 L = 3 detik

//                    ToastHelper.displayCustomToast(message);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case editproperty:
                    iv_imgProeprty.setVisibility(View.VISIBLE);
                    tvLabel.setVisibility(View.VISIBLE);
                    tvLabel.setText(MessageHelper.getInstance().getAppMessage(getString(R.string.str_add_property_image)));
                    CustomDialog.getInstance().hide();
                    try {
                        String response = object.toString();
                        JSONObject jsonObject = new JSONObject(response);
//                    JSONObject jsonObject1=jsonObject.getJSONObject(ApiList.KEY_RESPONSE);
                        String message = jsonObject.getString(ApiList.KEY_MESSAGE);
//                    ToastHelper.displayCustomToast(message);
                        final Handler handler = new Handler();
                        CustomDialog.getInstance().showAlertWithButtonClick(AddPropertyActivity.this, message
                                , MessageHelper.getInstance().getAppMessage(getString(R.string.str_app_name))
                                , MessageHelper.getInstance().getAppMessage(getString(R.string.str_ok))
                                ,
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        if (handler != null) {
                                            handler.removeCallbacksAndMessages(null);
                                        }
                                        CustomDialog.getInstance().hide();
                                        PrefHelper.setBoolean("isnotification", false);
                                        BaseConstants.REFRESH_PROPERTIES = true;
                                       /* Intent ina = new Intent(AddPropertyActivity.this, HomeActivity.class);
                                        ina.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(ina);*/
                                        finish();

                                    }
                                }, MessageHelper.getInstance().getAppMessage(getString(R.string.str_dismiss))
                                ,
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        if (handler != null) {
                                            handler.removeCallbacksAndMessages(null);
                                        }
                                        CustomDialog.getInstance().hide();
                                    }
                                }, false
                        );

                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (CustomDialog.getInstance().isDialogShowing()) {
                                    try {
                                        CustomDialog.getInstance().hide();
                                        CustomDialog.getInstance().hide();
                                        PrefHelper.setBoolean("isnotification", false);
                                        BaseConstants.REFRESH_PROPERTIES = true;
                                      /*  Intent ina = new Intent(AddPropertyActivity.this, HomeActivity.class);
                                        ina.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(ina);*/
                                        finish();
                                    } catch (Exception ex) {
                                    }
                                }
                            }
                        }, 2000L); //3000 L = 3 detik


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    break;

                case deletepropertyimage:
                    iv_imgProeprty.setVisibility(View.VISIBLE);
                    tvLabel.setVisibility(View.VISIBLE);
                    tvLabel.setText(MessageHelper.getInstance().getAppMessage(getString(R.string.str_add_property_image)));
                    CustomDialog.getInstance().hide();
                    try {
                        String response1 = object.toString();
                        BaseConstants.REFRESH_PROPERTIES = true;
                        JSONObject jsonObject = new JSONObject(response1);
//                    JSONObject jsonObject1=jsonObject.getJSONObject(ApiList.KEY_RESPONSE);
                        String message = jsonObject.getString(ApiList.KEY_MESSAGE);
                        ToastHelper.displayCustomToast(message);
                        propertyData.setPropertyImage("http://pinpointappointment.com/admin/images/Image-02.png");
                        Picasso.get().load(propertyData.getPropertyImage()).noFade().placeholder(R.drawable.user1)
                                .into(propertyBindng.ivImgProeprty);
                    } catch (Exception ex) {
                    }
                    break;
            }
        }

        @Override
        public void OnFailure(RequestCode requestCode, String errorCode, String error) {
            switch (requestCode) {
                case addproperty:
                    CustomDialog.getInstance().hide();
                    if (error == null || error.equalsIgnoreCase("")) {
                        BaseConstants.REFRESH_PROPERTIES = true;
                        finish();
                    } else {
                        CustomDialog.getInstance().showAlert(AddPropertyActivity.this, error, errorCode, Util.getAppKeyValue(AddPropertyActivity.this, R.string.lblOk), Util.getAppKeyValue(AddPropertyActivity.this, R.string.str_dismiss), false);
                    }
                    break;
                case editproperty:
                    CustomDialog.getInstance().hide();
                    if (error == null || error.equalsIgnoreCase("")) {
                        BaseConstants.REFRESH_PROPERTIES = true;
                        finish();
                    } else {
                        CustomDialog.getInstance().showAlert(AddPropertyActivity.this, error, errorCode, Util.getAppKeyValue(AddPropertyActivity.this, R.string.lblOk), Util.getAppKeyValue(AddPropertyActivity.this, R.string.str_dismiss), false);
                    }
                    break;
                case deletepropertyimage:
                    CustomDialog.getInstance().hide();
                    if (error == null || error.equalsIgnoreCase("")) {
                        BaseConstants.REFRESH_PROPERTIES = true;
                        finish();
                    } else {
                        CustomDialog.getInstance().showAlert(AddPropertyActivity.this, error, errorCode, Util.getAppKeyValue(AddPropertyActivity.this, R.string.lblOk), Util.getAppKeyValue(AddPropertyActivity.this, R.string.str_dismiss), false);
                    }
                    break;
            }
        }

        @Override
        public void onOtherStatus(RequestCode requestCode, Object object) {

            switch (requestCode) {
                case addproperty:
                    CustomDialog.getInstance().hide();
                    try {
                        String response = object.toString();
                        BaseConstants.REFRESH_PROPERTIES = false;
                        JSONObject jsonObject = new JSONObject(response);
//                    JSONObject jsonObject1=jsonObject.getJSONObject(ApiList.KEY_RESPONSE);
                        String message = jsonObject.getString(ApiList.KEY_MESSAGE);

                        CustomDialog.getInstance().showAlert(AddPropertyActivity.this, message, Util.getAppKeyValue(AddPropertyActivity.this, R.string.str_error), Util.getAppKeyValue(AddPropertyActivity.this, R.string.lblOk), Util.getAppKeyValue(AddPropertyActivity.this, R.string.str_dismiss), false);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    break;
                case editproperty:
                    CustomDialog.getInstance().hide();

                    try {
                        String response = object.toString();
                        BaseConstants.REFRESH_PROPERTIES = false;
                        JSONObject jsonObject = new JSONObject(response);
//                    JSONObject jsonObject1=jsonObject.getJSONObject(ApiList.KEY_RESPONSE);
                        String message = jsonObject.getString(ApiList.KEY_MESSAGE);

                        CustomDialog.getInstance().showAlert(AddPropertyActivity.this, message, Util.getAppKeyValue(AddPropertyActivity.this, R.string.str_error), Util.getAppKeyValue(AddPropertyActivity.this, R.string.lblOk), Util.getAppKeyValue(AddPropertyActivity.this, R.string.str_dismiss), false);

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
            case R.id.iv_imgProeprty:
                selectImage();
                break;
            case R.id.iv_imgMenu_Back:
                finish();
                break;
            case R.id.bt_signup:
                callAPI();
                break;
        }

    }

    private boolean checkValidation() {
        if (TextUtils.isEmpty(propertyBindng.acTxtCityState.getText().toString())) {
            CustomDialog.getInstance().showAlert(this, Util.getAppKeyValue(this, R.string.str_enter_address), MessageHelper.getInstance().getAppMessage(getString(R.string.str_fail_dialog_header)), Util.getAppKeyValue(this, R.string.lblOk), Util.getAppKeyValue(this, R.string.str_dismiss), false);
            return false;
        }
//        else if(TextUtils.isEmpty(selectedImagePath))
//        {
//            if(propertyData==null||propertyData.getPropertyImage().equalsIgnoreCase("")) {
//                CustomDialog.getInstance().showAlert(this, Util.getAppKeyValue(this, R.string.str_select_property_pic), MessageHelper.getInstance().getAppMessage(getString(R.string.str_fail_dialog_header)), Util.getAppKeyValue(this, R.string.lblOk), Util.getAppKeyValue(this, R.string.str_dismiss), false);
//                return false;
//            }
//            return true;
//        }

        return true;

    }


    public void selectImage() {

        final int REQUEST_CANCEL = 2;
        final int REQUEST_DELETE = 3;
        CharSequence[] items = null;
        if (selectedImagePath.equalsIgnoreCase("") && propertyData == null) {
            items = new CharSequence[]{Util.getAppKeyValue(this, R.string.str_click_property_photo),
                    Util.getAppKeyValue(this, R.string.str_open_gallery),
                    Util.getAppKeyValue(this, R.string.str_cancel)};
        } else {
            if (propertyData != null && !propertyData.getPropertyImage().equalsIgnoreCase("http://pinpointappointment.com/admin/images/Image-02.png")) {
                items = new CharSequence[]{Util.getAppKeyValue(this, R.string.str_click_property_photo),
                        Util.getAppKeyValue(this, R.string.str_open_gallery),
                        Util.getAppKeyValue(this, R.string.str_cancel), Util.getAppKeyValue(this, R.string.str_delete)};
            } else if (!selectedImagePath.equalsIgnoreCase("")) {
                items = new CharSequence[]{Util.getAppKeyValue(this, R.string.str_click_property_photo),
                        Util.getAppKeyValue(this, R.string.str_open_gallery),
                        Util.getAppKeyValue(this, R.string.str_cancel), Util.getAppKeyValue(this, R.string.str_delete)};
            } else {
                items = new CharSequence[]{Util.getAppKeyValue(this, R.string.str_click_property_photo),
                        Util.getAppKeyValue(this, R.string.str_open_gallery),
                        Util.getAppKeyValue(this, R.string.str_cancel)};
            }
        }
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
                            if (Util.isTabletDevice(AddPropertyActivity.this)) {
                                intent.putExtra(MediaStore.EXTRA_SCREEN_ORIENTATION, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                            }
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
                            propertyBindng.ivImgProeprty.setImageResource(R.mipmap.app_icon);
                        }
                        if (propertyData != null && !propertyData.getPropertyImage().equalsIgnoreCase("http://pinpointappointment.com/admin/images/Image-02.png")) {
                            selectedImagePath = "";
                            propertyBindng.ivImgProeprty.setImageResource(R.mipmap.app_icon);
                            PropertyData.deletePropertyImage(AddPropertyActivity.this, propertyData, dataObserver);
                        }
                        break;
                }
            }
        });
        builder.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
//        if(mGoogleApiClient==null) {
//            mGoogleApiClient = new GoogleApiClient.Builder(this)
//                    .addApi(Places.GEO_DATA_API)
//                    .enableAutoManage(this, GOOGLE_API_CLIENT_ID, this)
//                    .addConnectionCallbacks(this)
//                    .build();
        GPSTracker gpsTracker = new GPSTracker(parent);
        if (gpsTracker.canGetLocation()) {

            BaseConstants.LATITUDE = gpsTracker.getLatitude();
            BaseConstants.LONGITUDE = gpsTracker.getLongitude();
        }
        gpsTracker.stopUsingGPS();
        setupLocationAutocomplete();
//        }
    }


    private void setupLocationAutocomplete() {

        propertyBindng.acTxtCityState.setThreshold(1);

//        AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
//                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS)
//                .setCountry("US")
//                .build();
//        mPlaceArrayAdapter = new PlaceArrayAdapter(parent, android.R.layout.simple_list_item_1,
//                BaseConstants.BOUNDS_MOUNTAIN_VIEW, typeFilter);
//        propertyBindng.acTxtCityState.setAdapter(mPlaceArrayAdapter);

        ArrayList<com.pinpoint.appointment.models.Places> resultList = new ArrayList<>();
        placeAdapter = new AdpPlacesNew(this, resultList);
        try {
            propertyBindng.acTxtCityState.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    try {
                        if (placeAdapter.getCount() == 0) {
                            new getPlaces().execute(charSequence.toString());
                        } else {
                            placeAdapter.getFilter().filter(charSequence.toString());
                        }
                    } catch (Exception Ex) {
                        Ex.printStackTrace();
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {
                }
            });
        } catch (Exception Ex) {
            Ex.printStackTrace();
        }
        propertyBindng.acTxtCityState.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                com.pinpoint.appointment.models.Places mPlaces = (com.pinpoint.appointment.models.Places) adapterView.getItemAtPosition(position);

                Util.hideKeyboard(propertyBindng.acTxtCityState);
                String finalAddRess = getFinalAddress(mPlaces.getDescription());
                selectedAddress = mPlaces.getDescription();
                propertyBindng.acTxtCityState.setText(finalAddRess);
//                propertyBindng.acTxtCityState.setText(mPlaces.getDescription());
//                addAppointmentBinding.acTxtCityState.setSelection(addAppointmentBinding.acTxtCityState.getText().length());
            }
        });
    }

    private class getPlaces extends AsyncTask<String, Void, Void> {
        private final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
        private final String TYPE_AUTOCOMPLETE = "/autocomplete";
        private final String TYPE_NEARBYSEARCH = "/nearbysearch";
        private final String OUT_JSON = "/json";
        ArrayList<com.pinpoint.appointment.models.Places> resultList = null;

        @Override
        protected Void doInBackground(String... voids) {
            HttpURLConnection conn = null;
            StringBuilder jsonResults = new StringBuilder();
            if (resultList == null) {
                resultList = new ArrayList<>();
            }
            try {

                String sb = PLACES_API_BASE + TYPE_AUTOCOMPLETE + OUT_JSON + "?input=" + URLEncoder.encode(voids[0], "utf8") + "" +
                        "&types=address" +
                        "&location=" + BaseConstants.LATITUDE + "," + BaseConstants.LONGITUDE +
                        "&radius=" + BaseConstants.RADIUS +
                        "&strictbounds" +
                        "&key=" + ServerConfig.MAP_API_KEY;


//                String sb = PLACES_API_BASE + TYPE_NEARBYSEARCH + OUT_JSON + "?location=" + BaseConstants.LATITUDE+","+BaseConstants.LONGITUDE
//                        +"&radius=5000"
//                        +"&types=address"
//                        +"&keyword=" + URLEncoder.encode(voids[0], "utf8")
//                        +"&key=" + ServerConfig.MAP_API_KEY ;
                URL url = new URL(sb);
                conn = (HttpURLConnection) url.openConnection();
                InputStreamReader in = new InputStreamReader(conn.getInputStream());

                // Load the results into a StringBuilder
                int read;
                char[] buff = new char[1024];
                while ((read = in.read(buff)) != -1) {
                    jsonResults.append(buff, 0, read);
                }
            } catch (MalformedURLException ignored) {


            } catch (IOException e) {
                e.printStackTrace();

            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }

            try {
//                Debug.trace("JSON Result:" + jsonResults.toString());
                // Create a JSON object hierarchy from the results
                JSONObject jsonObj = new JSONObject(jsonResults.toString());
                JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");

                // Extract the Place descriptions from the results
                resultList = new ArrayList<>(predsJsonArray.length());
                com.pinpoint.appointment.models.Places mPlace;
                for (int i = 0; i < predsJsonArray.length(); i++) {
                    mPlace = new com.pinpoint.appointment.models.Places();
                    mPlace.setDescription(predsJsonArray.getJSONObject(i).getString("description"));
                    mPlace.setPlace_id(predsJsonArray.getJSONObject(i).getString("place_id"));
                    resultList.add(mPlace);
                }
            } catch (JSONException e) {
                Debug.trace("Cannot process JSON results:" + e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            placeAdapter = new AdpPlacesNew(parent, resultList);
            propertyBindng.acTxtCityState.setAdapter(placeAdapter);

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CAMERA:
                if (resultCode == Activity.RESULT_OK) {
                    String Message = "From: AddPropertyActivity , On image selected from Camera";
//                    LeadDetails.setMessage(AddPropertyActivity.this, Message);
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
            case CROP_PIC_REQUEST_CODE:
                if (data != null) {
                    Bundle extras = data.getExtras();
                    Bitmap bitmap = extras.getParcelable("data");
                    int n = 10000;
                    Random generator = new Random();
                    FileOutputStream outStream = null;
                    n = generator.nextInt(n);
                    String fileName = n + "image.jpg";
                    final File file1 = new File(getFilesDir(), fileName);
                    try {
                        outStream = new FileOutputStream(file1);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream); // bmp is your Bitmap instance
                    selectedImagePath = file1.getAbsolutePath();
                    propertyBindng.ivImgProeprty.setImageBitmap(BitmapFactory.decodeFile(selectedImagePath));

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
                    String Message = "From: AddPropertyActivity , After Image cropped : Successfully";
//                    LeadDetails.setMessage(AddPropertyActivity.this, Message);
                    Uri resultUri = result.getUri();
                    File cropFile = new File(resultUri.getPath());

                    if (!TextUtils.isEmpty(cropFile.toString())) {

                        if (cropFile.exists())
                            selectedImagePath = cropFile.getAbsolutePath();

                        if (selectedImagePath != null) {

                            propertyBindng.ivImgProeprty.setImageBitmap(BitmapFactory.decodeFile(selectedImagePath));
                        } else {
                            ToastHelper.displayInfo(MessageHelper.getInstance().getAppMessage(getString(R.string.str_msg_image_pick_issue)));
                        }
                    } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                        String Messagea = "From: AddPropertyActivity , After Image cropped : " + result.getError().toString();
//                        LeadDetails.setMessage(AddPropertyActivity.this, Messagea);
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

    public String getFinalAddress(String address) {
        String finalAddress = address;
        String postalCode = "";
        final Geocoder gcd = new Geocoder(context);
        try {

            finalAddress = address.replace(", USA", "");
            finalAddress = address.replace(",USA", "");
            finalAddress = address.replace(", usa", "");
            finalAddress = address.replace(", Usa", "");
            finalAddress = address.replace(",Usa", "");
            finalAddress = address.replace(", India", "");
//

        } catch (Exception e) {
            finalAddress = address.replace(", USA", "");
            finalAddress = address.replace(", India", "");
            finalAddress = address.replace(", Usa", "");
            e.printStackTrace();
        }
        return finalAddress;
    }

    public void onCaptureImageResult() {
        Intent intent = new Intent(AddPropertyActivity.this, CropImageActivity.class);
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
//                intent.putExtra(CropImageActivity.IMAGE_PATH, picPath);
//                intent.putExtra(CropImageActivity.SHAPE, GraphicsUtil.SHAPE_SQUARE);
//                if (width > 0 && height > 0) {
//                    float ratio = height / width;
//                    intent.putExtra(CropImageActivity.BITMAP_RATIO, ratio);
//                }
//
//                intent.putExtra(CropImageActivity.DIRECTORY_PATH, GraphicsUtil.CAPTURED_DIRECTORY_NAME);
//                startActivityForResult(intent, REQUEST_CROPPED_IMAGE);
            }

        }
    }

    public Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height,
                matrix, false);
        return resizedBitmap;
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
//            CropImage.activity()
//                    .setGuidelines(CropImageView.Guidelines.ON)
//                    .start(this);

// start cropping activity for pre-acquired image saved on the device
//            int length=screenWH[1]-40;
//            int length=imageWidth-100;
//            CropImage.activity(selectedImage).setRequestedSize(length,length).setMaxZoom(1).setMaxCropResultSize(length,length).setMinCropResultSize(length,length).setAutoZoomEnabled(false)
//                    .start(this);
            int length = 500;
            if (imageWidth < imageHeight) {
                length = imageWidth - 100;
            } else {
                length = imageHeight - 100;
            }
            String Message = "From: AddPropertyActivity , On image selected from gallery";
//            LeadDetails.setMessage(AddPropertyActivity.this, Message);
            CropImage.activity(selectedImage).setRequestedSize(400, 400).setMaxZoom(1).setAutoZoomEnabled(false).setMaxCropResultSize(length, length).setCropMenuCropButtonTitle(MessageHelper.getInstance().getAppMessage(getString(R.string.str_done))).setMinCropResultSize(length, length)
                    .start(this);
// for fragment (DO NOT use `getActivity()`)
//            CropImage.activity()
//                    .start( this);
//            String picPath = GraphicsUtil.getInstance().saveImage(bitmap, GraphicsUtil.CONTEST_DIRECTORY);
//            doCrop(Uri.parse(picPath));

//            Intent intent;
//            if(Util.isTabletDevice(AddPropertyActivity.this))
//            {
//                 intent= new Intent(parent, CropImageActivity.class);
//            }
//            else
//            {
//                intent= new Intent(parent, CropImageActivity.class);
//            }
//
//            if (imagePath.length() > 0) {
//                String picPath = GraphicsUtil.getInstance().saveImage(bitmap, GraphicsUtil.CONTEST_DIRECTORY);
//                intent.putExtra(CropImageActivity.IMAGE_PATH, picPath);
//                intent.putExtra(CropImageActivity.SHAPE, GraphicsUtil.SHAPE_SQUARE);
//                intent.putExtra(CropImageActivity.BITMAP_RATIO, (float) 0 / 0);
//                intent.putExtra(CropImageActivity.DIRECTORY_PATH, GraphicsUtil.CONTEST_DIRECTORY);
//                startActivityForResult(intent, REQUEST_CROPPED_IMAGE);
//            }
        } else {
//            CustomDialog.getInstance().showAlert(, MessageHelper.getInstance().getAppMessage(parent.getResources().getString(R
//                    .string.str_err_image_size)), null);

        }

    }

    private void doCrop(Uri picUri) {
        try {

            Intent cropIntent = new Intent("com.android.camera.action.CROP");

            cropIntent.setDataAndType(picUri, "image/*");
            cropIntent.putExtra("crop", "true");
            cropIntent.putExtra("aspectX", 1);
            cropIntent.putExtra("aspectY", 1);
            cropIntent.putExtra("outputX", 200);
            cropIntent.putExtra("outputY", 200);
            cropIntent.putExtra("return-data", true);

            startActivityForResult(cropIntent, CROP_PIC_REQUEST_CODE);
        }
        // respond to users whose devices do not support the crop action
        catch (ActivityNotFoundException anfe) {
            // display an error message
            String errorMessage = "Whoops - your device doesn't support the crop action!";
            Toast toast = Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    public void afterCroppedImageResult(Intent data) {
        File cropFile = new File(data.getStringExtra(CropImageActivity.IMAGE_PATH));

        if (!TextUtils.isEmpty(cropFile.toString())) {

            if (cropFile.exists())
                selectedImagePath = cropFile.getAbsolutePath();

            if (selectedImagePath != null) {

                propertyBindng.ivImgProeprty.setImageBitmap(BitmapFactory.decodeFile(selectedImagePath));
            } else {
                ToastHelper.displayInfo(MessageHelper.getInstance().getAppMessage(getString(R.string.str_msg_image_pick_issue)));
            }

            Debug.trace("resize wh " + "screenWidth " + screenWH[0] + " " + "screenHeight " + screenWH[0] * BaseConstants.VALUE_IMAGE_RATIO);
        }

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
//        mPlaceArrayAdapter.setGoogleApiClient(mGoogleApiClient);

    }

    @Override
    public void onConnectionSuspended(int i) {
//        mPlaceArrayAdapter.setGoogleApiClient(null);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

}
