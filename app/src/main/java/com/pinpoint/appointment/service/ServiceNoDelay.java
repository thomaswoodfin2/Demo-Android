package com.pinpoint.appointment.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import androidx.annotation.Nullable;
import android.util.Log;

import com.pinpoint.appointment.BaseConstants;
import com.pinpoint.appointment.api.ApiList;
import com.pinpoint.appointment.models.LoginHelper;
import com.pinpoint.appointment.receivers.ScheduleBroadcastReceiver;
import com.pinpoint.appointment.utils.Debug;
import com.pinpoint.appointment.utils.Util;

import java.util.Calendar;

public class ServiceNoDelay extends Service {
    public int counter = 0;
    Context context;
    private AlarmManager updateServiceManager;
    private PendingIntent pendingIntentUpdateService;
    int userId;

    public ServiceNoDelay(Context applicationContext) {
        super();
        context = applicationContext;
        Log.i("HERE", "here service created!");
    }

    public ServiceNoDelay() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        if(intent!=null&&!intent.getExtras().isEmpty())
        {
            if(intent.hasExtra(ApiList.KEY_USERID)) {
                userId = intent.getIntExtra(ApiList.KEY_USERID, Integer.parseInt(LoginHelper.getInstance().getUserID()));
                Debug.trace("USERID : FROM INTENT"+ userId);
            }
            else {
                userId = Integer.parseInt(LoginHelper.getInstance().getUserID());
                Debug.trace("USERID : FROM PREFERENCE"+userId);
            }
//            LeadDetails.setMessage(context, " From : ServiceNoDelay onStartCommand"  + new Date());
        }
        else
        {
            Debug.trace("USERID : FROM ELSE" +userId);
            userId = Integer.parseInt(LoginHelper.getInstance().getUserID());
            Debug.trace("USERID : FROM ELSE" +userId);
        }

        scheduleUpdateService();
//      startTimer();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.i("EXIT", "ondestroy!");
        stopScheduleService();

        Intent broadcastIntent = new Intent("ac.in.ActivityRecognition.RestartSensor");
        sendBroadcast(broadcastIntent);
//        stoptimertask();
    }

    public void stopScheduleService() {
//        LeadDetails.setMessage(HomeActivity.this, "Service STOP from HomeActivity" + new Date());
        AlarmManager manager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        if (manager != null) {
            manager.cancel(pendingIntentUpdateService);//cancel the alarm manager of the pending intent

        }
//        stopService(new Intent(HomeActivity.this, UpdateStatusService.class));
    }


    @Override
    public void onCreate() {
        context = this;
    }

    private void scheduleUpdateService() {
        if (pendingIntentUpdateService != null) {
            return;
        }

        Intent toastIntent = new Intent(getApplicationContext(), ScheduleBroadcastReceiver.class);
        toastIntent.putExtra("userId", userId);
        toastIntent.putExtra("isTablet", false);
        if (Util.isTabletDevice(context)) {
            toastIntent.putExtra("isTablet", true);
        }

        pendingIntentUpdateService = PendingIntent.getBroadcast(getApplicationContext(), 0,
                toastIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        updateServiceManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        if (Build.VERSION.SDK_INT >= 23) {
            updateServiceManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis() + BaseConstants.ALARM_TIMER_Service, pendingIntentUpdateService);
        } else if (Build.VERSION.SDK_INT >= 19) {
            updateServiceManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis() + BaseConstants.ALARM_TIMER_Service, pendingIntentUpdateService);
        } else {
            updateServiceManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis() + BaseConstants.ALARM_TIMER_Service, pendingIntentUpdateService);
        }
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
