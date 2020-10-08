package com.pinpoint.appointment.fragment;


import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;

import androidx.databinding.DataBindingUtil;

import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.hbb20.CountryCodePicker;
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
import com.pinpoint.appointment.databinding.FragmentAddAppointmentBinding;
import com.pinpoint.appointment.enumeration.CalendarDateSelection;
import com.pinpoint.appointment.helper.MessageHelper;
import com.pinpoint.appointment.helper.PrefHelper;
import com.pinpoint.appointment.interfaces.ClickEvent;
import com.pinpoint.appointment.interfaces.DataObserver;
import com.pinpoint.appointment.models.AppointmentData;

import com.pinpoint.appointment.models.Places;
import com.pinpoint.appointment.utils.Debug;
import com.pinpoint.appointment.utils.GPSTracker;
import com.pinpoint.appointment.utils.Util;
import com.pinpoint.appointment.validator.ValidationClass;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentAddAppointment extends BaseActivity implements ClickEvent, DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener, GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks {


    private FragmentAddAppointment parent;
    private String strPickerDate, strPickerTime, strExpireDate, strExpireTime;
    private Bundle bundle;
    String selectedAddress = "";
    FragmentAddAppointmentBinding addAppointmentBinding;
    String strName, strPhone, strLocation;
    private static final int GOOGLE_API_CLIENT_ID = 0;
    private GoogleApiClient mGoogleApiClient;
    private PlaceArrayAdapter mPlaceArrayAdapter;
    private static final LatLngBounds BOUNDS_MOUNTAIN_VIEW = new LatLngBounds(
            new LatLng(37.398160, -122.180831), new LatLng(37.430610, -121.972090));
    AdpPlacesNew placeAdapter;
    private CountryCodePicker ccp;

    public FragmentAddAppointment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addAppointmentBinding = DataBindingUtil.setContentView(this, R.layout.fragment_add_appointment);
        if (Util.isTabletDevice(this)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        ImageView imgBack = GenericView.findViewById(this, R.id.iv_imgMenu_Back);
        ImageView imgSettings = GenericView.findViewById(this, R.id.iv_imgSetting);
        TextView tvHeader = GenericView.findViewById(this, R.id.tv_txtTitle);
        tvHeader.setText(MessageHelper.getInstance().getAppMessage(getString(R.string.str_add_appointment)));

        ccp = findViewById(R.id.ccp);
        ccp.setDefaultCountryUsingNameCode("US");

        tvHeader.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_SEMIBOLD));
        addAppointmentBinding.etEdtPhone.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));
        addAppointmentBinding.etEdtName.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));
        addAppointmentBinding.tvTime.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));
        addAppointmentBinding.tvDate.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));
        addAppointmentBinding.acTxtCityState.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));
        addAppointmentBinding.btSignup.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_SEMIBOLD));

        addAppointmentBinding.inputLayoutLocation.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));
        addAppointmentBinding.inputLayoutName.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));
        addAppointmentBinding.inputLayoutPhone.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));

        addAppointmentBinding.inputLayoutPhone.setHint(MessageHelper.getInstance().getAppMessage(getString(R.string.str_phone)));
        addAppointmentBinding.inputLayoutName.setHint(MessageHelper.getInstance().getAppMessage(getString(R.string.str_name)));
        addAppointmentBinding.inputLayoutLocation.setHint(MessageHelper.getInstance().getAppMessage(getString(R.string.str_enter_location_label)));
        addAppointmentBinding.tvTime.setHint(MessageHelper.getInstance().getAppMessage(getString(R.string.str_enter_time)));
        addAppointmentBinding.tvDate.setHint(MessageHelper.getInstance().getAppMessage(getString(R.string.str_selectDate)));
        addAppointmentBinding.btSignup.setText(MessageHelper.getInstance().getAppMessage(getString(R.string.str_submit)));

        imgBack.setImageResource(R.mipmap.ic_back);
        imgSettings.setVisibility(View.INVISIBLE);
        parent = this;

        setupLocationAutocomplete();
        GPSTracker gpsTracker = new GPSTracker(parent);
        if (gpsTracker.canGetLocation()) {

            BaseConstants.LATITUDE = gpsTracker.getLatitude();
            BaseConstants.LONGITUDE = gpsTracker.getLongitude();
        }
        gpsTracker.stopUsingGPS();
        this.stopService(new Intent(parent, GPSTracker.class));

        Util.setupOutSideTouchHideKeyboard(GenericView.findViewById(this, R.id.parent));
    /*    if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            addAppointmentBinding.etEdtPhone.addTextChangedListener(new PhoneNumberFormattingTextWatcher("US"));
        } else {
            addAppointmentBinding.etEdtPhone.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
        }*/
        ccp.setOnCountryChangeListener(new CountryCodePicker.OnCountryChangeListener() {
            @Override
            public void onCountrySelected() {

            }
        });


    }

    private void setupLocationAutocomplete() {

        addAppointmentBinding.acTxtCityState.setThreshold(1);


        ArrayList<Places> resultList = new ArrayList<>();
        placeAdapter = new AdpPlacesNew(this, resultList);
        try {
            addAppointmentBinding.acTxtCityState.addTextChangedListener(new TextWatcher() {
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
        addAppointmentBinding.acTxtCityState.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Places mPlaces = (Places) adapterView.getItemAtPosition(position);


                Util.hideKeyboard(addAppointmentBinding.acTxtCityState);

                String finalAddRess = getFinalAddress(mPlaces.getDescription());
                selectedAddress = mPlaces.getDescription();
                addAppointmentBinding.acTxtCityState.setText(finalAddRess);
//                addAppointmentBinding.acTxtCityState.setSelection(addAppointmentBinding.acTxtCityState.getText().length());
            }
        });

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
//            if(postalCode!=null&&!postalCode.equalsIgnoreCase("")&&!finalAddress.contains(postalCode))
//            finalAddress=finalAddress+", "+postalCode;

        } catch (Exception e) {
            finalAddress = address.replace(", USA", "");
            finalAddress = address.replace(", India", "");
            finalAddress = address.replace(", Usa", "");
            e.printStackTrace();
        }
        return finalAddress;
    }


    private class getPlaces extends AsyncTask<String, Void, Void> {
        private final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
        private final String TYPE_AUTOCOMPLETE = "/autocomplete";
        private final String TYPE_NEARBYSEARCH = "/nearbysearch";
        private final String OUT_JSON = "/json";
        ArrayList<Places> resultList = null;

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


                System.out.println("map place link" + sb);

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
                Places mPlace;
                for (int i = 0; i < predsJsonArray.length(); i++) {
                    mPlace = new Places();
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
            addAppointmentBinding.acTxtCityState.setAdapter(placeAdapter);

        }
    }


    @Override
    public void onClickEvent(View view) {
        switch (view.getId()) {
            case R.id.rl_Date:
                Date date = new Date();
                Calendar c = Calendar.getInstance();
                c.setTime(date);
                CustomDialog.showDatePickerDialog(parent, this, CalendarDateSelection.CALENDAR_WITH_FUTURE_DATE,
                        c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH));
                break;

            case R.id.rl_Time:
                CustomDialog.showTimePickerDialog(parent, this);
                break;
            case R.id.iv_imgMenu_Back:
                finish();
                break;
            case R.id.bt_signup:
                if (checkValidation()) {
//                    {"agentid":"730","clientid":"","date":"2018-05-24","time":"14:00","information":"Test","address":"New York","contact":"9726443657","name":"Test","mode":"1"}

                    callApi();

                }

                break;
        }

    }

    private void callApi() {
//      String phonenumber=addAppointmentBinding.etEdtPhone.getText().toString().replaceAll("[^\\d]", "");
        String orignalPhoneNumber = addAppointmentBinding.etEdtPhone.getText().toString();
        String phonenumber = orignalPhoneNumber.replace("(", "");
        phonenumber = phonenumber.replace(")", "");
        phonenumber = phonenumber.replace(" ", "");
        phonenumber = phonenumber.replace("-", "");
        AppointmentData.addAppointment(this, strPickerDate, strPickerTime, ccp.getSelectedCountryCodeWithPlus() + phonenumber, addAppointmentBinding.etEdtName.getText().toString(), addAppointmentBinding.acTxtCityState.getText().toString(), selectedAddress, "info", 1, dataObserver);
    }

    private boolean checkValidation() {

        strName = addAppointmentBinding.etEdtName.getText().toString().trim();
        strPhone = addAppointmentBinding.etEdtPhone.getText().toString().trim();
        strLocation = addAppointmentBinding.acTxtCityState.getText().toString().trim();


        if (TextUtils.isEmpty(strName)) {
            CustomDialog.getInstance().showAlert(this, MessageHelper.getInstance().getAppMessage(getString(R.string.str_enter_name)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_validation_dialog_header)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_ok)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_dismiss)), false);

            return false;
        } else if (TextUtils.isEmpty(strPhone)) {
            CustomDialog.getInstance().showAlert(this, MessageHelper.getInstance().getAppMessage(getString(R.string.str_enter_phone)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_validation_dialog_header)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_ok)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_dismiss)), false);

            return false;
        } else if (!ValidationClass.isValidPhoneNumber(strPhone)) {
            CustomDialog.getInstance().showAlert(this, MessageHelper.getInstance().getAppMessage(getString(R.string.str_valid_phone)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_validation_dialog_header)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_ok)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_dismiss)), false);

            return false;
        } else if (TextUtils.isEmpty(strLocation)) {
            CustomDialog.getInstance().showAlert(this, MessageHelper.getInstance().getAppMessage(getString(R.string.str_enter_location)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_validation_dialog_header)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_ok)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_dismiss)), false);

            return false;
        } else if (TextUtils.isEmpty(strPickerDate)) {
            CustomDialog.getInstance().showAlert(this, MessageHelper.getInstance().getAppMessage(getString(R.string.str_select_appointment_date)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_validation_dialog_header)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_ok)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_dismiss)), false);

            return false;
        } else if (TextUtils.isEmpty(strPickerTime)) {
            CustomDialog.getInstance().showAlert(this, MessageHelper.getInstance().getAppMessage(getString(R.string.str_select_appointment_time)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_validation_dialog_header)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_ok)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_dismiss)), false);

            return false;
        } else if (!checkCurrentDate()) {
            return false;
        } else {
            return true;
//            callRegisterApi();
        }

    }

    @Override
    public void onDateSet(DatePicker datePicker, int selectedYear, int selectedMonth, int selectedDay) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(selectedYear, selectedMonth, selectedDay);
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat format = new SimpleDateFormat(BaseConstants.PICKDATEFORMAT_API);
        SimpleDateFormat format_display = new SimpleDateFormat(BaseConstants.PICKDATEFORMAT);
        strPickerDate = format.format(calendar.getTime());
        addAppointmentBinding.tvDate.setText(format_display.format(calendar.getTime()));
    }

    @Override
    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedminute) {
        if (timePicker.isShown()) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, selectedHour);
            calendar.set(Calendar.MINUTE, selectedminute);
            SimpleDateFormat format = new SimpleDateFormat(BaseConstants.PICKTIMEFORMAT);
            SimpleDateFormat displayFormat = new SimpleDateFormat(BaseConstants.DISPLAYPICKTIMEFORMAT);
            strPickerTime = format.format(calendar.getTime());
            String pickerDisplayTime = displayFormat.format(calendar.getTime());
            if (checkCurrentDate()) {
                addAppointmentBinding.tvTime.setText(pickerDisplayTime);
            }
        }
    }

    private boolean checkCurrentDate() {
        SimpleDateFormat format1 = new SimpleDateFormat(BaseConstants.PICKDATETIMEFORMAT);
        try {
            Date selectedDate = format1.parse(strPickerDate + " " + strPickerTime);
            Date currentdATE = format1.parse(String.valueOf(format1.format(new Date())));
            if (selectedDate.before(currentdATE)) {

                CustomDialog.getInstance().showAlert(this, MessageHelper.getInstance().getAppMessage(getString(R.string.str_select_futuredate)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_validation_dialog_header)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_ok)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_dismiss)), false);
                return false;
            }


        } catch (ParseException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mPlaceArrayAdapter.setGoogleApiClient(mGoogleApiClient);

    }

    @Override
    public void onConnectionSuspended(int i) {
        mPlaceArrayAdapter.setGoogleApiClient(null);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    DataObserver dataObserver = new DataObserver() {
        @Override
        public void OnSuccess(RequestCode requestCode, Object object) {
            switch (requestCode) {
                case addappointment:
                    CustomDialog.getInstance().hide();
                    String response = object.toString();
                    try {
                        JSONObject jsonObject = new JSONObject(response);
//                      JSONObject jsonObject1=jsonObject.getJSONObject(ApiList.KEY_RESPONSE);
                        String message = jsonObject.getString(ApiList.KEY_MESSAGE);
//                      ToastHelper.displayCustomToast(message);
                        CustomDialog.getInstance().showAlertWithButtonClick(FragmentAddAppointment.this, message
                                , MessageHelper.getInstance().getAppMessage(getString(R.string.str_app_name))
                                , MessageHelper.getInstance().getAppMessage(getString(R.string.str_ok))
                                ,
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        CustomDialog.getInstance().hide();
                                        BaseConstants.REFRESH_APPOINTMENT = true;
                                        PrefHelper.setBoolean("isnotification", false);
                                        BaseConstants.SELECT_SENT = true;
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
            switch (requestCode) {
                case addappointment:
                    CustomDialog.getInstance().hide();
                    PrefHelper.setBoolean("isnotification", false);
                    CustomDialog.getInstance().showAlert(FragmentAddAppointment.this, error, errorCode, Util.getAppKeyValue(FragmentAddAppointment.this, R.string.lblOk), Util.getAppKeyValue(FragmentAddAppointment.this, R.string.str_dismiss), false);
                    break;
            }
        }

        @Override
        public void onOtherStatus(RequestCode requestCode, Object object) {

            switch (requestCode) {
                case addappointment:
                    CustomDialog.getInstance().hide();
                    PrefHelper.setBoolean("isnotification", false);
                    CustomDialog.getInstance().hide();
                    String response = object.toString();
                    try {
                        JSONObject jsonObject = new JSONObject(response);
//                      JSONObject jsonObject1=jsonObject.getJSONObject(ApiList.KEY_RESPONSE);
                        String message = jsonObject.getString(ApiList.KEY_MESSAGE);
                        CustomDialog.getInstance().showAlert(FragmentAddAppointment.this, message, "Error", Util.getAppKeyValue(FragmentAddAppointment.this, R.string.lblOk), Util.getAppKeyValue(FragmentAddAppointment.this, R.string.str_dismiss), false);

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

}
