package com.pinpoint.appointment.models;

import android.app.Activity;
import android.os.Build;

import com.android.volley.Request;
import com.pinpoint.appointment.BaseConstants;
import com.pinpoint.appointment.BuildConfig;
import com.pinpoint.appointment.activities.LoginActivity;
import com.pinpoint.appointment.api.ApiList;
import com.pinpoint.appointment.api.RequestCode;
import com.pinpoint.appointment.api.RequestListener;
import com.pinpoint.appointment.api.RestClient;
import com.pinpoint.appointment.enumeration.Friendstatus;
import com.pinpoint.appointment.helper.PrefHelper;
import com.pinpoint.appointment.interfaces.DataObserver;
import com.pinpoint.appointment.utils.Util;

import org.json.JSONObject;

import java.io.Serializable;

public class FriendDetails implements Serializable {

    private String userId;
    private String name;
    private String userLatitude;

    public FriendDetails() {
    }

    private static FriendDetails friendDetails;

    public static FriendDetails getFriendDetails() {
        return friendDetails;
    }

    public String getUser_lat() {
        return userLatitude;
    }

    public void setUser_lat(String user_lat) {
        this.userLatitude = user_lat;
    }

    public String getUser_lon() {
        return userLongtitude;
    }

    public void setUser_lon(String user_lon) {
        this.userLongtitude = user_lon;
    }

    public String getRequest_status() {
        return requestStatus;
    }

    public void setRequest_status(String request_status) {
        this.requestStatus = request_status;
    }

    public String getFriend_direction() {
        return friendDirection;
    }

    public void setFriend_direction(String friend_direction) {
        this.friendDirection = friend_direction;
    }

    public String getUser_status() {
        return userStatus;
    }

    public void setUser_status(String user_status) {
        this.userStatus = user_status;
    }

    private String userLongtitude;
    private String profileImage;
    private String phone;
    private String email;
    private String requestStatus;
    private String friendDirection;
    private String userStatus;

    public String getUserid() {
        return userId;
    }

    public void setUserid(String userid) {
        this.userId = userid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getProfileimage() {
        return profileImage;
    }

    public void setProfileimage(String profileimage) {
        this.profileImage = profileimage;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email == null? "" : email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public static void addFriendsData(final Activity activity, String phone, String name, int mode, final DataObserver dataObserver) {
        try {
            JSONObject param = new JSONObject();
            param.put(ApiList.KEY_SENDERID, LoginHelper.getInstance().getUserID());
            param.put(ApiList.KEY_CONTACT, phone);
            param.put(ApiList.KEY_NAME, name);
            param.put(ApiList.KEY_MODE, String.valueOf(mode));


            RestClient.getInstance().post(activity, Request.Method.POST, ApiList.APIs.addfriend.getUrl(), param, new RequestListener() {
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
            }, RequestCode.addfriend, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void acceptFriendRequest(final Activity activity, String friendId, int mode, final DataObserver dataObserver) {
        try {
            JSONObject param = new JSONObject();
            param.put(ApiList.KEY_LOGINID, LoginHelper.getInstance().getUserID());
            param.put(ApiList.KEY_FRIENDID, friendId);
            param.put(ApiList.KEY_MODE, String.valueOf(mode));


            RestClient.getInstance().post(activity, Request.Method.POST, ApiList.APIs.acceptfriendrequest.getUrl(), param, new RequestListener() {
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
            }, RequestCode.acceptfriendrequest, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void denyFriendRequest(final Activity activity, String friendId, int mode, final DataObserver dataObserver) {
        try {
            JSONObject param = new JSONObject();
            param.put(ApiList.KEY_LOGINID, LoginHelper.getInstance().getUserID());
            param.put(ApiList.KEY_FRIENDID, friendId);
            param.put(ApiList.KEY_MODE, String.valueOf(mode));


            RestClient.getInstance().post(activity, Request.Method.POST, ApiList.APIs.denyfriendrequest.getUrl(), param, new RequestListener() {
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
            }, RequestCode.denyfriendrequest, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void deleteFriendRequest(final Activity activity, String friendId, int mode, final DataObserver dataObserver) {
        try {
            JSONObject param = new JSONObject();
            param.put(ApiList.KEY_LOGINID, LoginHelper.getInstance().getUserID());
            param.put(ApiList.KEY_FRIENDID, friendId);


            RestClient.getInstance().post(activity, Request.Method.POST, ApiList.APIs.deletefriend.getUrl(), param, new RequestListener() {
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
            }, RequestCode.deletefriend, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void resendFriendRequest(final Activity activity, String friendId, int mode, final DataObserver dataObserver) {
        try {
            JSONObject param = new JSONObject();
            param.put(ApiList.KEY_AGENTID, LoginHelper.getInstance().getUserID());
            param.put(ApiList.KEY_CLIENTID, friendId);
            param.put(ApiList.KEY_MODE, String.valueOf(mode));

            RestClient.getInstance().post(activity, Request.Method.POST, ApiList.APIs.resendfriend.getUrl(), param, new RequestListener() {
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
            }, RequestCode.resendfriend, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void getFriendsData(final Activity activity, String start, String limit, int issent, final DataObserver dataObserver) {
        try {
            JSONObject param = new JSONObject();
            param.put(ApiList.KEY_USERID, LoginHelper.getInstance().getUserID());
            param.put(ApiList.KEY_START, start);
            param.put(ApiList.KEY_LIMIT, limit);

            /*
            if (issent == Friendstatus.RECEIVED.getType()) {
                param.put(ApiList.KEY_ISSENT, Friendstatus.SENT.getType());
            } else {
                param.put(ApiList.KEY_ISSENT, Friendstatus.RECEIVED.getType());
            }
            */

            RestClient.getInstance().post(activity, Request.Method.POST, ApiList.APIs.friendslist.getUrl(), param, new RequestListener() {
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
            }, RequestCode.friendslist, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}