/**
 * Copyright 2017 Google Inc. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pinpoint.appointment.location;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import android.util.Log;

import com.android.volley.Request;
import com.pinpoint.appointment.R;
import com.pinpoint.appointment.activities.HomeActivity;
import com.pinpoint.appointment.api.ApiList;
import com.pinpoint.appointment.api.RequestCode;
import com.pinpoint.appointment.api.RequestListener;
import com.pinpoint.appointment.api.RestClient;
import com.pinpoint.appointment.interfaces.DataObserver;
import com.pinpoint.appointment.models.LoginHelper;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Timer;

/**
 * A bound and started service that is promoted to a foreground service when location updates have
 * been requested and all clients unbind.
 *
 * For apps running in the background on "O" devices, location is computed only once every 10
 * minutes and delivered batched every 30 minutes. This restriction applies even to apps
 * targeting "N" or lower which are run on "O" devices.
 *
 * This sample show how to use a long-running service for location updates. When an activity is
 * bound to this service, frequent location updates are permitted. When the activity is removed
 * from the foreground, the service promotes itself to a foreground service, and location updates
 * continue. When the activity comes back to the foreground, the foreground service stops, and the
 * notification assocaited with that service is removed.
 */
public class LocationUpdatesService extends Service {


    private IBinder binder = new LocalBinder();
    private boolean isTracking;
    private ArrayList<Location> trackedWaypoints;
    private String bestProvider;
    private Timer timer;
    String NOTIFICATION_CHANNEL_ID = "4";
    Context mContext;
    boolean stopService = false;
    String userId = "0";
//    private Timer myTimer;
    private int mInterval = 1000 * 60 * 2; // 2 minutes
    private Handler mHandler;

    @SuppressLint("MissingPermission")
    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;

//        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
//        Criteria criteria = new Criteria();
//        bestProvider = locationManager.getBestProvider(criteria, true);
//
//        isTracking = false;
//
//        locationListener = new LocationListener() {
//            @Override
//            public void onLocationChanged(Location location) {
//                Intent intent = new Intent("location_update");
//                intent.putExtra("latitude", location.getLatitude());
//                intent.putExtra("longitude", location.getLongitude());
//                sendBroadcast(intent);
//                if (isTracking) {
//                    if (trackedWaypoints.size() > 1) {
//                        distance.add(trackedWaypoints.get(trackedWaypoints.size() - 1).distanceTo(location));
//                    }
//                    trackedWaypoints.add(location);
//                }
//            }
//
//            @Override
//            public void onStatusChanged(String s, int i, Bundle bundle) { }
//
//            @Override
//            public void onProviderEnabled(String s) { }
//
//            @Override
//            public void onProviderDisabled(String s) {
//                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(intent);
//            }
//        };
//
//        locationManager.requestLocationUpdates(bestProvider, 0, 0, locationListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    public void startTracking() {

        startInForeground();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try
        {
        CountDownTimer waitTimer;
        if(intent!=null)
        {
        userId = intent.getStringExtra(ApiList.KEY_USERID);//"userId"
            if(intent.hasExtra(ApiList.KEY_STOP))
            stopService = intent.getBooleanExtra(ApiList.KEY_STOP, false);
        }
        if (stopService) {
            stopForegroundService();
        } else {

            if (userId == null)
                userId = LoginHelper.getInstance().getUserID();
            startInForeground();
            if (mHandler == null) {
                mHandler = new Handler();
                startRepeatingTask();
            }
//            Log.d("LocationUpdatesService", "Userid:" + userId);
//            myTimer = new Timer();
//            TimerTask t = new TimerTask() {
//                @Override
//                public void run() {
//                    if (userId == null)
//                        userId = LoginHelper.getInstance().getUserID();
//
//                    Log.d("LocationUpdatesService", "Userid:" + userId);
//                    try {
//                        updateStatusApi(mContext, dataObserver, Integer.parseInt(userId));
//                    } catch (Exception ec) {
//                    }
////                    System.out.println("1");
//                }
//            };
//            myTimer.scheduleAtFixedRate(t, 1000 * 60, 1000 * 60 * 5);


        }
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        //return super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    void startRepeatingTask() {
        mStatusChecker.run();
    }

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {
                if (userId == null)
                    userId = LoginHelper.getInstance().getUserID();

                Log.d("LocationUpdatesService", "Userid:" + userId);
                try {
                    updateStatusApi(mContext, dataObserver, Integer.parseInt(userId));
                } catch (Exception ec) {
                }
            }
            catch (Exception ex)
            {}
            finally {
                // 100% guarantee that this always happens, even if
                // your update method throws an exception
                mHandler.postDelayed(mStatusChecker, mInterval);
            }
        }
    };
    void stopRepeatingTask() {
        mHandler.removeCallbacks(mStatusChecker);
    }
    private void stopForegroundService() {
//        Log.d(TAG_FOREGROUND_SERVICE, "Stop foreground service.");

        // Stop foreground service and remove the notification.
        stopForeground(true);
        try {
            userId = "0";
//            if (myTimer != null)
//                myTimer.cancel();
            stopRepeatingTask();
        } catch (Exception ex) {

        }

        // Stop the foreground service.
        stopSelf();
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
                    try {
//                            Crashlytics.log( "ScheduleBroadcastReceiver"+"From : On Status Update "+userId);
//                            LeadDetails.setMessageLog(mContext, "LocationUpdatesService" + "From : CountDownTimer" + new Date());
//                            locationContent = "\nFrom:ScheduleBroadcastReceiver  OnStatus Updated" + new Date();
//                            writeToFile();
                    } catch (Exception ex) {
                    }
//                    }

                }

                @Override
                public void onException(String statusCode, String error, RequestCode requestCode) {
                    dataObserver.OnFailure(requestCode, statusCode, error);
                    try {
//                        LeadDetails.setMessageLog(mContext, "ScheduleBroadcastReceiver" + error + userId + new Date());
                    } catch (Exception ex) {
                    }
                }

                @Override
                public void onOtherStatus(RequestCode requestCode, Object object) {
                    dataObserver.onOtherStatus(requestCode, object);
                    try {
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

    private void startInForeground() {
        Intent notificationIntent = new Intent(this, HomeActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK );
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.mipmap.notification_icon)
//               .setColor(mContext.getResources().getColor(R.color.white))
                .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                .setContentIntent(pendingIntent);
        Notification notification = builder.build();
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "PinPoint", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }

        //commented by sarfaraj
        startForeground(1002, notification);
    }

    public void stopTracking() {
        isTracking = false;
        stopForeground(true);
    }

    public boolean isTracking() {
        return isTracking;
    }

    public ArrayList<Location> getTrackedWaypoints() {
        return trackedWaypoints;
    }

    public Timer getTime() {
//        timer.update();
        return timer;
    }

//    public Distance getDistance() {
//        return distance;
//    }

    public int getSteps() {
        return 0;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class LocalBinder extends Binder {
        public LocationUpdatesService getLocationTrackerInstance() {
            return LocationUpdatesService.this;
        }
    }

}
