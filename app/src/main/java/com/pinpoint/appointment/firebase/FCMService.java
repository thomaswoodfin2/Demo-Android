package com.pinpoint.appointment.firebase;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.pinpoint.appointment.BaseConstants;
import com.pinpoint.appointment.R;
import com.pinpoint.appointment.activities.SplashActivity;
import com.pinpoint.appointment.api.ApiList;
import com.pinpoint.appointment.helper.DBHelper;
import com.pinpoint.appointment.helper.PrefHelper;
import com.pinpoint.appointment.utils.MyLifecycleHandler;
import com.pinpoint.appointment.utils.Util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import me.leolin.shortcutbadger.ShortcutBadger;

import static com.android.volley.VolleyLog.TAG;


/**
 * Created by admin on 27-Nov-17.
 */

public class FCMService extends FirebaseMessagingService {

    String title = "PinPoint";

    @Override
    public void onCreate() {
        super.onCreate();

//      ShortcutBadger.applyCount(FCMService.this, PrefHelper.getInt(PrefHelper.KEY_NOTIFICATION_COUNT, 0));
//        ShortcutBadger.applyCount(FCMService.this, BaseConstant.NOTIFICATION_COUNT);
    }


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // ...

//        Note newBotw=new Note();
//        newBotw.setNote("yes");
//        newBotw.setTimestamp("yes");

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
//            MyNotification notification = gson.fromJson(new JSONObject(remoteMessage.getData()).toString(), MyNotification.class);
//            BaseConstant.NOTIFICATION_COUNT=notification.getBadgcount();
            try {
                title = remoteMessage.getData().get("title");
            } catch (Exception ex) {
                title = getApplicationContext().getResources().getString(R.string.app_name);
            }

            String msg_body = remoteMessage.getData().get("body");
            DBHelper dbHelper = new DBHelper(FCMService.this);
            dbHelper.insertLogItem("device wake up notification Network " + msg_body + Util.checkConnectivity(FCMService.this));

            System.out.println("FCM : " + msg_body);

            //by sarfaraj
            if (msg_body != null && msg_body.equalsIgnoreCase("7890")) {
                handlePushToDeviceWakeUp();
                return;
            }

            generateNotification(remoteMessage.getData().get("body"));
//            sendNotification(remoteMessage.getNotification().getBody().toString());
        }
        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            generateNotification(remoteMessage.getNotification().getBody().toString());
//            sendNotification(remoteMessage.getNotification().getBody().toString());
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }


    private void generateNotification(String message) {
        PendingIntent pendingIntent;

//              notificationIntent.putExtra(Ba
        Date now = new Date();
        int notid = Integer.parseInt(new SimpleDateFormat("ddHHmmss", Locale.US).format(now));


        NotificationCompat.Builder mBuilder;
        BaseConstants.NOTIFICATION_COUNT++;
        PrefHelper.setInt(BaseConstants.COUNT, BaseConstants.NOTIFICATION_COUNT);
        ShortcutBadger.applyCount(FCMService.this, BaseConstants.NOTIFICATION_COUNT);
        Intent notificationIntent = null;
//          if (Util.isAppRunning(getApplicationContext(),getPackageName()))
        if (MyLifecycleHandler.isApplicationVisible()) {
            notificationIntent = new Intent(this, SplashActivity.class);
            notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            notificationIntent.putExtra(BaseConstants.NOTIFICATION_CLICK, true);
            notificationIntent.setAction("" + notid);
            Intent intent = new Intent("com.pinpoint.appointment.broadcast1");
//                intent.putExtra(BaseConstants.EXTRA_LOCATION, BaseConstants.NOTIFICATION_COUNT);
            intent.putExtra(ApiList.KEY_MESSAGE, message);
            intent.putExtra(ApiList.KEY_TITLE, title);
//                if(PrefHelper.getBoolean(BaseConstants.NOTIFICATION_RECEIVED,false))
//                {
            PrefHelper.setBoolean(BaseConstants.NOTIFICATION_RECEIVED, false);

            // LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            sendBroadcast(intent);
//                }
        } else {
            notificationIntent = new Intent(this, SplashActivity.class);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            notificationIntent.putExtra(BaseConstants.NOTIFICATION_CLICK, true);
            notificationIntent.setAction("" + notid);
//              notificationIntent.putExtra(BaseConstants.KEY_FROM, "notification");

        }

        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, notid, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//            PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            String channelId = getString(R.string.default_notification_channel_id);
            String messageToDisplay = message.replace("<br/>", "");
            mBuilder = new NotificationCompat.Builder(this, "1")
                    .setSmallIcon(R.mipmap.app_icon)
                    .setContentTitle(title)
                    .setWhen(Calendar.getInstance().getTimeInMillis())
                    .setContentText(messageToDisplay)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(messageToDisplay))
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setContentIntent(resultPendingIntent)
                    .setAutoCancel(true);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel channel = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                channel = new NotificationChannel(channelId,
                        "PinPoint Channel",
                        NotificationManager.IMPORTANCE_DEFAULT);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                notificationManager.createNotificationChannel(channel);
            }
        } else {
            String messageToDisplay = message.replace("<br/>", "");
            mBuilder = new NotificationCompat.Builder(this, "1")
                    .setSmallIcon(R.mipmap.app_icon)
                    .setContentTitle(title)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(messageToDisplay))
                    .setWhen(Calendar.getInstance().getTimeInMillis())
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setContentText(messageToDisplay)
                    .setAutoCancel(true)
                    .setContentIntent(resultPendingIntent);
        }
        PrefHelper.setBoolean("isnotification", true);
//            SharedPreferences sp = getSharedPreferences("notification", Context.MODE_PRIVATE);
//            SharedPreferences.Editor editor=sp.edit();
//            editor.clear();
//            editor.putBoolean("isnotification",true);
//            editor.commit();

        Notification notification = mBuilder.build();
//            NotificationCompat.Builder notification = new NotificationCompat.Builder(this, CHANNEL_ID);
        notification.defaults |= Notification.DEFAULT_SOUND;
        notification.defaults |= Notification.DEFAULT_VIBRATE;
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        int mNotificationId = (int) System.currentTimeMillis();
        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        assert mNotifyMgr != null;
        mNotifyMgr.notify(notid, notification);


    }


    //handlePushToDeviceWakeUp
    private void handlePushToDeviceWakeUp() {
        //generateNotification("device wake up notification");

//        DBHelper dbHelper = new DBHelper(FCMService.this);
//        dbHelper.insertLogItem("device wake up notification Network "+ Util.checkConnectivity(FCMService.this));

    }


}
