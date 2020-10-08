package com.pinpoint.appointment.models;
import android.app.Activity;

import com.android.volley.Request;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.pinpoint.appointment.api.ApiList;
import com.pinpoint.appointment.api.RequestCode;
import com.pinpoint.appointment.api.RequestListener;
import com.pinpoint.appointment.api.RestClient;
import com.pinpoint.appointment.interfaces.DataObserver;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.TimeZone;

public class PanicData {

    @SerializedName("senderId")
    @Expose
    private String senderId;
    @SerializedName("senderName")
    @Expose
    private String senderName;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("contact")
    @Expose
    private String contact;
    @SerializedName("address")
    @Expose
    private String address;
    @SerializedName("deviceToken")
    @Expose
    private String deviceToken;
    @SerializedName("deviceType")
    @Expose
    private String deviceType;
    @SerializedName("latitude")
    @Expose
    private Double latitude;
    @SerializedName("longitude")
    @Expose
    private Double longitude;

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getPhoneNo() {
        return contact;
    }

    public void setPhoneNo(String phoneNo) {
        this.contact = phoneNo;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDeviceToken() {
        return deviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public Double getLat() {
        return latitude;
    }

    public void setLat(Double lat) {
        this.latitude = lat;
    }

    public Double getLong() {
        return longitude;
    }

    public void setLong(Double _long) {
        this.longitude = _long;
    }

    public static void callPanicPressed(final Activity activity, final DataObserver dataObserver, double latitude,double longitude)
    {
        try
        {  TimeZone timezone= TimeZone.getDefault();
            String timezoneName=timezone.getID();
            JSONObject param = new JSONObject();
            param.put(ApiList.KEY_USERID, Integer.parseInt(LoginHelper.getInstance().getUserID()));
            param.put(ApiList.KEY_MODE,"0");
            param.put(ApiList.KEY_LATITUDE,String.valueOf(latitude));
            param.put(ApiList.KEY_LONGITUDE,String.valueOf(longitude));

            param.put(ApiList.KEY_TIMEZONE,timezoneName);

            RestClient.getInstance().post(activity, Request.Method.POST, ApiList.APIs.panic.getUrl(), param,
                    new RequestListener() {
                        @Override
                        public void onComplete(RequestCode requestCode, Object object) {
//                            setCheckVersionModel((CheckVersion) object);
                            dataObserver.OnSuccess(requestCode, object);
                        }

                        @Override
                        public void onException(String error, String status, RequestCode requestCode) {
                            dataObserver.OnFailure(requestCode, status, error);
                        }

                        @Override
                        public void onOtherStatus(RequestCode requestCode, Object object) {
                            dataObserver.onOtherStatus(requestCode, object);
                        }
                    }, RequestCode.panic, false);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void callCheckPaymentStatus(final Activity activity, final DataObserver dataObserver)
    {
        try
        {
            JSONObject param = new JSONObject();
            param.put(ApiList.KEY_USERID, Integer.parseInt(LoginHelper.getInstance().getUserID()));




            RestClient.getInstance().post(activity, Request.Method.POST, ApiList.APIs.checkpaymentstatus.getUrl(), param,
                    new RequestListener() {
                        @Override
                        public void onComplete(RequestCode requestCode, Object object) {
//                            setCheckVersionModel((CheckVersion) object);
                            dataObserver.OnSuccess(requestCode, object);
                        }

                        @Override
                        public void onException(String error, String status, RequestCode requestCode) {
                            dataObserver.OnFailure(requestCode, status, error);
                        }

                        @Override
                        public void onOtherStatus(RequestCode requestCode, Object object) {
                            dataObserver.onOtherStatus(requestCode, object);
                        }
                    }, RequestCode.checkpaymentstatus, true);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
