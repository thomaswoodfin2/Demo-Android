package com.pinpoint.appointment.models;

import android.app.Activity;

import com.android.volley.Request;
import com.pinpoint.appointment.api.ApiList;
import com.pinpoint.appointment.api.RequestCode;
import com.pinpoint.appointment.api.RequestListener;
import com.pinpoint.appointment.api.RestClient;
import com.pinpoint.appointment.helper.PrefHelper;
import com.pinpoint.appointment.interfaces.DataObserver;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by admin on 12-12-2017.
 */

public class TrackingFriend {

    private String id;
    private String latitude;
    private String longitude;

    public String getOnlineStatus() {
        return onlineStatus;
    }

    public void setOnlineStatus(String onlineStatus) {
        this.onlineStatus = onlineStatus;
    }

    private String  onlineStatus;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    private String userId;

    public String getTrackingDateTime() {
        return trackingDateTime;
    }

    public void setTrackingDateTime(String trackingDateTime) {
        this.trackingDateTime = trackingDateTime;
    }

    private String trackingDateTime;
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }



    public static void getLastTrackingDetails(final Activity activity, String friendid,  final DataObserver dataObserver,boolean isDialogRequired) {
        try {

            JSONObject param = new JSONObject();
            param.put(ApiList.KEY_USERID, Integer.parseInt(friendid));

//          param.put(ApiList.KEY_USER_TYPE,userType);

            RestClient.getInstance().post(activity, Request.Method.POST, ApiList.APIs.getfriendlocation.getUrl(), param,
                    new RequestListener() {
                        @Override
                        public void onComplete(RequestCode requestCode, Object object)
                        {
                            dataObserver.OnSuccess(requestCode, object);
                        }

                        @Override
                        public void onException(String error, String status, RequestCode requestCode) {
                            dataObserver.OnFailure(requestCode, status, error);
                        }

                        @Override
                        public void onOtherStatus(RequestCode requestCode, Object object) {

                        }
                    }, RequestCode.getfriendlocation, isDialogRequired);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


}