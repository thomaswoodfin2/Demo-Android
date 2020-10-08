package com.pinpoint.appointment.models;


import android.app.Activity;

import com.android.volley.Request;
import com.pinpoint.appointment.BaseConstants;
import com.pinpoint.appointment.api.ApiList;
import com.pinpoint.appointment.api.MultipartRequest;
import com.pinpoint.appointment.api.RequestCode;
import com.pinpoint.appointment.api.RequestListener;
import com.pinpoint.appointment.api.RestClient;
import com.pinpoint.appointment.interfaces.DataObserver;
import com.pinpoint.appointment.utils.Debug;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PlanData implements Serializable{

    private String paymentAmount;
    private String sku;
    private String planTitle;
    private String expDate;
    private String cancelDate;
    private String paymentDate;

    public String getExpDate() {
        return expDate;
    }

    public void setExpDate(String expDate) {
        this.expDate = expDate;
    }

    public String getCancelDate() {
        return cancelDate;
    }

    public void setCancelDate(String cancelDate) {
        this.cancelDate = cancelDate;
    }

    public String getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(String paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getSubscriptionStatus() {
        return subscriptionStatus;
    }

    public void setSubscriptionStatus(String subscriptionStatus) {
        this.subscriptionStatus = subscriptionStatus;
    }

    private String subscriptionStatus;
    private int paymentStatus;

    public String getPaymentAmount() {
        return paymentAmount;
    }

    public void setPaymentAmount(String paymentAmount) {
        this.paymentAmount = paymentAmount;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getPlanTitle() {
        return planTitle;
    }

    public void setPlanTitle(String planTitle) {
        this.planTitle = planTitle;
    }

    public int getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(int paymentStatus) {
        this.paymentStatus = paymentStatus;
    }




    public static void getSubscriptionPlans(final Activity activity, int page, int limit, final DataObserver dataObserver) {
        try {
            JSONObject param = new JSONObject();
            param.put(ApiList.KEY_USERID, Integer.parseInt(LoginHelper.getInstance().getUserID()));
            param.put(ApiList.KEY_PAGENO, page);
            param.put(ApiList.KEY_LIMIT, limit);


            RestClient.getInstance().post(activity, Request.Method.POST, ApiList.APIs.getSubscriptionPlans.getUrl(), param, new RequestListener() {
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
            }, RequestCode.getSubscriptionPlans, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void updatePaymentStatus(final Activity activity,PlanData planData, final DataObserver dataObserver) {
        try
        {
            JSONObject param = new JSONObject();
            param.put(ApiList.KEY_USERID, Integer.parseInt(LoginHelper.getInstance().getUserID()));
            SimpleDateFormat format1=new SimpleDateFormat(BaseConstants.PAYMENTTIME);
            Date currentdATE=format1.parse(String.valueOf(format1.format(new Date())));
            String datestring=format1.format(currentdATE);
            param.put(ApiList.KEY_DATETIME, datestring);
            param.put(ApiList.KEY_AMOUNT, planData.getPaymentAmount());

            RestClient.getInstance().post(activity, Request.Method.POST, ApiList.APIs.updatePaymentStatus.getUrl(), param, new RequestListener() {
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
            }, RequestCode.updatePaymentStatus, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void cancelSubscription(final Activity activity, final DataObserver dataObserver) {
        try
        {
            JSONObject param = new JSONObject();
            param.put(ApiList.KEY_USERID, Integer.parseInt(LoginHelper.getInstance().getUserID()));


            RestClient.getInstance().post(activity, Request.Method.POST, ApiList.APIs.cancelsubscription.getUrl(), param, new RequestListener() {
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
            }, RequestCode.cancelsubscription, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
