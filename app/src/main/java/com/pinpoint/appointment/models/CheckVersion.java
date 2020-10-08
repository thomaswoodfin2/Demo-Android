package com.pinpoint.appointment.models;

import android.app.Activity;
import android.content.Context;
import android.os.Build;

import com.android.volley.Request;
import com.pinpoint.appointment.BaseConstants;
import com.pinpoint.appointment.BuildConfig;
import com.pinpoint.appointment.api.ApiList;
import com.pinpoint.appointment.api.RequestCode;
import com.pinpoint.appointment.api.RequestListener;
import com.pinpoint.appointment.api.RestClient;
import com.pinpoint.appointment.helper.PrefHelper;
import com.pinpoint.appointment.interfaces.DataObserver;
import com.pinpoint.appointment.utils.Util;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.TimeZone;

/**
 * Created on 18-07-2017.
 */

public class CheckVersion {
    private static CheckVersion checkVersion;

    private int logoutCustomer;
    private String updateMessage;
    private String url;
    private String currency;
    private int isUpdateType;
    private int isMessageUpdate;
    private int badgcount;

    public int getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(int paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    private int paymentStatus;

    public static CheckVersion getCheckVersionModel() {
        return checkVersion;
    }

    private static void setCheckVersionModel(CheckVersion mCheckVersion) {
        checkVersion = mCheckVersion;
        System.out.println(checkVersion);
    }

    /**
     * This method call checkAppVersion API by API call
     * with posting necessary post parameters
     */
    public static void checkAppVersion(final Activity activity, final DataObserver dataObserver) {
        try {
            TimeZone timezone = TimeZone.getDefault();
            String timezoneName = timezone.getID();
            StringBuilder errorReport = new StringBuilder();
            JSONObject param = new JSONObject();
            String LINE_SEPARATOR = "\n";
            param.put(ApiList.KEY_VERSIONCODE, BuildConfig.VERSION_CODE);
            param.put(ApiList.KEY_APP_VERSION, BuildConfig.VERSION_NAME);
            param.put(ApiList.KEY_DEVICETYPE, BaseConstants.DEVICE_TYPE);
            param.put(ApiList.KEY_APPLICATION_TYPE, BaseConstants.APP_TYPE);
            param.put(ApiList.KEY_TIMEZONE, timezoneName);
            param.put(ApiList.KEY_DEVICE_DETAILS,
                    "Android V:" + Util.getAppVersionName()
                            + ", OS :" + Build.VERSION.RELEASE
                            + ", Model:" + Build.MODEL
                            + ", Brand:" + Build.BRAND
                            + ", Device:" + Build.DEVICE
                            + ", Id:" + Build.ID
                            + ", Product:" + Build.PRODUCT
                            + ", SDK:" + Build.VERSION.SDK_INT
                            + ", Release:" + Build.VERSION.RELEASE);

            param.put(ApiList.KEY_DEVICE_TOKEN, PrefHelper.getString(PrefHelper.KEY_DEVICE_TOKEN, ""));
            if (LoginHelper.getInstance().getUserID() != null) {
                param.put(ApiList.KEY_USERID, LoginHelper.getInstance().getUserID());
            } else {
                param.put(ApiList.KEY_USERID, 0);
            }
            param.put(ApiList.KEY_MESSAGEUPDATE, PrefHelper.getString(PrefHelper.KEY_MESSAGE_UPDATE_DATE, ""));
            param.put(ApiList.KEY_LASTLOGINTIME, "");

            RestClient.getInstance().post(activity, Request.Method.POST, ApiList.APIs.checkAppVersion.getUrl(), param,
                    new RequestListener() {
                        @Override
                        public void onComplete(RequestCode requestCode, Object object) {
                            setCheckVersionModel((CheckVersion) object);
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
                    }, RequestCode.checkVersion, false);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void checkAppVersionService(final Context activity) {
        try {
            StringBuilder errorReport = new StringBuilder();
            JSONObject param = new JSONObject();
            String LINE_SEPARATOR = "\n";
            param.put(ApiList.KEY_VERSIONCODE, BuildConfig.VERSION_CODE);
            param.put(ApiList.KEY_APP_VERSION, BuildConfig.VERSION_NAME);
            param.put(ApiList.KEY_DEVICETYPE, BaseConstants.DEVICE_TYPE);
            param.put(ApiList.KEY_APPLICATION_TYPE, BaseConstants.APP_TYPE);


            param.put(ApiList.KEY_DEVICE_DETAILS,
                    "Android V:" + Util.getAppVersionName()
                            + ", OS :" + Build.VERSION.RELEASE
                            + ", Model:" + Build.MODEL
                            + ", Brand:" + Build.BRAND
                            + ", Device:" + Build.DEVICE
                            + ", Id:" + Build.ID
                            + ", Product:" + Build.PRODUCT
                            + ", SDK:" + Build.VERSION.SDK_INT
                            + ", Release:" + Build.VERSION.RELEASE);

            param.put(ApiList.KEY_DEVICE_TOKEN, PrefHelper.getString(PrefHelper.KEY_DEVICE_TOKEN, ""));
            if (LoginHelper.getInstance().getUserID() != null) {
                param.put(ApiList.KEY_USERID, LoginHelper.getInstance().getUserID());
            } else {
                param.put(ApiList.KEY_USERID, 0);
            }
            param.put(ApiList.KEY_MESSAGEUPDATE, PrefHelper.getString(PrefHelper.KEY_MESSAGE_UPDATE_DATE, ""));
            param.put(ApiList.KEY_LASTLOGINTIME, "");

            RestClient.getInstance().postForService(activity, Request.Method.POST, ApiList.APIs.checkAppVersion.getUrl(), param,
                    new RequestListener() {
                        @Override
                        public void onComplete(RequestCode requestCode, Object object) {
                            System.out.println("checkAppVersionService onComplete " + requestCode + ", " + object);

                            setCheckVersionModel((CheckVersion) object);
//                            dataObserver.OnSuccess(requestCode, object);
                        }

                        @Override
                        public void onException(String error, String status, RequestCode requestCode) {
                            System.out.println("checkAppVersionService onException " + status + ", " + error);

//                            dataObserver.OnFailure(requestCode, status, error);
                        }

                        @Override
                        public void onOtherStatus(RequestCode requestCode, Object object) {
                            System.out.println("checkAppVersionService onOtherStatus " + requestCode + ", " + object);
//                            dataObserver.onOtherStatus(requestCode, object);
                        }
                    }, RequestCode.checkVersion, false);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public int getBadgcount() {
        return badgcount;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUpdateMessage() {
        return updateMessage;
    }

    public void setUpdateMessage(String updateMessage) {
        this.updateMessage = updateMessage;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public int getIsUpdateType() {
        return isUpdateType;
    }

    public void setIsUpdateType(int isUpdateType) {
        this.isUpdateType = isUpdateType;
    }

    public int getIsMessageUpdate() {
        return isMessageUpdate;
    }

    public void setIsMessageUpdate(int isMessageUpdate) {
        this.isMessageUpdate = isMessageUpdate;
    }

    public int getLogoutCustomer() {
        return logoutCustomer;
    }

    public void setLogoutCustomer(int logoutCustomer) {
        this.logoutCustomer = logoutCustomer;
    }
}