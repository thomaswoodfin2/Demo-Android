package com.pinpoint.appointment.receivers;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.PowerManager;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.android.volley.Request;
import com.pinpoint.appointment.BaseConstants;
import com.pinpoint.appointment.R;
import com.pinpoint.appointment.activities.HomeActivity;
import com.pinpoint.appointment.activities.SplashActivity;
import com.pinpoint.appointment.api.ApiList;
import com.pinpoint.appointment.api.RequestCode;
import com.pinpoint.appointment.api.RequestListener;
import com.pinpoint.appointment.api.RestClient;
import com.pinpoint.appointment.helper.PrefHelper;
import com.pinpoint.appointment.models.LeadDetails;
import com.pinpoint.appointment.models.LoginHelper;
import com.pinpoint.appointment.utils.Util;

import org.json.JSONObject;

import java.util.Calendar;

import static android.content.Context.NOTIFICATION_SERVICE;
import static com.pinpoint.appointment.activities.HomeActivity.pendingIntentUpdateService;

public class DozeChangeReceiver extends BroadcastReceiver
{
    Context mContext;
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onReceive(Context context, Intent intent)
    {
        try
        {
            mContext=context;
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            if (pm.isDeviceIdleMode())
            {
                scheduleUpdateService();
//                generateNotification("App enters in power saver mode please open app.");
                LeadDetails.setMessageLog(mContext,"DozeChangeReceiver App enters in  Doze Mode.");
                // the device is now in doze mode
            }
            else
            {
                LeadDetails.setMessageLog(mContext,"DozeChangeReceiver App Exits from Doze Mode.");
                scheduleUpdateService();
                // the device just woke up from doze mode
            }
//            scheduleUpdateService();
        } catch (NullPointerException e) {
            e.printStackTrace();
            LeadDetails.setMessageLog(mContext,"DozeChange Receiver "+e.toString());
        }
    }

    //scheduleUpdateService
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


    //updateStatusApi
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

    private void generateNotification(String message) {
        PendingIntent pendingIntent;
        NotificationCompat.Builder mBuilder;

        Intent notificationIntent = null;
//          if (Util.isAppRunning(getApplicationContext(),getPackageName()))

            notificationIntent = new Intent(mContext, SplashActivity.class);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            notificationIntent.putExtra(BaseConstants.NOTIFICATION_CLICK, true);
            notificationIntent.setAction("" + 105);
//              notificationIntent.putExtra(BaseConstants.KEY_FROM, "notification");

        PendingIntent resultPendingIntent = PendingIntent.getActivity(mContext, 105, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//            PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            String messageToDisplay = message;
            mBuilder = new NotificationCompat.Builder(mContext,"2")
                    .setSmallIcon(R.mipmap.app_icon)
                    .setContentTitle(mContext.getResources().getString(R.string.app_name))
                    .setWhen(Calendar.getInstance().getTimeInMillis())
                    .setContentText(messageToDisplay)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(messageToDisplay))
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setContentIntent(resultPendingIntent)
                    .setAutoCancel(true);
        }
        else
        {
            String messageToDisplay = message;
            mBuilder = new NotificationCompat.Builder(mContext,"2")
                    .setSmallIcon(R.mipmap.app_icon)
                    .setContentTitle(mContext.getResources().getString(R.string.app_name))
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(messageToDisplay))
                    .setWhen(Calendar.getInstance().getTimeInMillis())
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setContentText(messageToDisplay)
                    .setAutoCancel(true)
                    .setContentIntent(resultPendingIntent);
        }

        PrefHelper.setBoolean("isnotification",false);

        Notification notification = mBuilder.build();
//            NotificationCompat.Builder notification = new NotificationCompat.Builder(this, CHANNEL_ID);
        notification.defaults |= Notification.DEFAULT_SOUND;
        notification.defaults |= Notification.DEFAULT_VIBRATE;
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        int mNotificationId = (int) System.currentTimeMillis();
        NotificationManager mNotifyMgr = (NotificationManager) mContext.getSystemService(NOTIFICATION_SERVICE);
        assert mNotifyMgr != null;
        mNotifyMgr.notify(105, notification);

    }

}