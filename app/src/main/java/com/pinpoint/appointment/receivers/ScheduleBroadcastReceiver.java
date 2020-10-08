package com.pinpoint.appointment.receivers;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import androidx.core.app.NotificationCompat;

import android.util.Log;

import com.android.volley.Request;
import com.pinpoint.appointment.BaseApplication;
import com.pinpoint.appointment.BaseConstants;
import com.pinpoint.appointment.R;
import com.pinpoint.appointment.activities.SplashActivity;
import com.pinpoint.appointment.api.ApiList;
import com.pinpoint.appointment.api.RequestCode;
import com.pinpoint.appointment.api.RequestListener;
import com.pinpoint.appointment.api.RestClient;
import com.pinpoint.appointment.helper.DBHelper;
import com.pinpoint.appointment.helper.PrefHelper;
import com.pinpoint.appointment.interfaces.DataObserver;
//import com.pinpoint.appointment.location.LocationUpdateServiceBackground;
import com.pinpoint.appointment.models.LeadDetails;
import com.pinpoint.appointment.models.LoginHelper;
import com.pinpoint.appointment.service.BackgroundIntentService;
import com.pinpoint.appointment.utils.Constants;
import com.pinpoint.appointment.utils.Debug;
import com.pinpoint.appointment.utils.Util;

import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;

import static android.content.Context.ACTIVITY_SERVICE;
import static android.content.Context.NOTIFICATION_SERVICE;

public class ScheduleBroadcastReceiver extends BroadcastReceiver {
    Context mContext;
    boolean isTablet=false;
    private AlarmManager updateServiceManager;
    public PendingIntent pendingIntentUpdateService;
    int userId = 0;

    @Override
    public void onReceive(Context context, Intent intent) {
//        int userId=Integer.parseInt(LoginHelper.getInstance().getUserID());
        userId = 0;
        mContext = context;

//        LeadDetails.setMessage(mContext, "ScheduleBroadcastReceiver" + "From : On ReceiveMethod " + new Date());
        if (intent != null && intent.getExtras() != null && intent.hasExtra("userId")) {
            userId = intent.getIntExtra("userId", 0);
            isTablet=intent.getBooleanExtra("isTablet", false);
        }
        Debug.trace("From ScheduleBroadcastReceiver  onReceive");

        scheduleUpdateService();
        try {
            updateStatusApi(context, dataObserver, userId);
        }
        catch (Exception ex) {
//            LeadDetails.setMessageLog(mContext, ex.toString());
        }
        try
        {
            if(!isTablet)
            {
                BackgroundIntentService.Companion.startLocationTracking(context.getApplicationContext(), null, null);
                new Handler().postDelayed(() -> { ((BaseApplication) context.getApplicationContext()).bus().send(Constants.BUS_ACTION_START_LOCATION_TRACKING); }, 1000);
                /*
                if (!checkServiceRunning()) {

                    startServiceIntent();
                    Debug.trace("Location Service Restarted");

                } else {
                    Debug.trace("Location Service Running");
                }
                */
            }
        }catch (Exception ex)
        {
            LeadDetails.setMessage(mContext, ex.toString());
        }
        try
        {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (pm.isDeviceIdleMode())
                {
//                    LeadDetails.setMessageLog(mContext,"ScheduleBroadcastreceiver: App enters in  Doze Mode.");
//                    generateNotification("App enters in power saver mode, Please open app.");
                    scheduleUpdateService();
                }
            }
        }
        catch (Exception e)
        {}

        //logs
        DBHelper dbHelper = new DBHelper(mContext);
        dbHelper.insertLogItem("ScheduleBroadcastReceiver [onReceive] " + userId +" Network "+ Util.checkConnectivity(mContext));
    }


    //generateNotification
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
//      PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            String messageToDisplay=message;
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
            String messageToDisplay=message;
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


    /* 02-22-2020 - Removed by TBL. Replaced with status update inside BackgroundIntentService
    private void startServiceIntent() {
//        LeadDetails.setMessage(mContext, "Location Service restarted from Alarmmanager");
        try {
            Intent ina = new Intent(mContext, LocationUpdateServiceBackground.class);
            ina.putExtra("updateTimeInterval", 2000L);//in milliseconds
            ina.putExtra("updateDistance", 1f);//in meters
            ina.putExtra(ApiList.KEY_USERID, userId);//in meters

            ina.putExtra("isDistanceRequired", false);//To enable minimum distance for location check
            mContext.startService(ina);
        }catch (Exception ex)
        {
            LeadDetails.setMessageLog(mContext, " From : ScheduleBroadcastReceiver startserviceintent Exception:" + ex.toString() + new Date());
        }
    }
    */

    public boolean checkServiceRunning() {
        ActivityManager manager = (ActivityManager) mContext.getSystemService(ACTIVITY_SERVICE);
//      String serviceName = mContext.getPackageName() + ".location.LocationUpdateServiceBackground";
        String serviceName = "com.pinpoint.appointment.location.LocationUpdateServiceBackground";

        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {

            if (serviceName
                    .equals(service.service.getClassName())) {
                return true;
            }
        }

        return false;
    }

    private void scheduleUpdateService() {

        if(userId!=0&&LoginHelper.getInstance().isLoggedIn())
        {
            if (pendingIntentUpdateService != null) {
                return;
            }

            Intent toastIntent = new Intent(mContext, ScheduleBroadcastReceiver.class);
            toastIntent.putExtra("userId", userId);
            toastIntent.putExtra("isTablet", isTablet);

            pendingIntentUpdateService = PendingIntent.getBroadcast(mContext, 0,
                    toastIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            updateServiceManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());

            if (Build.VERSION.SDK_INT >= 23) {
                updateServiceManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis() + BaseConstants.ALARM_TIMER, pendingIntentUpdateService);
            } else if (Build.VERSION.SDK_INT >= 19) {
                updateServiceManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis() + BaseConstants.ALARM_TIMER, pendingIntentUpdateService);
            } else {
                updateServiceManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis() + BaseConstants.ALARM_TIMER, pendingIntentUpdateService);
            }
        }
//        LeadDetails.setMessage(mContext, "Alarm start from ScheduleBroadcastReceiver" + new Date());
    }

    public void updateStatusApi(final Context activity, final DataObserver dataObserver, final int userId) {
        try {
            JSONObject param = new JSONObject();
            param.put(ApiList.KEY_USERID, userId);
            param.put(ApiList.KEY_STATUS, "1");

            RestClient.getInstance().postForService(activity, Request.Method.POST, ApiList.APIs.setuserstatus.getUrl(), param, new RequestListener() {
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

//    void writeToFile() {
//        try {
//            if (file != null) {
//                FileOutputStream fOut = new FileOutputStream(file.getAbsolutePath(), true);
//                fOut.write(locationContent.getBytes());
//                fOut.close();
//            }
////          Files.write(Paths.get("myfile.txt"), "the text".getBytes(), StandardOpenOption.APPEND);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        locationContent = "";
//    }

}
