package com.pinpoint.appointment.models;


import android.app.Activity;
import android.content.Context;

import com.android.volley.Request;
import com.pinpoint.appointment.api.ApiList;
import com.pinpoint.appointment.api.MultipartRequest;
import com.pinpoint.appointment.api.RequestCode;
import com.pinpoint.appointment.api.RequestListener;
import com.pinpoint.appointment.api.RestClient;
import com.pinpoint.appointment.interfaces.DataObserver;
import com.pinpoint.appointment.utils.Util;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

public class LeadDetails implements Serializable {

    private String id;

    public String getpId() {
        return pId;
    }

    public void setpId(String pId) {
        this.pId = pId;
    }

    private String pId;
    private String name;
    private String profileImage;
    private String contact;
    private String email;
    private String duration;
    private String status;
    private String date;
    private String date_format;

    private String company;

    public int getLeadCount() {
        return leadCount;
    }

    public void setLeadCount(int leadCount) {
        this.leadCount = leadCount;
    }

    private int leadCount;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPId() {
        return pId;
    }

    public void setPId(String pId) {
        this.pId = pId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDate() {
        return date;
    }
    public String getDateFormat() {
        return date_format;
    }

    public void setDate(String date) {
        this.date = date;
    }
    public void setDateFormat(String date_format) {
        this.date_format = date_format;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public static void addLead(final Activity activity, final LeadDetails leadDetails, String filepath, final boolean isAdd, final DataObserver dataObserver) {
        try {

            List<NameValuePair> nameValuePairs = new ArrayList<>();

///storage/emulated/0/Codebase/Images/IMG_1526656238891.jpg
            nameValuePairs.add(new BasicNameValuePair(ApiList.KEY_NAME, leadDetails.getName()));
            nameValuePairs.add(new BasicNameValuePair(ApiList.KEY_EMAIL, leadDetails.getEmail()));
            nameValuePairs.add(new BasicNameValuePair(ApiList.KEY_CONTACT, leadDetails.getContact()));
            nameValuePairs.add(new BasicNameValuePair(ApiList.KEY_COMPANY, leadDetails.getCompany()));
            nameValuePairs.add(new BasicNameValuePair(ApiList.KEY_TIMEFRAME, leadDetails.getDuration()));
            nameValuePairs.add(new BasicNameValuePair(ApiList.KEY_PID, leadDetails.getPId()));
//            nameValuePairs.add(new BasicNameValuePair(ApiList.KEY_DEVICE_TOKEN_CLIENT, PrefHelper.getString(PrefHelper.KEY_DEVICE_TOKEN, "")));
//            nameValuePairs.add(new BasicNameValuePair(ApiList.KEY_DEVICE_HWID, String.valueOf("")));
//            if (isAdd) {
//                nameValuePairs.add(new BasicNameValuePair(ApiList.KEY_DEVICE_TOKEN, String.valueOf(PrefHelper.getString(PrefHelper.KEY_DEVICE_TOKEN, ""))));
//                nameValuePairs.add(new BasicNameValuePair(ApiList.KEY_DEVICETYPE, String.valueOf(BaseConstant.DEVICE_TYPE)));
//                nameValuePairs.add(new BasicNameValuePair(ApiList.KEY_APP_VERSION, String.valueOf(BaseConstant.DEVICE_TYPE)));
//                nameValuePairs.add(new BasicNameValuePair(ApiList.KEY_DEVICE_DETAILS,
//                        "Android V:" + Utils.getInstance().getAppVersionName()
//                                + ", OS :" + Build.VERSION.RELEASE
//                                + ", Model:" + Build.MODEL
//                                + ", Brand:" + Build.BRAND
//                                + ", Device:" + Build.DEVICE
//                                + ", Id:" + Build.ID
//                                + ", Product:" + Build.PRODUCT
//                                + ", SDK:" + Build.VERSION.SDK_INT
//                                + ", Release:" + Build.VERSION.RELEASE));
//            }

            MultipartRequest restClient = new MultipartRequest(activity, nameValuePairs, RequestCode.addlead, new RequestListener() {
                @Override
                public void onComplete(RequestCode requestCode, Object object) {
                    String response = String.valueOf(object);
                    JSONObject mJobjResponse = null;
                    try {


                        mJobjResponse = new JSONObject(response);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

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
            }, false, true);
            if (filepath != null && !filepath.equalsIgnoreCase("")) {
                restClient.execute(MultipartRequest.REQUEST_POSTIMAGE, ApiList.APIs.addlead.getUrl(), filepath);
            } else {
                restClient.execute(MultipartRequest.REQUEST_POSTPAIR, ApiList.APIs.addlead.getUrl(), filepath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void getLeadList(final Activity activity, int pageNo, int limit, int pId, final DataObserver dataObserver) {
        try {
//            {"pageNo":1,"limit":"10","pId":"135"}
            TimeZone timezone= TimeZone.getDefault();
            String timezoneName=timezone.getID();
            JSONObject param = new JSONObject();
            param.put(ApiList.KEY_PAGENO, pageNo);
            param.put(ApiList.KEY_LIMIT, limit);
            param.put(ApiList.KEY_TIMEZONE,timezoneName);
            param.put(ApiList.KEY_PID, pId);

            RestClient.getInstance().post(activity, Request.Method.POST, ApiList.APIs.getleads.getUrl(), param, new RequestListener() {
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
            }, RequestCode.getleads, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void deleteLead(final Activity activity, int leadId, final DataObserver dataObserver) {
        try {
            JSONObject param = new JSONObject();
            param.put(ApiList.KEY_ID, leadId);


            RestClient.getInstance().post(activity, Request.Method.POST, ApiList.APIs.deletelead.getUrl(), param, new RequestListener() {
                @Override
                public void onComplete(RequestCode requestCode, Object object) {

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
            }, RequestCode.deletelead, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setMessage(Context activity, String message) {
        try {
            JSONObject param = new JSONObject();

            param.put(ApiList.KEY_MESSAGE, LoginHelper.getInstance().getUserID() + " \n"
                    + LoginHelper.getInstance().getName() + " \n\n" + message + "\n\n\n" + Util.getDeviceDetails());

            RestClient.getInstance().postForService(activity, Request.Method.POST, ApiList.APIs.crashreportlog.getUrl(), param, new RequestListener() {
                @Override
                public void onComplete(RequestCode requestCode, Object object) {
//                    ToastHelper.displayCustomToast("Message sent");
//                    dataObserver.OnSuccess(requestCode, object);
                }

                @Override
                public void onException(String statusCode, String error, RequestCode requestCode) {
//                    dataObserver.OnFailure(requestCode, statusCode, error);
                }

                @Override
                public void onOtherStatus(RequestCode requestCode, Object object) {
//                    dataObserver.onOtherStatus(requestCode, object);
                }
            }, RequestCode.crashreportlog, false);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setMessageLog(Context activity, String message) {
        try {
//            JSONObject param = new JSONObject();
//
//            param.put(ApiList.KEY_MESSAGE, LoginHelper.getInstance().getUserID() + " \n"
//                    + LoginHelper.getInstance().getName() + " \n\n" + message + "\n\n\n" + Util.getDeviceDetails());
//
//            RestClient.getInstance().postForService(activity, Request.Method.POST, ApiList.APIs.crashreportlog.getUrl(), param, new RequestListener() {
//                @Override
//                public void onComplete(RequestCode requestCode, Object object) {
////                    ToastHelper.displayCustomToast("Message sent");
////                    dataObserver.OnSuccess(requestCode, object);
//                }
//
//                @Override
//                public void onException(String statusCode, String error, RequestCode requestCode) {
////                    dataObserver.OnFailure(requestCode, statusCode, error);
//                }
//
//                @Override
//                public void onOtherStatus(RequestCode requestCode, Object object) {
////                    dataObserver.onOtherStatus(requestCode, object);
//                }
//            }, RequestCode.crashreportlog, false);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}