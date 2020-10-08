package com.pinpoint.appointment.models;

import android.app.Activity;
import android.view.View;

import com.android.volley.Request;
import com.pinpoint.appointment.api.ApiList;
import com.pinpoint.appointment.api.RequestCode;
import com.pinpoint.appointment.api.RequestListener;
import com.pinpoint.appointment.api.RestClient;
import com.pinpoint.appointment.customviews.CustomDialog;
import com.pinpoint.appointment.enumeration.Friendstatus;
import com.pinpoint.appointment.fragment.FragmentAddAppointment;
import com.pinpoint.appointment.helper.PrefHelper;
import com.pinpoint.appointment.interfaces.DataObserver;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class AppointmentData implements Serializable {

    private String appointmentId;
    private String appointmentStatus;
    private String userId;
    private String name;
    private String profileImage;
    private String appointDate;
    private String appointTime;
    private String dateTime;
    private String address;
    private String phone;

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    private String startTime;
    private String endTime;
    private String appointDirection;

    public String getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(String appointmentId) {
        this.appointmentId = appointmentId;
    }

    public String getAppointmentStatus() {
        return appointmentStatus;
    }

    public void setAppointmentStatus(String appointmentStatus) {
        this.appointmentStatus = appointmentStatus;
    }

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

    public String getAppointDate() {
        return appointDate;
    }

    public void setAppointDate(String appointDate) {
        this.appointDate = appointDate;
    }

    public String getAppointTime() {
        return appointTime;
    }

    public void setAppointTime(String appointTime) {
        this.appointTime = appointTime;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAppointDirection() {
        return appointDirection;
    }

    public void setAppointDirection(String appointDirection) {
        this.appointDirection = appointDirection;
    }


    public static void getAppointmentData(final Activity activity, String pageNo, String limit, int issent, final DataObserver dataObserver) {
        try {
            TimeZone timezone = TimeZone.getDefault();
            String timezoneName = timezone.getID();
            JSONObject param = new JSONObject();
            param.put(ApiList.KEY_USERID, LoginHelper.getInstance().getUserID());
            param.put(ApiList.KEY_START, pageNo);
            param.put(ApiList.KEY_LIMIT, limit);
            param.put(ApiList.KEY_TIMEZONE, timezoneName);
            if (issent == Friendstatus.RECEIVED.getType()) {
                param.put(ApiList.KEY_ISSENT, Friendstatus.SENT.getType());
            } else {
                param.put(ApiList.KEY_ISSENT, Friendstatus.RECEIVED.getType());
            }
            param.put(ApiList.KEY_APP_DATE, "");
            Date today = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String todaysdate = sdf.format(today);

            param.put(ApiList.KEY_DEVICE_DATE, todaysdate);

            RestClient.getInstance().post(activity, Request.Method.POST, ApiList.APIs.appointmentlist.getUrl(), param, new RequestListener() {
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
            }, RequestCode.appointmentlist, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void addAppointment(FragmentAddAppointment fragmentAddAppointment, String strPickerDate, String strPickerTime, String phone, String name, String address, String fullAddress, String info, int mode, final DataObserver dataObserver) {
        try {
            if (fullAddress != null && fullAddress.equalsIgnoreCase("")) {
                fullAddress = address;
            }
            TimeZone timezone = TimeZone.getDefault();
            String timezoneName = timezone.getID();
            JSONObject param = new JSONObject();
            param.put(ApiList.KEY_AGENTID, LoginHelper.getInstance().getUserID());
            param.put(ApiList.KEY_CLIENTID, "");
            param.put(ApiList.KEY_DATE, strPickerDate);
            param.put(ApiList.KEY_TIME, strPickerTime);
            param.put(ApiList.KEY_INFORMATION, info);
            param.put(ApiList.KEY_ADDRESS, address);
            param.put(ApiList.KEY_CONTACT, phone);
            param.put(ApiList.KEY_NAME, name);
            param.put(ApiList.KEY_FULLADDRESS, fullAddress);
            param.put(ApiList.KEY_TIMEZONE, timezoneName);
            param.put(ApiList.KEY_MODE, String.valueOf(mode));

            RestClient.getInstance().post(fragmentAddAppointment, Request.Method.POST, ApiList.APIs.addappointment.getUrl(), param, new RequestListener() {
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
            }, RequestCode.addappointment, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void deleteAppointment(final Activity activity, String appointmentId, int mode, final DataObserver dataObserver) {
        try {
            JSONObject param = new JSONObject();
            param.put(ApiList.KEY_APPOINTMENTID, appointmentId);


            RestClient.getInstance().post(activity, Request.Method.POST, ApiList.APIs.deleteappointment.getUrl(), param, new RequestListener() {
                @Override
                public void onComplete(RequestCode requestCode, Object object) {
//                  LoginHelper.getInstance().doLogin((CustomerDetails) object);
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
            }, RequestCode.deleteappointment, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void acceptAppoinrment(final Activity activity, String appointmentId, int mode, final DataObserver dataObserver) {
        try {
            JSONObject param = new JSONObject();
            param.put(ApiList.KEY_APPOINTMENTID, appointmentId);
            param.put(ApiList.KEY_LOGINID, LoginHelper.getInstance().getUserID());
            param.put(ApiList.KEY_MODE, mode);
//            {"app_id":"684","loginid":"776","mode":"0"}


            RestClient.getInstance().post(activity, Request.Method.POST, ApiList.APIs.acceptappointment.getUrl(), param, new RequestListener() {
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
            }, RequestCode.acceptappointment, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void denyAppoinrment(final Activity activity, String appointmentId, int mode, final DataObserver dataObserver) {
        try {
            JSONObject param = new JSONObject();

            param.put(ApiList.KEY_APPOINTMENTID, appointmentId);
            param.put(ApiList.KEY_LOGINID, LoginHelper.getInstance().getUserID());
            param.put(ApiList.KEY_MODE, mode);


            RestClient.getInstance().post(activity, Request.Method.POST, ApiList.APIs.denyappointment.getUrl(), param, new RequestListener() {
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
            }, RequestCode.denyappointment, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void resendAppointment(final Activity activity, String appointmentId, final DataObserver dataObserver) {
        try {
            TimeZone timezone = TimeZone.getDefault();
            String timezoneName = timezone.getID();
            JSONObject param = new JSONObject();
            param.put(ApiList.KEY_APPOINTMENTID, appointmentId);
            param.put(ApiList.KEY_TIMEZONE, timezoneName);
            RestClient.getInstance().post(activity, Request.Method.POST, ApiList.APIs.resendappointment.getUrl(), param, new RequestListener() {
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
            }, RequestCode.resendappointment, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
