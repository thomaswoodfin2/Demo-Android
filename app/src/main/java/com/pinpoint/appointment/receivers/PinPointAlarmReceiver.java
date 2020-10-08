package com.pinpoint.appointment.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.android.volley.Request;
import com.pinpoint.appointment.api.ApiList;
import com.pinpoint.appointment.api.RequestCode;
import com.pinpoint.appointment.api.RequestListener;
import com.pinpoint.appointment.api.RestClient;
import com.pinpoint.appointment.interfaces.DataObserver;
import com.pinpoint.appointment.models.LoginHelper;
import com.pinpoint.appointment.service.SensorService;
import com.pinpoint.appointment.utils.Constants;
import com.pinpoint.appointment.utils.Util;

import org.json.JSONObject;

public class PinPointAlarmReceiver extends BroadcastReceiver {

    private SharedPreferences sp;

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub

        //sp = context.getSharedPreferences(Utility.SHARED_PREFS, 0);
        boolean isLoggedIn = LoginHelper.getInstance().isLoggedIn();

        if (intent.getAction().equalsIgnoreCase(Constants.SENSOR_ACTION)) {

            if(isLoggedIn){
                context.startService(new Intent(context, SensorService.class));

                if (Util.checkConnectivity(context)) {
                    int userId = Integer.parseInt(LoginHelper.getInstance().getUserID());
                    updateStatusApi(context, dataObserver, userId);
                }
            } else{
                Util.stopAlaramSensor(context);
            }

//            if (sp.getBoolean("logged_in", false)) {
//                if (Util.checkConnectivity(context))
//                    context.startService(new Intent(context, SensorService.class));
//            } else {
//
//                Util.stopAlaramSensor(context);
//            }
        }

//        if (intent.getAction().equalsIgnoreCase(Utility.GROUP_UPDATE_ACTION)) {
//            if (sp.getBoolean("logged_in", false)) {
//                if (Utility.checkConnectivity(context))
//                    context.startService(new Intent(context, RefreshGroup.class));
//            } else {
//                Utility.stopGroupAlaram(context);
//            }
//        }
    }



    //updateStatusApi
    public void updateStatusApi(final Context context, final DataObserver dataObserver, final int userId) {
        try {
            JSONObject param = new JSONObject();
            param.put(ApiList.KEY_USERID, userId);
            param.put(ApiList.KEY_STATUS, "1");

            RestClient.getInstance().postForService(context, Request.Method.POST, ApiList.APIs.setuserstatus.getUrl(), param, new RequestListener() {
                @Override
                public void onComplete(RequestCode requestCode, Object object) {
                    dataObserver.OnSuccess(requestCode, object);
//                    if (file != null) {
                    try
                    {
//                            Crashlytics.log( "ScheduleBroadcastReceiver"+"From : On Status Update "+userId);
//                            LeadDetails.setMessageLog(mContext, "ScheduleBroadcastReceiver" + "From : On Status Update " + new Date());
//                            locationContent = "\nFrom:ScheduleBroadcastReceiver  OnStatus Updated" + new Date();
//                            writeToFile();
                    }
                    catch(Exception ex)
                    {
                    }
//                    }
                    if (Integer.parseInt(LoginHelper.getInstance().getUserID()) == 0 || userId == 0) {
//                        LeadDetails.setMessageLog(mContext, "Got 0 as userid\n Reason : " + "\nTime :" + new Date());
                    }
                }

                @Override
                public void onException(String statusCode, String error, RequestCode requestCode) {
                    dataObserver.OnFailure(requestCode, statusCode, error);
                    try
                    {
//                        LeadDetails.setMessageLog(mContext, "ScheduleBroadcastReceiver" + error + userId + new Date());
                    } catch (Exception ex) {
                    }
                }

                @Override
                public void onOtherStatus(RequestCode requestCode, Object object) {
                    dataObserver.onOtherStatus(requestCode, object);
                    try
                    {
//                        locationContent = "From:ScheduleBroadcastReceiver  OnError In Status Update" + new Date();
//                        writeToFile();
//                        Crashlytics.log( "ScheduleBroadcastReceiver"+"From : onOtherStatus "+userId);
//                        LeadDetails.setMessageLog(mContext, "ScheduleBroadcastReceiver" + "From : onOtherStatus " + userId + new Date());
                    } catch (Exception ex) {
                    }
                }


            }, RequestCode.setuserstatus, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    DataObserver dataObserver = new DataObserver() {
        @Override
        public void OnSuccess(RequestCode requestCode, Object object) {
            Log.d("TAG:", "Status Updated");
        }

        @Override
        public void OnFailure(RequestCode requestCode, String errorCode, String error) {
//            Log.e(IntervalBroadcastReceiver.class.getSimpleName(), "OnFailure: " + errorCode + " error : " + error);
//            LeadDetails.setMessage(mContext, "API failed from ScheduleBroadcastReceiver\n Reason : " + error + "\nTime :" + new Date());
        }

        @Override
        public void onOtherStatus(RequestCode requestCode, Object object) {
        }

        @Override
        public void onRetryRequest(RequestCode requestCode) {

        }
    };


}
