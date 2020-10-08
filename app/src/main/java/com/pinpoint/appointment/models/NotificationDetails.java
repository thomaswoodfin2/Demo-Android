package com.pinpoint.appointment.models;


import android.app.Activity;

import com.android.volley.Request;
import com.pinpoint.appointment.api.ApiList;
import com.pinpoint.appointment.api.RequestCode;
import com.pinpoint.appointment.api.RequestListener;
import com.pinpoint.appointment.api.RestClient;
import com.pinpoint.appointment.enumeration.Friendstatus;
import com.pinpoint.appointment.interfaces.DataObserver;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.TimeZone;

public class NotificationDetails implements Serializable{

    public String getN_id() {
        return notiId;
    }

    public void setN_id(String n_id) {
        this.notiId = n_id;
    }

    public int getNote_flag() {
        return notiFlag;
    }

    public void setNote_flag(int note_flag) {
        this.notiFlag = note_flag;
    }

    private int notiFlag;
    private String notiId;
    private String post;
    private String id;
    private String name;
    private String contact;
    private String userType;
    private String latitude;
    private String longtitude;
    private String notiDate;
    private String notiTime;

    public String getTrackingEndTime() {
        return trackingEndTime;
    }

    public void setTrackingEndTime(String trackingEndTime) {
        this.trackingEndTime = trackingEndTime;
    }

    private String trackingEndTime;
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    private String message;
    private String profileImage;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    private int status;


    public boolean isSeleceted() {
        return isSeleceted;
    }

    public void setSeleceted(boolean seleceted) {
        isSeleceted = seleceted;
    }

    private boolean isSeleceted=false;
    public String getNId() {
        return notiId;
    }

    public void setNId(String nId) {
        this.notiId = nId;
    }

    public String getPost() {
        return post;
    }

    public void setPost(String post) {
        this.post = post;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getUsertype() {
        return userType;
    }

    public void setUsertype(String usertype) {
        this.userType = usertype;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongtitude() {
        return longtitude;
    }

    public void setLongtitude(String longtitude) {
        this.longtitude = longtitude;
    }

    public String getNotidate() {
        return notiDate;
    }

    public void setNotidate(String notidate) {
        this.notiDate = notidate;
    }

    public String getNotitime() {
        return notiTime;
    }

    public void setNotitime(String notitime) {
        this.notiTime = notitime;
    }

    public String getProfileimage() {
        return profileImage;
    }

    public void setProfileimage(String profileimage) {
        this.profileImage = profileimage;
    }

    public static void getFriendsDataNotificationData(final Activity activity, int page, int limit, final DataObserver dataObserver,boolean isDialogRequired) {
        try {
            TimeZone timezone= TimeZone.getDefault();
            String timezoneName=timezone.getID();
            JSONObject param = new JSONObject();
            param.put(ApiList.KEY_USERID, Integer.parseInt(LoginHelper.getInstance().getUserID()));
            param.put(ApiList.KEY_PAGE, page);
            param.put(ApiList.KEY_LIMIT, limit);
            param.put(ApiList.KEY_TIMEZONE, timezoneName);

            RestClient.getInstance().post(activity, Request.Method.POST, ApiList.APIs.getnotification.getUrl(), param, new RequestListener() {
                @Override
                public void onComplete(RequestCode requestCode, Object object) {
//                    LoginHelper.getInstance().doLogin((CustomerDetails) object);
                    dataObserver.OnSuccess(requestCode, object);
                }

                @Override
                public void onException(String statusCode, String error, RequestCode requestCode) {
                    dataObserver.OnFailure(requestCode, statusCode, error);
                }

                @Override
                public void onOtherStatus(RequestCode requestCode, Object object) {
                    dataObserver.onOtherStatus(requestCode, object);
                }
            }, RequestCode.getnotification, isDialogRequired);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void deleteNotification(final Activity activity, JSONArray notificationIds, final DataObserver dataObserver) {
        try {
            JSONObject param = new JSONObject();
            param.put(ApiList.KEY_USERID, Integer.parseInt(LoginHelper.getInstance().getUserID()));
            param.put(ApiList.KEY_NOTIFICATIONID, notificationIds);



            RestClient.getInstance().post(activity, Request.Method.POST, ApiList.APIs.deletenotification.getUrl(), param, new RequestListener() {
                @Override
                public void onComplete(RequestCode requestCode, Object object) {
//                    LoginHelper.getInstance().doLogin((CustomerDetails) object);
                    dataObserver.OnSuccess(requestCode, object);
                }

                @Override
                public void onException(String statusCode, String error, RequestCode requestCode) {
                    dataObserver.OnFailure(requestCode, statusCode, error);
                }

                @Override
                public void onOtherStatus(RequestCode requestCode, Object object) {
                    dataObserver.onOtherStatus(requestCode, object);
                }
            }, RequestCode.deletenotification, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void readNotification(final Activity activity,String notificationid, final DataObserver dataObserver) {
        try {
            JSONObject param = new JSONObject();
            param.put(ApiList.KEY_USERID, Integer.parseInt(LoginHelper.getInstance().getUserID()));
            param.put(ApiList.KEY_NOTIFICATIONID, notificationid);



            RestClient.getInstance().post(activity, Request.Method.POST, ApiList.APIs.readnotification.getUrl(), param, new RequestListener() {
                @Override
                public void onComplete(RequestCode requestCode, Object object) {
//                    LoginHelper.getInstance().doLogin((CustomerDetails) object);
                    dataObserver.OnSuccess(requestCode, object);
                }

                @Override
                public void onException(String statusCode, String error, RequestCode requestCode) {
                    dataObserver.OnFailure(requestCode, statusCode, error);
                }

                @Override
                public void onOtherStatus(RequestCode requestCode, Object object) {
                    dataObserver.onOtherStatus(requestCode, object);
                }
            }, RequestCode.readnotification, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}