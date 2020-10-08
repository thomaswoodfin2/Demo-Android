package com.pinpoint.appointment.location;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;

import com.android.volley.Request;
import com.pinpoint.appointment.BaseConstants;
import com.pinpoint.appointment.activities.HomeActivity;
import com.pinpoint.appointment.api.ApiList;
import com.pinpoint.appointment.api.RequestCode;
import com.pinpoint.appointment.api.RequestListener;
import com.pinpoint.appointment.api.RestClient;
import com.pinpoint.appointment.helper.ToastHelper;
import com.pinpoint.appointment.interfaces.DataObserver;
import com.pinpoint.appointment.models.LeadDetails;
import com.pinpoint.appointment.models.LoginHelper;
import com.pinpoint.appointment.receivers.ScheduleBroadcastReceiver;
import com.pinpoint.appointment.utils.Debug;
import com.pinpoint.appointment.utils.Util;

import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;

import static com.pinpoint.appointment.activities.HomeActivity.pendingIntentUpdateService;

public class NetworkChangeReceiver extends BroadcastReceiver
{
    Context mContext;
    @Override
    public void onReceive(Context context, Intent intent)
    {
        try
        {
            mContext=context;
            if (isOnline(context))
            {
                if(LoginHelper.getInstance().isLoggedIn())
                {
                    Debug.trace("Mobile Connected to internet");
//                    ToastHelper.displayCustomToast("Status Updated for Online Change");

//                    LeadDetails.setMessage(context, " From : NetworkChangereceiver before status update Method"  + new Date());
                    updateStatusApi(context);

                }

//                dialog(true);

            }
            else {
//                dialog(false);

            }
            scheduleUpdateService();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }
    public  void scheduleUpdateService() {
        try {
            if (pendingIntentUpdateService != null) {
                return;
            }
            Intent toastIntent = new Intent(mContext, ScheduleBroadcastReceiver.class);
            toastIntent.putExtra("userId", Integer.parseInt(LoginHelper.getInstance().getUserID()));
            toastIntent.putExtra("isTablet", false);
            if (Util.isTabletDevice(mContext)) {
                toastIntent.putExtra("isTablet", true);
            }
            pendingIntentUpdateService = PendingIntent.getBroadcast(mContext, 0,
                    toastIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            HomeActivity.updateServiceManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());

            if (Build.VERSION.SDK_INT >= 23) {
                HomeActivity.updateServiceManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis() + BaseConstants.ALARM_TIMER, pendingIntentUpdateService);
            } else if (Build.VERSION.SDK_INT >= 19) {
                HomeActivity.updateServiceManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis() + BaseConstants.ALARM_TIMER, pendingIntentUpdateService);
            } else {
                HomeActivity.updateServiceManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis() + BaseConstants.ALARM_TIMER, pendingIntentUpdateService);
            }
        }catch (Exception ex)
        {

        }
//        if (updateServiceManager != null)
//            updateServiceManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
//                    2*60 * 1000, pendingIntentUpdateService);
//        LeadDetails.setMessage(HomeActivity.this, "Service start from HomeActivity" + new Date());
    }



    public void updateStatusApi(final Context activity) {
        try {
            JSONObject param = new JSONObject();
            param.put(ApiList.KEY_USERID, Integer.parseInt(LoginHelper.getInstance().getUserID()));
            param.put(ApiList.KEY_STATUS, "1");


            RestClient.getInstance().postForService(activity, Request.Method.POST, ApiList.APIs.setuserstatus.getUrl(), param, new RequestListener() {
                @Override
                public void onComplete(RequestCode requestCode, Object object) {
//                    Debug.trace("List Size:" + object.toString());
//                    ToastHelper.displayCustomToast("Status Updated for Online Change");
//                    LeadDetails.setMessage(activity, " From : NetworkChangereceiver After status update Method"  + new Date());
                }

                @Override
                public void onException(String statusCode, String error, RequestCode requestCode) {

                }

                @Override
                public void onOtherStatus(RequestCode requestCode, Object object) {

                }


            }, RequestCode.setuserstatus , false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isOnline(Context context) {
        try
        {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            //should check null because in airplane mode it will be null
            return (netInfo != null && netInfo.isConnected());
        } catch (NullPointerException e) {
            e.printStackTrace();
            return false;
        }
    }
}