package com.pinpoint.appointment;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Application;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.pinpoint.appointment.activities.HomeActivity;
import com.pinpoint.appointment.api.ApiList;
import com.pinpoint.appointment.enumeration.BottomTabs;
import com.pinpoint.appointment.enumeration.BottomTabsTablet;
import com.pinpoint.appointment.helper.MessageHelper;
import com.pinpoint.appointment.helper.PrefHelper;
import com.pinpoint.appointment.location.LocationUpdatesService;
import com.pinpoint.appointment.models.LoginHelper;
import com.pinpoint.appointment.receivers.ScheduleBroadcastReceiver;
import com.pinpoint.appointment.service.BackgroundIntentService;
import com.pinpoint.appointment.utils.Constants;
import com.pinpoint.appointment.utils.Util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.annotation.Nullable;

public class BaseActivity extends FragmentActivity {
    public static Context context;
    public static BaseActivity baseActivity;
    //    private MyReceiver myReceiver;
    private Dialog alertDialog;
    private AlarmManager updateServiceManager;
    private PendingIntent pendingIntentUpdateService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if(myReceiver==null)
//        {
//            myReceiver = new MyReceiver();
//            LocalBroadcastManager.getInstance(this).registerReceiver(myReceiver,
//                    new IntentFilter(LocationUpdateServiceBackground.ACTION_BROADCAST));
//        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

//        if(myReceiver!=null) {
//            LocalBroadcastManager.getInstance(this).unregisterReceiver(myReceiver);
//            myReceiver = null;
//        }
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    private BackgroundIntentService bgService;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (name.getClassName().endsWith("BackgroundIntentService"))
                bgService = ((BackgroundIntentService.LocationServiceBinder) service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            if (name.getClassName().endsWith("BackgroundIntentService"))
                bgService = null;
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        context = this;
        baseActivity = this;

        if (!Util.isTabletDevice(baseActivity)) {
            //02-22-2020 - Removed by TBL. Replaced with BackgroundIntentService
            //startLocationTrackingBase();
            toggleBackgroundService(true);
            new Handler().postDelayed(() -> toggleLocationTracking(true), 1000);
        }

    }

    protected void toggleBackgroundService(Boolean start) {
        if (start)
            BackgroundIntentService.Companion.startLocationTracking(getApplicationContext(), getApplication(), serviceConnection);
        else {
            if (bgService != null)
                bgService.stopForeground();
            BackgroundIntentService.Companion.stopLocationTracking(getApplicationContext());
        }
    }

    protected void toggleLocationTracking(Boolean start) {
        if (start)
            ((BaseApplication) getApplication()).bus().send(Constants.BUS_ACTION_START_LOCATION_TRACKING);
        else
            ((BaseApplication) getApplication()).bus().send(Constants.BUS_ACTION_STOP_LOCATION_TRACKING);

    }

    /* 02-22-2020 - Removed by TBL. Replaced with BackgroundIntentService
    private void startLocationTrackingBase() {
        try {
            if (!checkServiceRunningBase()) {
                if (!checkPermissionsBase()) {
                } else {
                    startServiceIntentBase();
                }
            }
        } catch (Exception ex) {
        }
    }
    */

    /* 02-22-2020 - Removed by TBL. Replaced with BackgroundIntentService
    private void startServiceIntentBase() {
        try {
            Intent ina = new Intent(baseActivity, LocationUpdateServiceBackground.class);
            ina.putExtra("updateTimeInterval", 2000L);//in milliseconds
            ina.putExtra("updateDistance", 1f);//in meters
            ina.putExtra(ApiList.KEY_USERID, Integer.parseInt(LoginHelper.getInstance().getUserID()));//in meters

            ina.putExtra("isDistanceRequired", false);//To enable minimum distance for location check
            startService(ina);
        } catch (Exception ex) {
        }
//        scheduleUpdateServiceBase();
    }
    */

    private void scheduleUpdateServiceBase() {
        if (pendingIntentUpdateService != null) {
            return;
        }
        Intent toastIntent = new Intent(getApplicationContext(), ScheduleBroadcastReceiver.class);
        toastIntent.putExtra("userId", Integer.parseInt(LoginHelper.getInstance().getUserID()));
        toastIntent.putExtra("isTablet", false);
        if (Util.isTabletDevice(baseActivity)) {
            toastIntent.putExtra("isTablet", true);
        }
        pendingIntentUpdateService = PendingIntent.getBroadcast(getApplicationContext(), 0,
                toastIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        updateServiceManager = (AlarmManager) baseActivity.getSystemService(Context.ALARM_SERVICE);
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

//
//        LeadDetails.setMessage(baseActivity, "Service start from BaseActivity" + new Date());
    }

    private boolean checkPermissionsBase() {

        String[] permissions = {android.Manifest.permission.ACCESS_FINE_LOCATION};
        List<String> deniedPermissionList = new ArrayList<>();

        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(baseActivity, permission) != PackageManager.PERMISSION_GRANTED) {
                deniedPermissionList.add(permission);
            }
        }
        if (deniedPermissionList.size() > 0) {
            return false;
        } else {
            return true;
        }

    }

    public boolean checkServiceRunningBase() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
//        String serviceName = getPackageName() + ".location.LocationUpdateServiceBackground";
        String serviceName = "com.pinpoint.appointment.location.LocationUpdateServiceBackground";
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {

            if (serviceName
                    .equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public void showMessagePopup(Context context, Intent intent) {


//            setupNotificationCount();

        PrefHelper.setBoolean("isnotification", false);
        if (intent.getExtras() != null) {
            String message = intent.getStringExtra(ApiList.KEY_MESSAGE);
            String title = intent.getStringExtra(ApiList.KEY_TITLE);

            if (Util.isTabletDevice(baseActivity)) {

                if (baseActivity instanceof HomeActivity) {
                    final HomeActivity homeActivity = (HomeActivity) baseActivity;
                    if (homeActivity.selectedTabPositionTablet == 2) {
                        homeActivity.setCurrentTabFragmentTablet(BottomTabsTablet.ALERTS.getType());
                        homeActivity.setupNotificationCount();
                    } else {
                        homeActivity.setupNotificationCount();
                        if (alertDialog == null) {
                            if (!Util.isDialogShowing()) {
                                Util.showAlertWithButtonClick(baseActivity, message
                                        , title
                                        , MessageHelper.getInstance().getAppMessage(getString(R.string.str_goto_alerts))
                                        , view -> {
//                                                        alertDialog.dismiss();
                                            alertDialog = null;
                                            Util.hide();
                                            if (baseActivity instanceof HomeActivity) {
                                                HomeActivity homeActivity1 = (HomeActivity) baseActivity;
                                                homeActivity1.setCurrentTabFragmentTablet(BottomTabsTablet.ALERTS.getType());
                                            } else {
                                                Intent notificationIntent = new Intent(baseActivity, HomeActivity.class);
                                                notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                notificationIntent.putExtra(BaseConstants.NOTIFICATION_CLICK, true);
                                                startActivity(notificationIntent);
                                            }
                                            PrefHelper.setBoolean(BaseConstants.NOTIFICATION_RECEIVED, true);
                                        }, MessageHelper.getInstance().getAppMessage(getString(R.string.str_ok))
                                        ,
                                        new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
//                                                  alertDialog.dismiss();
                                                Util.hide();
                                                alertDialog = null;
                                                PrefHelper.setBoolean(BaseConstants.NOTIFICATION_RECEIVED, true);
                                                if (baseActivity instanceof HomeActivity) {

                                                    homeActivity.refreshCurrentTab();
                                                }
                                            }
                                        }, true
                                );
                            }
                        }
                    }

                } else {

                    if (alertDialog == null) {
                        if (!Util.isDialogShowing()) {
                            Util.showAlertWithButtonClick(baseActivity, message
                                    , title
                                    , MessageHelper.getInstance().getAppMessage(getString(R.string.str_goto_alerts))
                                    , view -> {
//                                            alertDialog.dismiss();
                                        Util.hide();

                                        alertDialog = null;
                                        if (baseActivity instanceof HomeActivity) {
                                            HomeActivity homeActivity = (HomeActivity) baseActivity;
                                            homeActivity.setCurrentTabFragmentTablet(BottomTabsTablet.ALERTS.getType());
                                        } else {
                                            Intent notificationIntent = new Intent(baseActivity, HomeActivity.class);
                                            notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            notificationIntent.putExtra(BaseConstants.NOTIFICATION_CLICK, true);
                                            startActivity(notificationIntent);
                                        }
                                        PrefHelper.setBoolean(BaseConstants.NOTIFICATION_RECEIVED, true);
                                    }, MessageHelper.getInstance().getAppMessage(getString(R.string.str_ok))
                                    ,
                                    new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
//                                            alertDialog.dismiss();
                                            Util.hide();
                                            alertDialog = null;
                                            PrefHelper.setBoolean(BaseConstants.NOTIFICATION_RECEIVED, true);
                                            if (baseActivity instanceof HomeActivity) {
                                                HomeActivity homeActivity = (HomeActivity) baseActivity;
                                                homeActivity.refreshCurrentTab();
                                            }
                                        }
                                    }, true
                            );
                        }
                    }
                }
//                        }
//                    }
//                    Intent instatus = new Intent(context, StatusService.class);
//                    startService(instatus);
            } else {
                if (baseActivity instanceof HomeActivity) {
                    final HomeActivity homeActivity = (HomeActivity) baseActivity;
//                        homeActivity.setupNotificationCount();
                    if (homeActivity.tabLayout.getSelectedTabPosition() == BottomTabs.ALERTS.getType()) {
                        homeActivity.selectTabatPosition(BottomTabs.ALERTS.getType());
                        ((HomeActivity) homeActivity).setupNotificationCount();
                    } else {
                        if (alertDialog == null) {
                            homeActivity.setupNotificationCount();
                            if (!Util.isDialogShowing()) {
                                Util.showAlertWithButtonClick(baseActivity, message
                                        , title
                                        , MessageHelper.getInstance().getAppMessage(getString(R.string.str_goto_alerts))
                                        ,
                                        new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
//                                                    CustomDialog.getInstance().hide();
//                                                    alertDialog.dismiss();
                                                Util.hide();
                                                alertDialog = null;
                                                if (baseActivity instanceof HomeActivity) {
                                                    HomeActivity homeActivity = (HomeActivity) baseActivity;
                                                    homeActivity.selectTabatPosition(BottomTabs.ALERTS.getType());
                                                } else {
                                                    Intent notificationIntent = new Intent(baseActivity, HomeActivity.class);
                                                    notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    notificationIntent.putExtra(BaseConstants.NOTIFICATION_CLICK, true);
                                                    startActivity(notificationIntent);
                                                }
                                                PrefHelper.setBoolean(BaseConstants.NOTIFICATION_RECEIVED, true);
//
//                                                selectTabatPosition(BottomTabs.ALERTS.getType());
//
                                            }
                                        }, MessageHelper.getInstance().getAppMessage(getString(R.string.str_ok))
                                        ,
                                        new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
//                                                    alertDialog.dismiss();
                                                Util.hide();
                                                alertDialog = null;
                                                PrefHelper.setBoolean(BaseConstants.NOTIFICATION_RECEIVED, true);
                                                if (baseActivity instanceof HomeActivity) {

                                                    homeActivity.refreshCurrentTab();
                                                }
//                                                      CustomDialog.getInstance().hide();
                                            }
                                        }, true
                                );
                            }
                        }
                    }
                } else {
                    if (alertDialog == null) {
                        if (!Util.isDialogShowing()) {
                            Util.showAlertWithButtonClick(baseActivity, message
                                    , title
                                    , MessageHelper.getInstance().getAppMessage(getString(R.string.str_goto_alerts))
                                    ,
                                    new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
//                                                    CustomDialog.getInstance().hide();
//                                                alertDialog.dismiss();
                                            alertDialog = null;
                                            Util.hide();
                                            if (baseActivity instanceof HomeActivity) {
                                                HomeActivity homeActivity = (HomeActivity) baseActivity;
                                                homeActivity.selectTabatPosition(BottomTabs.ALERTS.getType());
                                            } else {
                                                Intent notificationIntent = new Intent(baseActivity, HomeActivity.class);
                                                notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                notificationIntent.putExtra(BaseConstants.NOTIFICATION_CLICK, true);
                                                startActivity(notificationIntent);
                                            }
                                            PrefHelper.setBoolean(BaseConstants.NOTIFICATION_RECEIVED, true);
//
//                                                selectTabatPosition(BottomTabs.ALERTS.getType());
//
                                        }
                                    }, MessageHelper.getInstance().getAppMessage(getString(R.string.str_ok))
                                    ,
                                    new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
//                                                alertDialog.dismiss();
                                            Util.hide();
                                            alertDialog = null;
//                                                      CustomDialog.getInstance().hide();
                                        }
                                    }, true
                            );
                        }
                    }
                }
            }
        }

//              if(!CustomDialog.getInstance().isDialogShowing())
//              {
//                  CustomDialog.getInstance().showAlertWithButtonClick(HomeActivity.this, message
//                          , MessageHelper.getInstance().getAppMessage(getString(R.string.str_app_name))
//                          , MessageHelper.getInstance().getAppMessage(getString(R.string.str_goto_alerts))
//                          ,
//                          new View.OnClickListener() {
//                              @Override
//                              public void onClick(View view) {
//                                  CustomDialog.getInstance().hide();
//                                  if (!Util.isTabletDevice(HomeActivity.this))
//                                  {
////                                  if (tabLayout != null && tabLayout.getSelectedTabPosition() == BottomTabs.ALERTS.getType())
////                                  {
//                                      selectTabatPosition(BottomTabs.ALERTS.getType());
////                                  }
//                                  }
//                                  else
//                                  {
////                                  if(selectedTabPositionTablet==2)
////                                  {
//                                      setCurrentTabFragmentTablet(2);
////                                  }
//                                  }
//                              }
//                          }, MessageHelper.getInstance().getAppMessage(getString(R.string.str_ok))
//                          ,
//                          new View.OnClickListener() {
//                              @Override
//                              public void onClick(View view) {
//                                  CustomDialog.getInstance().hide();
//                              }
//                          }, true
//                  );
//              }


//            if(Util.isTabletDevice(HomeActivity.this))
//            {
//                if (tabLayout != null && tabLayout.getSelectedTabPosition() == BottomTabs.ALERTS.getType()) {
//                    selectTabatPosition(BottomTabs.ALERTS.getType());
//                }
//            }
//            else
//            {
//                if(selectedTabPositionTablet==2)
//                {
//                    setCurrentTabFragmentTablet(2);
//                }
//            }

    }

    public void showAlertWithButtonClick(final Context context, String msg, String header, String positiveButton, View.OnClickListener onClickListener1, String negativeButton, View.OnClickListener onClickListener2, boolean isShowNegative) {

        if (alertDialog == null) {
            alertDialog = new Dialog(context, R.style.DialogTheme);

            @SuppressLint("InflateParams") final View view = LayoutInflater.from(context).inflate(R.layout.dialog_alert_new, null);
            TextView txtHeader = view.findViewById(R.id.txtHeader);
            TextView txtMessage = view.findViewById(R.id.tv_txtMessage);
            TextView txtNegative = view.findViewById(R.id.tv_txtNegative);
            ImageView iv_header = (ImageView) view.findViewById(R.id.iv_header);

            txtHeader.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_SEMIBOLD));
            txtMessage.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));
            txtNegative.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));

            if (header.equalsIgnoreCase("Alert")) {
                iv_header.setImageResource(R.drawable.ic_call_black_24dp);
            } else if (header.equalsIgnoreCase("Logout")) {
                iv_header.setImageResource(R.drawable.ic_info_outline_black_24dp);
            }
            Button btn_positive = view.findViewById(R.id.btn_positive);
            btn_positive.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_SEMIBOLD));
            txtHeader.setText(header);
            txtMessage.setText(msg);
            btn_positive.setText(positiveButton);
            txtNegative.setText(negativeButton);
            if (isShowNegative) {
                txtNegative.setVisibility(View.VISIBLE);
            }
//          mTxtYes.setTag(buttonText);*/
            btn_positive.setOnClickListener(onClickListener1);
            txtNegative.setOnClickListener(onClickListener2);

            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.setContentView(view);
            try {
                if (alertDialog != null) {
                    if (!alertDialog.isShowing()) {
                        alertDialog.show();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
