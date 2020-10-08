package com.pinpoint.appointment.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;

import androidx.databinding.DataBindingUtil;

import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.strictmode.IntentReceiverLeakedViolation;
import android.provider.Settings;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.content.ContextCompat;
import io.reactivex.disposables.CompositeDisposable;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.orhanobut.hawk.Hawk;
import com.pinpoint.appointment.BaseActivity;
import com.pinpoint.appointment.BaseApplication;
import com.pinpoint.appointment.BaseConstants;
import com.pinpoint.appointment.R;
import com.pinpoint.appointment.api.ApiList;
import com.pinpoint.appointment.api.RequestCode;
import com.pinpoint.appointment.core.FragmentNavigationInfo;
import com.pinpoint.appointment.customviews.CustomDialog;
import com.pinpoint.appointment.databinding.ActivityHomeTabBinding;
import com.pinpoint.appointment.enumeration.BottomTabs;
import com.pinpoint.appointment.enumeration.BottomTabsTablet;
import com.pinpoint.appointment.fragment.AppointmentListFragment;
import com.pinpoint.appointment.fragment.AppointmentMainFregment;
import com.pinpoint.appointment.fragment.AppointmentReceivedListFragment;
import com.pinpoint.appointment.fragment.FragmentAddAppointment;
import com.pinpoint.appointment.fragment.FriendMainFregment;
import com.pinpoint.appointment.fragment.FriendsListFragment;
import com.pinpoint.appointment.fragment.FriendsListReceivedFragment;
import com.pinpoint.appointment.fragment.NotificationListFragment;
import com.pinpoint.appointment.fragment.OpenHouseRegistryFragment;
import com.pinpoint.appointment.fragment.SettingsFragment;
import com.pinpoint.appointment.helper.DBHelper;
import com.pinpoint.appointment.helper.MessageHelper;
import com.pinpoint.appointment.helper.PrefHelper;
import com.pinpoint.appointment.helper.ToastHelper;
import com.pinpoint.appointment.interfaces.ClickEvent;
import com.pinpoint.appointment.interfaces.DataObserver;

//import com.pinpoint.appointment.location.LocationUpdateServiceBackground;
//import com.pinpoint.appointment.location.LocationUpdatesService;
import com.pinpoint.appointment.models.CustomerDetails;
import com.pinpoint.appointment.models.LeadDetails;
import com.pinpoint.appointment.models.LoginHelper;
import com.pinpoint.appointment.models.PanicData;
import com.pinpoint.appointment.models.PlanData;
import com.pinpoint.appointment.permissionUtils.PermissionClass;
import com.pinpoint.appointment.receivers.DozeChangeReceiver;
import com.pinpoint.appointment.receivers.ScheduleBroadcastReceiver;

import com.pinpoint.appointment.service.BackgroundIntentService;
import com.pinpoint.appointment.util.IabHelper;
import com.pinpoint.appointment.util.IabResult;
import com.pinpoint.appointment.util.Inventory;
import com.pinpoint.appointment.util.Purchase;
import com.pinpoint.appointment.utils.Constants;
import com.pinpoint.appointment.utils.Debug;
import com.pinpoint.appointment.utils.GPSTracker;
import com.pinpoint.appointment.utils.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Stack;

import me.leolin.shortcutbadger.ShortcutBadger;

import static com.pinpoint.appointment.permissionUtils.PermissionClass.REQUEST_CODE_PERMISSION_SETTING;

public class HomeActivity extends BaseActivity implements ClickEvent {
    private static final int CALL_PHONE_PERMISSION = 5;
    public ClickEvent onClick;
    public Fragment currentFragment;
    private Stack<FragmentNavigationInfo> navigationStack = new Stack<>();
    private Dialog dialog, alertDialog1;
    public TabLayout tabLayout;
    private Stack<Integer> stack = new Stack<>(); // Edited
    private int tabPosition = 0;
    boolean againback = false;
    public ImageView iv_back, iv_settings;
    public TextView tv_header, tv_addNew;
    private int lastTabPosition = 0;
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    ActivityHomeTabBinding tabactivitybinding;
    int panicPosition = -1;
    boolean panicPressed = false;
    public final int OPEN_GPS = 1531;
    public int selectedTabPositionTablet = 0;
    boolean mServiceBound = false, gotoNotification = false;
    public boolean isHomeActivity = true;
    IabHelper mHelper;
    static final String ITEM_SKU = "android.test.purchased";
    boolean isFromLogin = false;
    public static AlarmManager updateServiceManager;
    public static PendingIntent pendingIntentUpdateService;
    private boolean mBound = false;

    //02-22-2020 - Removed by TBL. Replaced with status update inside BackgroundIntentService
    //private LocationUpdatesService mService = null;

    // Monitors the state of the connection to the service.
//    private final ServiceConnection mServiceConnection1 = new ServiceConnection() {
//
//        @Override
//        public void onServiceConnected(ComponentName name, IBinder service) {
//            LocationUpdatesService.LocalBinder binder = (LocationUpdatesService.LocalBinder) service;
//            mService = binder.getService();
//            mBound = true;
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName name) {
//            mService = null;
//            mBound = false;
//        }
//    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BaseApplication.isAppRunning = true;
        BaseConstants.PACKAGE_NAME = getPackageName();
        ShortcutBadger.applyCount(HomeActivity.this, BaseConstants.NOTIFICATION_COUNT);
        isHomeActivity = true;
//        TimeZone timezone=TimeZone.getDefault();
//        String timezoneName=timezone.getID();
        if (Util.isTabletDevice(HomeActivity.this)) {
            tabactivitybinding = DataBindingUtil.setContentView(this, R.layout.activity_home_tab);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            iv_back = (ImageView) findViewById(R.id.iv_imgMenu_Back);
            iv_settings = (ImageView) findViewById(R.id.iv_imgSetting);
            tv_header = (TextView) findViewById(R.id.tv_txtTitle);
            tv_addNew = (TextView) findViewById(R.id.btn_addNew);
            tv_header.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_SEMIBOLD));
            tv_addNew.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_SEMIBOLD));
            tv_addNew.setText(MessageHelper.getInstance().getAppMessage(getString(R.string.str_addnew)));
            tv_header.setText(MessageHelper.getInstance().getAppMessage(getString(R.string.str_friends)));
            iv_back.setVisibility(View.GONE);
            if (getIntent() != null && getIntent().hasExtra(BaseConstants.NOTIFICATION_CLICK)) {
                gotoNotification = getIntent().getExtras().getBoolean(BaseConstants.NOTIFICATION_CLICK, false);
            }

            if (gotoNotification) {
//                PrefHelper.setBoolean("isnotification", false);
                setCurrentTabFragmentTablet(BottomTabsTablet.ALERTS.getType());
//                clearNotifications();
            } else {
//                setCurrentTabFragmentTablet(0);
                setCurrentTabFragmentTablet(BottomTabsTablet.FRIENDS.getType());
            }
            if (BaseConstants.NOTIFICATION_COUNT > 0) {
                tabactivitybinding.btNotificationCount.setVisibility(View.VISIBLE);
                tabactivitybinding.btNotificationCount.setText(String.valueOf(BaseConstants.NOTIFICATION_COUNT));
            } else {
                tabactivitybinding.btNotificationCount.setVisibility(View.GONE);
            }
            /*Intent instatus = new Intent(HomeActivity.this, StatusService.class);
            startService(instatus);*/
            scheduleUpdateService();

        } else {
            setContentView(R.layout.activity_home);
            tabLayout = (TabLayout) findViewById(R.id.tabs);
            iv_back = (ImageView) findViewById(R.id.iv_imgMenu_Back);
            iv_settings = (ImageView) findViewById(R.id.iv_imgSetting);
            tv_header = (TextView) findViewById(R.id.tv_txtTitle);
            tv_header.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_SEMIBOLD));
            setupTabIcons();
            tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    tabPosition = tab.getPosition();
                    againback = false;
                    if (panicPosition != -1) {
                        setTabUnselected(panicPosition);
                        panicPosition = -1;
                    }
                    setTabUnselected(lastTabPosition);


                    setCurrentTabFragment(tab.getPosition());
                    if (stack.empty())
                        stack.push(0);

                    if (stack.contains(tabPosition)) {
                        stack.remove(stack.indexOf(tabPosition));
                        stack.push(tabPosition);
                    } else {
                        stack.push(tabPosition);
                    }
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {
                    lastTabPosition = tab.getPosition();


                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {
                    BaseConstants.SELECT_SENT = true;
                    BaseConstants.SELECT_SENT_FRIEND = true;
                    setCurrentTabFragment(tab.getPosition());
                }
            });
            tabLayout.setSelectedTabIndicatorHeight(0);

            if (getIntent() != null && getIntent().hasExtra(BaseConstants.NOTIFICATION_CLICK) && getIntent().getExtras().getBoolean(BaseConstants.NOTIFICATION_CLICK, false)) {

                gotoNotification = getIntent().getExtras().getBoolean(BaseConstants.NOTIFICATION_CLICK, false);

                if (getIntent().hasExtra("login"))
                    isFromLogin = getIntent().getExtras().getBoolean("login", false);

//                Debug.trace("NOTIFICATIONCHECK"+gotoNotification);
            }

            if (gotoNotification) {
                PrefHelper.setBoolean("isnotification", false);
//                setCurrentTabFragment(BottomTabs.ALERTS.getType());
                TabLayout.Tab tab = tabLayout.getTabAt(BottomTabs.ALERTS.getType());
                if (tab != null) {
                    tab.select();
                }
                clearNotifications();
            } else {
                TabLayout.Tab tab = tabLayout.getTabAt(BottomTabs.FRIENDS.getType());
                if (tab != null) {
                    tab.select();
                }
            }

            //02-23-2019 - Removed by TBL
            //scheduleUpdateService();

            startLocationTracking();
            setupNotificationCount();
//          Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
        }
        registerDozeReceiver();

        //TODO for status update. Change to a new class
        //registerForegroundService();
        new Handler().postDelayed(() -> ((BaseApplication) getApplication()).bus().send(Constants.BUS_ACTION_START_STATUS_UPDATE), 1000);

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(pm.isIgnoringBatteryOptimizations(getPackageName())){
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData( Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, 7890);
            }
        }
    }

    //registerDozeReceiver
    private void registerDozeReceiver() {
        try {
            IntentFilter filter = new IntentFilter();
            //        filter.addAction("android.os.action.DEVICE_IDLE_MODE_CHANGED");
            filter.addAction(PowerManager.ACTION_DEVICE_IDLE_MODE_CHANGED);
            filter.addAction(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED);
            //        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
            //        filter.addAction("android.os.action.DEVICE_IDLE_MODE_CHANGED");

            DozeChangeReceiver receiver1 = new DozeChangeReceiver();
            registerReceiver(receiver1, filter);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
//        receiver = new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                //do something based on the intent's action
//            }
//        };
//        registerReceiver(receiver, filter);
    }

    //registerForegroundService
    /* 02-22-2020 - Removed by TBL. Replaced with status update inside BackgroundIntentService
    void registerForegroundService() {
        try {
            if ((!isServiceRunningInForeground(HomeActivity.this, LocationUpdatesService.class)) || isFromLogin) {
                isFromLogin = false;
                Intent intent = new Intent(HomeActivity.this, LocationUpdatesService.class);
                intent.putExtra(ApiList.KEY_USERID, LoginHelper.getInstance().getUserID());
                ContextCompat.startForegroundService(HomeActivity.this, intent);//startService(intent);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    */

    public static boolean isServiceRunningInForeground(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                if (service.foreground) {
                    return true;
                }

            }
        }
        return false;
    }

    public void refreshCurrentTab() {
        if (Util.isTabletDevice(HomeActivity.this)) {
            setCurrentTabFragmentTablet(selectedTabPositionTablet);
        } else {
            if (tabLayout.getSelectedTabPosition() != BottomTabs.PANIC.getType()) {
                setCurrentTabFragment(tabLayout.getSelectedTabPosition());
            } else {
                setCurrentTabFragment(lastTabPosition);
            }
        }
    }


    private void setUpInAppBilling() {

        String base64EncodedPublicKey =
                "<your license key here>";
        mHelper = new IabHelper(this, BaseConstants.base64EncodedPublicKey);

        mHelper.startSetup(new
                                   IabHelper.OnIabSetupFinishedListener() {
                                       @SuppressLint("LongLogTag")
                                       public void onIabSetupFinished(IabResult result) {
                                           if (!result.isSuccess()) {
//                                               Log.d(TAG, "In-app Billing setup failed: " +
//                                                       result);

                                           } else {
                                               buyClick(tv_header);
//                                               Log.d(TAG, "In-app Billing is set up OK");
                                           }
                                       }
                                   });
//        showProgressBar(this);
    }

    public void setTabUnselected(int tabPositionUnselected) {

        if (tabPosition != BottomTabs.PANIC.getType()) {
            switch (tabPositionUnselected) {
                case 0:
                    tabLayout.getTabAt(0).getCustomView().findViewById(R.id.icon).setBackgroundResource(R.mipmap.ic_appoinmant);
                    TextView tvTxtName1 = (TextView) tabLayout.getTabAt(0).getCustomView().findViewById(R.id.tv_txtMenuName);
                    tvTxtName1.setTextColor(getResources().getColor(R.color.white));
                    if (!BaseConstants.SELECT_SENT) {
                        BaseConstants.SELECT_SENT = true;
                    }

                    break;

                case 1:
                    tabLayout.getTabAt(1).getCustomView().findViewById(R.id.icon).setBackgroundResource(R.mipmap.ic_notyfy);
                    TextView tvTxtName2 = (TextView) tabLayout.getTabAt(1).getCustomView().findViewById(R.id.tv_txtMenuName);
                    tvTxtName2.setTextColor(getResources().getColor(R.color.white));
                    break;
//                    case 2:
//                        view1.findViewById(R.id.icon).setBackgroundResource(R.mipmap.ic_appoinmant);
//                        break;
                case 3:
                    TextView tvTxtName3 = (TextView) tabLayout.getTabAt(3).getCustomView().findViewById(R.id.tv_txtMenuName);
                    tvTxtName3.setTextColor(getResources().getColor(R.color.white));
                    tabLayout.getTabAt(3).getCustomView().findViewById(R.id.icon).setBackgroundResource(R.mipmap.ic_friends);
                    if (!BaseConstants.SELECT_SENT_FRIEND) {
                        BaseConstants.SELECT_SENT_FRIEND = true;
                    }
                    break;
                case 4:
                    TextView tvTxtName4 = (TextView) tabLayout.getTabAt(4).getCustomView().findViewById(R.id.tv_txtMenuName);
                    tvTxtName4.setTextColor(getResources().getColor(R.color.white));
                    tabLayout.getTabAt(4).getCustomView().findViewById(R.id.icon).setBackgroundResource(R.mipmap.ic_setting);
                    break;
                default:
                    break;
            }
        } else {
            panicPosition = tabPositionUnselected;
        }
    }

    private CompositeDisposable disposables = new CompositeDisposable();

    private void startLocationTracking(){

        if(!checkPermissions()) {
            requestPermissions();
            return;
        }

        if (!CheckGpsStatus()) {
            showOpenGPSDialog();
            return;
        }

        isFromLogin = false;

        //((BaseApplication) getApplication()).bus().send(Constants.BUS_ACTION_START_LOCATION_TRACKING);

        toggleLocationTracking(true);

        disposables.add(
                ((BaseApplication) getApplication()).locationBus().toObservable().subscribe(location ->
                        Log.e("TBL", "received locationUpdate " + new Gson().toJson(location))
                )
        );
    }

    /* 02-09-2020 - Replaced by TBL. Please see new startLocationTracking function
    private void startLocationTracking() {
        if (!checkServiceRunning()) {
            if (!checkPermissions()) {
                requestPermissions();
            } else {
                isFromLogin = false;
                startServiceIntent();
            }
        } else if (isFromLogin) {
            isFromLogin = false;
            if (!checkPermissions()) {
                requestPermissions();
            } else {
                startServiceIntent();
            }
        }
        try {
            if (!CheckGpsStatus()) {
                showOpenGPSDialog();
            }
        } catch (Exception ex) {
        }
    }
    */

    private void startServiceIntent() {
//        if (CheckGpsStatus()) {
//            Intent mServiceIntent = new Intent(this, DriverLocationService.class);
//            startService(mServiceIntent);

//        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            Intent i = new Intent(HomeActivity.this, LocationUpdateServiceBackgroundJob.class);  //is any of that needed?  idk.
//            //note, putExtra remembers type and I need this to be an integer.  so get an integer first.
//            i.putExtra("updateTimeInterval", 2000L);//in milliseconds
//            i.putExtra("updateDistance", 1f);//in meters
//            i.putExtra(ApiList.KEY_USERID, Integer.parseInt(LoginHelper.getInstance().getUserID()));
//            Debug.trace("USERID : FROM HOME" + LoginHelper.getInstance().getUserID());
//            i.putExtra("isDistanceRequired", false);//To enable minimum distance for location check
//            startService(i);
////            LocationUpdateServiceBackgroundJob.enqueueWork(HomeActivity.this,i);
////             only for gingerbread and newer versions
////            LocationUpdateServiceBackgroundJob.scheduleJob(MainActivity.this, Integer.valueOf(et_input.getText().toString()), false);  //mainActivity context, not listener...
//
//        }
//        else {


        /* 02-22-2020 - Removed by TBL. Replaced with BackgroundIntentService
        try {
            Intent ina = new Intent(HomeActivity.this, LocationUpdateServiceBackground.class);
            ina.putExtra("updateTimeInterval", 2000L);//in milliseconds
            ina.putExtra("updateDistance", 1f);//in meters
            ina.putExtra(ApiList.KEY_USERID, Integer.parseInt(LoginHelper.getInstance().getUserID()));
            Debug.trace("USERID : FROM HOME" + LoginHelper.getInstance().getUserID());
            ina.putExtra("isDistanceRequired", false);//To enable minimum distance for location check
            startService(ina);
        } catch (Exception ex) {
            LeadDetails.setMessage(HomeActivity.this, " From : HomeActivity startserviceintent Exception:" + ex.toString() + new Date());
        }
        */

//        }

//        if(!CheckGpsStatus())
//        {
//            showOpenGPSDialog();
//
//        }


           /* Intent instatus = new Intent(HomeActivity.this, StatusService.class);
            startService(instatus);*/
//        scheduleUpdateService();
    }


    //startAlarmService
    private void startAlarmService() {
        if (!Util.checkSensorAlaram(this)) {
            Util.startSensorAlaram(this, Constants.SENSOR_REPORTING_5_MINUTES);
        }
    }


    //scheduleUpdateService
    public void scheduleUpdateService() {

        startAlarmService();

        if (pendingIntentUpdateService != null) {
            return;
        }
        Intent toastIntent = new Intent(getApplicationContext(), ScheduleBroadcastReceiver.class);
        toastIntent.putExtra("userId", Integer.parseInt(LoginHelper.getInstance().getUserID()));
        toastIntent.putExtra("isTablet", false);
        if (Util.isTabletDevice(HomeActivity.this)) {
            toastIntent.putExtra("isTablet", true);
        }
        pendingIntentUpdateService = PendingIntent.getBroadcast(getApplicationContext(), 0,
                toastIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        updateServiceManager = (AlarmManager) HomeActivity.this.getSystemService(Context.ALARM_SERVICE);
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

//        if (updateServiceManager != null)
//            updateServiceManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
//                    2*60 * 1000, pendingIntentUpdateService);
//        LeadDetails.setMessage(HomeActivity.this, "Service start from HomeActivity" + new Date());
    }

    public void stopScheduleService() {

        //by sarfaraj
        Util.stopAlaramSensor(this);

//        LeadDetails.setMessage(HomeActivity.this, "Service STOP from HomeActivity" + new Date());
        try {
            AlarmManager manager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
            if (manager != null) {
                manager.cancel(pendingIntentUpdateService);//cancel the alarm manager of the pending intent

            }
            pendingIntentUpdateService = null;
        } catch (Exception exc) {
        }
//        stopService(new Intent(HomeActivity.this, UpdateStatusService.class));

    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mServiceBound = false;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

        }
    };

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
//                Log.i ("isMyServiceRunning?", true+"");
                return true;
            }
        }
//        Log.i ("isMyServiceRunning?", false+"");
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        toggleLocationTracking(false);
        toggleBackgroundService(false);

        disposables.clear();

        BaseApplication.isAppRunning = false;
//      LocalBroadcastManager.getInstance(this).unregisterReceiver(myReceiver);
        if (mHelper != null) {
            try {
                mHelper.dispose();
            } catch (IabHelper.IabAsyncInProgressException e) {
                e.printStackTrace();
            }
        }
        mHelper = null;
        ToastHelper.hideToast();
//      clearNotifications();

    }

    public boolean CheckGpsStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            return true;
        } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            return true;
        } else {
            return false;
        }
//        String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
//        if(provider != null){
//            return true;
//            //Start searching for location and update the location text when update available
//
//        }else{
//            return false;
//            // Notify users and show settings if they want to enable GPS
//        }
    }

    private boolean checkPermissions() {

        String[] permissions = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CALL_PHONE};
        List<String> deniedPermissionList = new ArrayList<>();

        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(HomeActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {
                deniedPermissionList.add(permission);
            }
        }
        if (deniedPermissionList.size() > 0) {
            return false;
        } else {
            return true;
        }
//        return  PermissionClass.checkPermission(HomeActivity.this, PermissionClass.REQUEST_CODE_RUNTIME_PERMISSION,
//                Arrays.asList(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.CALL_PHONE));

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {


        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 1:
            case REQUEST_CODE_PERMISSION_SETTING:

                //02-10-2020 - Replaced by TBL. Use startLocationTracking
                //startServiceIntent();
                startLocationTracking();

                break;
            case 2:

                if (PrefHelper.getInt(BaseConstants.PAYMENT_STATUS, 0) == 1)
                    call911();
                break;

                /*
                if (checkPermissions()) {
                    startServiceIntent();
                } else {
                    displaySnackBar(PermissionClass.REQUEST_CODE_PERMISSION_SETTING);
                }
                */


            case OPEN_GPS:
                if (resultCode == 0) {
                    String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
                    if (CheckGpsStatus()) {

                    } else {
                        showOpenGPSDialog();
                        //Users did not switch on the GPS
                    }
                }
                break;
        }
        if (mHelper != null && !mHelper.handleActivityResult(requestCode,
                resultCode, data)) {
        }

    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {

            displaySnackBar(REQUEST_CODE_PERMISSION_SETTING);

        } else {
            displaySnackBar(REQUEST_CODE_PERMISSION_SETTING);

        }
    }

    /* 02-22-2020 - Removed by TBL. Replaced with BackgroundIntentService
    public boolean checkServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        //String serviceName = "com.pinpoint.appointment.location.LocationUpdateServiceBackground";
        String serviceName = "com.pinpoint.appointment.location.LocationUpdateServiceBackground";
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {

            if (serviceName
                    .equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
    */

    public boolean checkForeGroundServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
//        String serviceName = getPackageName() + ".location.LocationUpdateServiceBackground";
        String serviceName = "com.pinpoint.appointment.location.LocationUpdatesService";
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {

            if (serviceName
                    .equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public void setCurrentTabFragment(int position) {

        switch (position) {
            case 0:
                if (!BaseConstants.FROM_NOTIFICATION) {
                    BaseConstants.SELECT_SENT = false;
                }
                BaseConstants.FROM_NOTIFICATION = false;
                tabLayout.getTabAt(0).getCustomView().findViewById(R.id.icon).setBackgroundResource(R.mipmap.ic_sel_appoinmant);
                TextView tvTxtName = (TextView) tabLayout.getTabAt(0).getCustomView().findViewById(R.id.tv_txtMenuName);
                tvTxtName.setTextColor(getResources().getColor(R.color.grey_color));

                Fragment appointmentListFragment = getSupportFragmentManager().findFragmentByTag(AppointmentMainFregment.class.getName());
                if (appointmentListFragment != null) {
                    loadFragment(new AppointmentMainFregment());
                } else {
                    addFragment(new AppointmentMainFregment());
                }

                break;
            case 1:

                tabLayout.getTabAt(1).getCustomView().findViewById(R.id.icon).setBackgroundResource(R.mipmap.ic_sel_notyfy);
                TextView tvTxtName1 = (TextView) tabLayout.getTabAt(1).getCustomView().findViewById(R.id.tv_txtMenuName);
                tvTxtName1.setTextColor(getResources().getColor(R.color.grey_color));
                Fragment notificationListFragment = getSupportFragmentManager().findFragmentByTag(NotificationListFragment.class.getName());
                if (notificationListFragment != null) {
                    loadFragment(new NotificationListFragment());
                } else {
                    addFragment(new NotificationListFragment());
                }
                break;
            case 2:
                if (/*PrefHelper.getInt(BaseConstants.PAYMENT_STATUS, 0)*/ 1 == 1) {
                    panicPressed = true;
                    showPanicButtonDialog();
                } else {
                    CustomDialog.getInstance().showAlertWithButtonClick(HomeActivity.this, MessageHelper.getInstance().getAppMessage(getString(R.string.str_please_subscribe)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_app_name)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_subscribe_button)), new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            CustomDialog.getInstance().hide();
//                            Intent ina=new Intent(HomeActivity.this,ExtendSubscriptionActivity.class);
//                            startActivity(ina);
                            setUpInAppBilling();
                        }
                    }, MessageHelper.getInstance().getAppMessage(getString(R.string.str_cancel)), new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            CustomDialog.getInstance().hide();
                        }
                    }, true);
                }


                break;
            case 3:
                if (!BaseConstants.FROM_NOTIFICATION) {
                    BaseConstants.SELECT_SENT_FRIEND = false;

                }
                BaseConstants.FROM_NOTIFICATION = false;
                tabLayout.getTabAt(3).getCustomView().findViewById(R.id.icon).setBackgroundResource(R.mipmap.ic_sel_friends);
                TextView tvTxtName3 = (TextView) tabLayout.getTabAt(3).getCustomView().findViewById(R.id.tv_txtMenuName);
                tvTxtName3.setTextColor(getResources().getColor(R.color.grey_color));

                Fragment friendsListFragment = getSupportFragmentManager().findFragmentByTag(FriendMainFregment.class.getName());
                if (friendsListFragment != null) {
                    loadFragment(new FriendMainFregment());
                } else {
                    addFragment(new FriendMainFregment());
                }
                break;
            case 4:
                tabLayout.getTabAt(4).getCustomView().findViewById(R.id.icon).setBackgroundResource(R.mipmap.ic_sel_setting);
                TextView tvTxtName4 = (TextView) tabLayout.getTabAt(4).getCustomView().findViewById(R.id.tv_txtMenuName);
                tvTxtName4.setTextColor(getResources().getColor(R.color.grey_color));
//
//                Intent ina=new Intent(HomeActivity.this,AddLeadsActivity.class);
//                startActivity(ina);

                Fragment settingsFragment = getSupportFragmentManager().findFragmentByTag(SettingsFragment.class.getName());
                if (settingsFragment != null) {
                    loadFragment(new SettingsFragment());
                } else {
                    addFragment(new SettingsFragment());
                }

                break;
        }
    }

    public void setCurrentTabFragmentTablet(int position) {
        selectedTabPositionTablet = position;
        tv_addNew.setVisibility(View.GONE);
        switch (position) {
            case 0:
                clearNotifications();
                tabactivitybinding.tvTxtAppointment.setTextColor(getResources().getColor(R.color.grey_color));
                tabactivitybinding.tvTxtalerts.setTextColor(getResources().getColor(R.color.white));
                tabactivitybinding.tvTxtOpenHouse.setTextColor(getResources().getColor(R.color.white));
                tabactivitybinding.tvTxtFriends.setTextColor(getResources().getColor(R.color.white));
                tabactivitybinding.tvTxtSettings.setTextColor(getResources().getColor(R.color.white));
                tabactivitybinding.ivAppointment.setImageResource(R.mipmap.ic_sel_appoinmant);
                tabactivitybinding.ivOpenHouse.setImageResource(R.drawable.ic_home_white_24dp);
                tabactivitybinding.ivAlerts.setImageResource(R.mipmap.ic_notyfy);
                tabactivitybinding.ivFriends.setImageResource(R.mipmap.ic_friends);
                tabactivitybinding.ivSettings.setImageResource(R.mipmap.ic_setting);

                Fragment appointmentListFragment = getSupportFragmentManager().findFragmentByTag(AppointmentMainFregment.class.getName());
                if (appointmentListFragment != null) {
                    loadFragment(new AppointmentMainFregment());
                } else {
                    addFragment(new AppointmentMainFregment());
                }

                break;
            case 1:
                clearNotifications();
                if (/*PrefHelper.getInt(BaseConstants.PAYMENT_STATUS, 0)*/ 1 == 1) {
                    tabactivitybinding.tvTxtAppointment.setTextColor(getResources().getColor(R.color.white));
                    tabactivitybinding.tvTxtalerts.setTextColor(getResources().getColor(R.color.white));
                    tabactivitybinding.tvTxtOpenHouse.setTextColor(getResources().getColor(R.color.grey_color));
                    tabactivitybinding.tvTxtFriends.setTextColor(getResources().getColor(R.color.white));
                    tabactivitybinding.tvTxtSettings.setTextColor(getResources().getColor(R.color.white));
                    tabactivitybinding.ivAppointment.setImageResource(R.mipmap.ic_appoinmant);
                    tabactivitybinding.ivOpenHouse.setImageResource(R.drawable.ic_home_blue_24dp);
                    tabactivitybinding.ivAlerts.setImageResource(R.mipmap.ic_notyfy);
                    tabactivitybinding.ivFriends.setImageResource(R.mipmap.ic_friends);
                    tabactivitybinding.ivSettings.setImageResource(R.mipmap.ic_setting);

                    Fragment notificationListFragment = getSupportFragmentManager().findFragmentByTag(OpenHouseRegistryFragment.class.getName());
                    if (notificationListFragment != null) {
                        loadFragment(new OpenHouseRegistryFragment());
                    } else {
                        addFragment(new OpenHouseRegistryFragment());
                    }
                } else {
                    CustomDialog.getInstance().showAlertWithButtonClick(HomeActivity.this, MessageHelper.getInstance().getAppMessage(getString(R.string.str_please_subscribe)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_app_name)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_subscribe_button)), new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            CustomDialog.getInstance().hide();
//                            Intent ina=new Intent(HomeActivity.this,ExtendSubscriptionActivity.class);
//                            startActivity(ina);
                            setUpInAppBilling();
                        }
                    }, MessageHelper.getInstance().getAppMessage(getString(R.string.str_cancel)), new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            CustomDialog.getInstance().hide();
                        }
                    }, true);
                }

                break;
            case 2:
                tabactivitybinding.tvTxtAppointment.setTextColor(getResources().getColor(R.color.white));
                tabactivitybinding.tvTxtalerts.setTextColor(getResources().getColor(R.color.grey_color));
                tabactivitybinding.tvTxtOpenHouse.setTextColor(getResources().getColor(R.color.white));
                tabactivitybinding.tvTxtFriends.setTextColor(getResources().getColor(R.color.white));
                tabactivitybinding.tvTxtSettings.setTextColor(getResources().getColor(R.color.white));
                tabactivitybinding.ivAppointment.setImageResource(R.mipmap.ic_appoinmant);
                tabactivitybinding.ivOpenHouse.setImageResource(R.drawable.ic_home_white_24dp);
                tabactivitybinding.ivAlerts.setImageResource(R.mipmap.ic_sel_notyfy);
                tabactivitybinding.ivFriends.setImageResource(R.mipmap.ic_friends);
                tabactivitybinding.ivSettings.setImageResource(R.mipmap.ic_setting);


                Fragment friendsListFragment = getSupportFragmentManager().findFragmentByTag(NotificationListFragment.class.getName());
                if (friendsListFragment != null) {
                    loadFragment(new NotificationListFragment());
                } else {
                    addFragment(new NotificationListFragment());
                }
                if (BaseConstants.NOTIFICATION_COUNT > 0) {
                    tabactivitybinding.btNotificationCount.setVisibility(View.VISIBLE);
                    tabactivitybinding.btNotificationCount.setText(String.valueOf(BaseConstants.NOTIFICATION_COUNT));
                } else {
                    tabactivitybinding.btNotificationCount.setVisibility(View.GONE);
                }
                break;

            case 3:
                clearNotifications();
                tabactivitybinding.tvTxtAppointment.setTextColor(getResources().getColor(R.color.white));
                tabactivitybinding.tvTxtalerts.setTextColor(getResources().getColor(R.color.white));
                tabactivitybinding.tvTxtOpenHouse.setTextColor(getResources().getColor(R.color.white));
                tabactivitybinding.tvTxtFriends.setTextColor(getResources().getColor(R.color.grey_color));
                tabactivitybinding.tvTxtSettings.setTextColor(getResources().getColor(R.color.white));
                tabactivitybinding.ivAppointment.setImageResource(R.mipmap.ic_appoinmant);
                tabactivitybinding.ivOpenHouse.setImageResource(R.drawable.ic_home_white_24dp);
                tabactivitybinding.ivAlerts.setImageResource(R.mipmap.ic_notyfy);
                tabactivitybinding.ivFriends.setImageResource(R.mipmap.ic_sel_friends);
                tabactivitybinding.ivSettings.setImageResource(R.mipmap.ic_setting);
                Fragment friendsListFragment1 = getSupportFragmentManager().findFragmentByTag(FriendMainFregment.class.getName());
                if (friendsListFragment1 != null) {
                    loadFragment(new FriendMainFregment());
                } else {
                    addFragment(new FriendMainFregment());
                }
                break;

            case 4:
                clearNotifications();
                tabactivitybinding.tvTxtAppointment.setTextColor(getResources().getColor(R.color.white));
                tabactivitybinding.tvTxtalerts.setTextColor(getResources().getColor(R.color.white));
                tabactivitybinding.tvTxtOpenHouse.setTextColor(getResources().getColor(R.color.white));
                tabactivitybinding.tvTxtFriends.setTextColor(getResources().getColor(R.color.white));
                tabactivitybinding.tvTxtSettings.setTextColor(getResources().getColor(R.color.grey_color));
                tabactivitybinding.ivAppointment.setImageResource(R.mipmap.ic_appoinmant);
                tabactivitybinding.ivOpenHouse.setImageResource(R.drawable.ic_home_white_24dp);
                tabactivitybinding.ivAlerts.setImageResource(R.mipmap.ic_notyfy);
                tabactivitybinding.ivFriends.setImageResource(R.mipmap.ic_friends);
                tabactivitybinding.ivSettings.setImageResource(R.mipmap.ic_sel_setting);


                Fragment settingsFragment = getSupportFragmentManager().findFragmentByTag(SettingsFragment.class.getName());
                if (settingsFragment != null) {
                    loadFragment(new SettingsFragment());
                } else {
                    addFragment(new SettingsFragment());
                }


                break;


            case 5:
                showLogoutDialog();
//                showChangePasswordDialog();
                break;
        }
    }

    //showOpenGPSDialog
    private void showOpenGPSDialog() {

        CustomDialog.getInstance().hide();

        if (dialog != null && dialog.isShowing())
            dialog.dismiss();

        String msg = "You Must Allow Location Permission to proceed for this app.";
        //MessageHelper.getInstance().getAppMessage(getString(R.string.str_enable_gps));
        showPanicAlertWithButtonClick(HomeActivity.this, msg, MessageHelper.getInstance().getAppMessage(getString(R.string.str_app_name)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_ok)), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (dialog != null && dialog.isShowing())
                    dialog.dismiss();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(intent, OPEN_GPS);
                //                call911();
            }
        }, MessageHelper.getInstance().getAppMessage(getString(R.string.str_dismiss)), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (dialog != null && dialog.isShowing())
                    dialog.dismiss();
                //                CustomDialog.getInstance().hide();
            }
        }, false);

    }

    //showPanicButtonDialog
    private void showPanicButtonDialog() {
        CustomDialog.getInstance().hide();
        if (dialog != null && dialog.isShowing())
            dialog.dismiss();
        showPanicAlertWithButtonClick(HomeActivity.this, MessageHelper.getInstance().getAppMessage(getString(R.string.str_call_911)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_app_name)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_ok)), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (dialog != null && dialog.isShowing())
                    dialog.dismiss();
                call911();

            }
        }, MessageHelper.getInstance().getAppMessage(getString(R.string.str_dismiss)), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (dialog != null && dialog.isShowing())
                    dialog.dismiss();
//                CustomDialog.getInstance().hide();
            }
        }, true);
    }


    //showPanicAlertWithButtonClick
    public void showPanicAlertWithButtonClick(final Context context, String msg, String header, String positiveButton, View.OnClickListener onClickListener1, String negativeButton, View.OnClickListener onClickListener2, boolean isShowNegative) {

        dialog = new Dialog(context, R.style.DialogTheme);
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
//        mTxtYes.setTag(buttonText);*/
        btn_positive.setOnClickListener(onClickListener1);
        txtNegative.setOnClickListener(onClickListener2);

        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(view);
        try {
            if (dialog != null) {
                if (!dialog.isShowing()) {
                    dialog.show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void checkPaymentStatus() {
        if (!checkPermissions()) {
            requestPermissions();
        } else {

            if (CheckGpsStatus()) {
                CustomDialog.getInstance().hide();
                GPSTracker gpsTracker = new GPSTracker(HomeActivity.this);
                if (gpsTracker.canGetLocation()) {

                    PanicData.callCheckPaymentStatus(HomeActivity.this, dataObserver);
                }
                gpsTracker.stopUsingGPS();
                this.stopService(new Intent(HomeActivity.this, GPSTracker.class));

            } else {
                startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), 2);
            }


        }


    }

    private void showLogoutDialog() {


        CustomDialog.getInstance().hide();
        if (dialog != null && dialog.isShowing())
            dialog.dismiss();
        showPanicAlertWithButtonClick(HomeActivity.this, MessageHelper.getInstance().getAppMessage(getString(R.string.str_ask_logout)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_app_name)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_ok)), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (dialog != null && dialog.isShowing())
                    dialog.dismiss();
                CustomDialog.getInstance().hide();
                CustomerDetails.callLogoutUser(HomeActivity.this, dataObserver);

            }
        }, MessageHelper.getInstance().getAppMessage(getString(R.string.str_dismiss)), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (dialog != null && dialog.isShowing())
                    dialog.dismiss();
//                CustomDialog.getInstance().hide();
            }
        }, true);

//      CustomDialog.getInstance().showAlert(this,Util.getAppKeyValue(this, R.string.str_enter_Name),"Error", Util.getAppKeyValue(this, R.string.lblOk), Util.getAppKeyValue(this, R.string.str_dismiss),false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isHomeActivity = true;
        if (BaseConstants.REFRESH_FRIENDS) {
            BaseConstants.REFRESH_FRIENDS = false;

            if (!Util.isTabletDevice(HomeActivity.this)) {
//                setCurrentTabFragment(3);
                BaseConstants.SELECT_SENT_FRIEND = false;
                selectTabatPosition(BottomTabs.FRIENDS.getType());

            } else {
                BaseConstants.SELECT_SENT_FRIEND = false;
                BaseConstants.FROM_NOTIFICATION = false;
                setCurrentTabFragmentTablet(BottomTabsTablet.FRIENDS.getType());
            }
        } else if (BaseConstants.REFRESH_APPOINTMENT) {
            BaseConstants.REFRESH_APPOINTMENT = false;

            if (!Util.isTabletDevice(HomeActivity.this)) {
//                setCurrentTabFragment(0);
//                TabLayout.Tab tab = tabLayout.getTabAt(0);
//                if (tab != null) {
//                    tab.select();
//                }
                BaseConstants.SELECT_SENT = false;
                selectTabatPosition(BottomTabs.APPOINTMENTS.getType());
            } else {
                BaseConstants.SELECT_SENT = false;
                BaseConstants.FROM_NOTIFICATION = false;

                setCurrentTabFragmentTablet(BottomTabsTablet.APPOINTMENTS.getType());
            }
        } else if (BaseConstants.REFRESH_PROPERTIES) {
            BaseConstants.REFRESH_PROPERTIES = false;
            setCurrentTabFragmentTablet(BottomTabsTablet.OPENHOUSE.getType());
        }
        if (!checkPermissions()) {
            displaySnackBar(REQUEST_CODE_PERMISSION_SETTING);
        }

        toggleBackgroundService(true);

        setupNotificationCount();

        if (!Util.isTabletDevice(HomeActivity.this)) {
            new Handler().postDelayed(this::startLocationTracking, 1000);
        }
//        stopScheduleService();

        //02-23-2019 - Removed by TBL
        //scheduleUpdateService();


        //doze mode
        //energySavingSetup();
    }

    public void clearNotifications() {
        NotificationManager nMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nMgr.cancelAll();
//        resetNotificationPreference();
        gotoNotification = false;
        PrefHelper.setBoolean("isnotification", false);
    }

    public void selectTabatPosition(int position) {

        TabLayout.Tab tab = tabLayout.getTabAt(position);
        if (tab != null) {
            tab.select();
        }
    }

    public void handlePermission() {
        if (ContextCompat.checkSelfPermission(HomeActivity.this,
                Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            // This is Case 1. Now we need to check further if permission was shown before or not

            if (ActivityCompat.shouldShowRequestPermissionRationale(HomeActivity.this,
                    Manifest.permission.CALL_PHONE)) {


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(HomeActivity.this,
                        new String[]{Manifest.permission.READ_CONTACTS},
                        CALL_PHONE_PERMISSION);
            }

        } else {
            call911();
            // This is Case 2. You have permission now you can do anything related to it
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {


        if (requestCode == CALL_PHONE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (panicPressed) {

                    call911();
//                    checkPaymentStatus();
                }

            } else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
//                ToastHelper.displayCustomToast(MessageHelper.getInstance().getAppMessage(getString(R.string.str_need_callpersmission)));
                handlePermission();
            }                //Utilities.requestPermissions(this);
        } else if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                //startServiceIntent();
                startLocationTracking();
            else
                handleLocationPermission();

            /* 02-10-2020 - By TBL. Revised if else condition and replaced startServiceIntent with startLocationTracking
            if (grantResults.length <= 0) {

            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                startServiceIntent();
                if (panicPressed) {
//                    checkPaymentStatus();
                }

            } else {
                handleLocationPermission();
            }
            */
        }

    }

    public void handleLocationPermission() {
        if (ContextCompat.checkSelfPermission(HomeActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // This is Case 1. Now we need to check further if permission was shown before or not

            if (ActivityCompat.shouldShowRequestPermissionRationale(HomeActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                displaySnackBar(REQUEST_CODE_PERMISSION_SETTING);


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(HomeActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        PermissionClass.REQUEST_CODE_RUNTIME_PERMISSION);
            }

        } else {
//            call911();
            // This is Case 2. You have permission now you can do anything related to it
        }
    }

    @SuppressLint("MissingPermission")
    private void call911() {
        try {
            if (PermissionClass.checkPermission(HomeActivity.this, CALL_PHONE_PERMISSION,
                    Arrays.asList(Manifest.permission.CALL_PHONE, Manifest.permission.ACCESS_FINE_LOCATION))) {

                if (panicPressed) {
                    panicPressed = false;

                    Location location = Hawk.get(Constants.P_KEY_LOCATION);

                    if(location != null){
                        PanicData.callPanicPressed(HomeActivity.this, dataObserver, location.getLatitude(), location.getLongitude());
                    }else{
                        GPSTracker gpsTracker = new GPSTracker(HomeActivity.this);
                        PanicData.callPanicPressed(HomeActivity.this, dataObserver, gpsTracker.getLatitude(), gpsTracker.getLongitude());
                    }

                    /* 02-22-2020 - Removed by TBL. Replaced with BackgroundIntentService
                    if (checkServiceRunning()) {
                        if (BaseConstants.lastLatLOng != null) {
                            PanicData.callPanicPressed(HomeActivity.this, dataObserver, BaseConstants.lastLatLOng.latitude, BaseConstants.lastLatLOng.longitude);

                        } else {
                            GPSTracker gpsTracker = new GPSTracker(HomeActivity.this);
                            PanicData.callPanicPressed(HomeActivity.this, dataObserver, gpsTracker.getLatitude(), gpsTracker.getLongitude());
                        }
                    } else {
                        GPSTracker gpsTracker = new GPSTracker(HomeActivity.this);
                        PanicData.callPanicPressed(HomeActivity.this, dataObserver, gpsTracker.getLatitude(), gpsTracker.getLongitude());
                    }
                    */

                    final Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_CALL);
                    intent.setData(Uri.parse("tel:911"));
                    startActivity(intent);
                }
            }
        } catch (Exception ex) {

        }
    }

    private void displaySnackBar(final int requestCode) {
        Snackbar.make(findViewById(R.id.activity_main), MessageHelper.getInstance().getAppMessage(getString(R.string.str_request_location_permission)), Snackbar.LENGTH_INDEFINITE)
                .setAction(MessageHelper.getInstance().getAppMessage(getString(R.string.str_settings)), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivityForResult(intent, requestCode);
                    }
                })
                .show();
    }

    private void setupTabIcons() {
        View view1 = getLayoutInflater().inflate(R.layout.custom_tab, null);
        view1.findViewById(R.id.icon).setBackgroundResource(R.mipmap.ic_appoinmant);
        TextView tvTxtName = (TextView) view1.findViewById(R.id.tv_txtMenuName);
        tvTxtName.setText(MessageHelper.getInstance().getAppMessage(getString(R.string.str_appointments)));
        tvTxtName.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));
        ImageView iv_whiteLine = (ImageView) view1.findViewById(R.id.iv_whiteLine);
        iv_whiteLine.setVisibility(View.VISIBLE);
        tabLayout.addTab(tabLayout.newTab().setCustomView(view1));

        View view2 = getLayoutInflater().inflate(R.layout.custom_tab, null);
        view2.findViewById(R.id.icon).setBackgroundResource(R.mipmap.ic_notyfy);
        TextView tvTxtName2 = (TextView) view2.findViewById(R.id.tv_txtMenuName);
        Button btnNotificationCount = (Button) view2.findViewById(R.id.bt_NotificationCount);
        if (BaseConstants.NOTIFICATION_COUNT > 0) {
            btnNotificationCount.setVisibility(View.VISIBLE);
            btnNotificationCount.setText(String.valueOf(BaseConstants.NOTIFICATION_COUNT));
        } else {
            btnNotificationCount.setVisibility(View.GONE);
        }
        tvTxtName2.setText(MessageHelper.getInstance().getAppMessage(getString(R.string.str_alerts)));
        tvTxtName2.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));
        tabLayout.addTab(tabLayout.newTab().setCustomView(view2));

        View view3 = getLayoutInflater().inflate(R.layout.custom_tab_big, null);

        Button img = (Button) view3.findViewById(R.id.icon);
        img.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));
        view3.setPadding(0, 0, 0, 0);

        tabLayout.addTab(tabLayout.newTab().setCustomView(view3));


        View view4 = getLayoutInflater().inflate(R.layout.custom_tab, null);
        view4.findViewById(R.id.icon).setBackgroundResource(R.mipmap.ic_friends);
        TextView tvTxtName4 = (TextView) view4.findViewById(R.id.tv_txtMenuName);
        tvTxtName4.setText(MessageHelper.getInstance().getAppMessage(getString(R.string.str_friends)));
        tvTxtName4.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));
        ImageView iv_whiteLine4 = (ImageView) view4.findViewById(R.id.iv_whiteLine);
        iv_whiteLine4.setVisibility(View.VISIBLE);
        tabLayout.addTab(tabLayout.newTab().setCustomView(view4));

        View view5 = getLayoutInflater().inflate(R.layout.custom_tab, null);
        view5.findViewById(R.id.icon).setBackgroundResource(R.mipmap.ic_setting);
        TextView tvTxtName5 = (TextView) view5.findViewById(R.id.tv_txtMenuName);
        tvTxtName5.setText(MessageHelper.getInstance().getAppMessage(getString(R.string.str_settings)));
        tvTxtName5.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));
        tabLayout.addTab(tabLayout.newTab().setCustomView(view5));
    }

    @Override
    public void onBackPressed() {
        if (againback) {
            BaseApplication.isAppRunning = false;
            clearNotifications();
            finish();
        } else {
            ToastHelper.displayCustomToast(MessageHelper.getInstance().getAppMessage(getString(R.string.str_back_again)));
            againback = true;
        }


    }

    @Override
    public void onClickEvent(View view) {
        switch (view.getId()) {
            case R.id.iv_imgSetting:
                isHomeActivity = false;
                if (currentFragment instanceof AppointmentMainFregment) {
                    if (/*PrefHelper.getInt(BaseConstants.PAYMENT_STATUS, 0)*/ 1 == 1) {
                        Intent ina = new Intent(HomeActivity.this, FragmentAddAppointment.class);
                        startActivity(ina);
                    } else {
                        CustomDialog.getInstance().showAlertWithButtonClick(HomeActivity.this, MessageHelper.getInstance().getAppMessage(getString(R.string.str_please_subscribe)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_app_name)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_subscribe_button)), new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                CustomDialog.getInstance().hide();
//                                Intent ina=new Intent(HomeActivity.this,ExtendSubscriptionActivity.class);
//                                startActivity(ina);
                                setUpInAppBilling();

                            }
                        }, MessageHelper.getInstance().getAppMessage(getString(R.string.str_cancel)), new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                CustomDialog.getInstance().hide();
                            }
                        }, true);
                    }

                } else if (currentFragment instanceof FriendMainFregment) {
                    Intent ina = new Intent(HomeActivity.this, AddFriendActivity.class);
                    startActivity(ina);
                } else if (currentFragment instanceof OpenHouseRegistryFragment) {
                    Intent ina = new Intent(HomeActivity.this, AddPropertyActivity.class);
                    startActivity(ina);
                } else if (currentFragment instanceof NotificationListFragment) {
                    ((NotificationListFragment) currentFragment).onClickEvent(view);
                }

                break;
            case R.id.btn_addNew:
                isHomeActivity = false;
                if (currentFragment instanceof OpenHouseRegistryFragment) {
                    Intent ina = new Intent(HomeActivity.this, AddPropertyActivity.class);
                    startActivity(ina);
                }
                break;
            case R.id.iv_imgMenu_Back:
                break;
            case R.id.ll_appointment:
//                BaseConstants.SELECT_SENT = true;
                if (!BaseConstants.FROM_NOTIFICATION) {
                    BaseConstants.SELECT_SENT = false;
                }
                BaseConstants.FROM_NOTIFICATION = false;
                setCurrentTabFragmentTablet(BottomTabsTablet.APPOINTMENTS.getType());
                break;
            case R.id.ll_openhouse:
                BaseConstants.FROM_NOTIFICATION = false;
                setCurrentTabFragmentTablet(BottomTabsTablet.OPENHOUSE.getType());
                break;
            case R.id.ll_alerts:
                BaseConstants.FROM_NOTIFICATION = false;
                setCurrentTabFragmentTablet(BottomTabsTablet.ALERTS.getType());
                break;
            case R.id.ll_friends:
                BaseConstants.FROM_NOTIFICATION = false;
                if (!BaseConstants.FROM_NOTIFICATION) {
                    BaseConstants.SELECT_SENT_FRIEND = false;

                }
                BaseConstants.FROM_NOTIFICATION = false;

//                BaseConstants.SELECT_SENT_FRIEND = false;
                setCurrentTabFragmentTablet(BottomTabsTablet.FRIENDS.getType());
                break;
            case R.id.ll_Settings:
                BaseConstants.FROM_NOTIFICATION = false;
                setCurrentTabFragmentTablet(BottomTabsTablet.SETTINGS.getType());
                break;
            case R.id.ll_Logout:
                BaseConstants.FROM_NOTIFICATION = false;
                setCurrentTabFragmentTablet(BottomTabsTablet.LOGOUT.getType());
                break;


            default:
                if (currentFragment instanceof FriendMainFregment) {
                    /*
                    BaseApplication.viewPagerPosition = ((FriendMainFregment) currentFragment).tabLayout.getSelectedTabPosition();
                    if (!BaseConstants.SELECT_SENT_FRIEND) {
                        if (BaseApplication.viewPagerPosition == 0) {
                            BaseApplication.viewPagerPosition = 1;
                        } else {
                            BaseApplication.viewPagerPosition = 0;
                        }
                    }
                    */
                    if (view.getId() == R.id.iv_imgDelete || view.getId() == R.id.iv_imgLocation || view.getId() == R.id.bt_accept || view.getId() == R.id.bt_Locate) {
                        Fragment fragment2 = getVisibleFragment().getChildFragmentManager().getFragments().get(BaseApplication.viewPagerPosition);
                        if (fragment2 instanceof FriendsListFragment) {
                            FriendsListFragment fragment = (FriendsListFragment) fragment2;
                            fragment.onClickEvent(view);
                        } else {
                            FriendsListReceivedFragment fragment = (FriendsListReceivedFragment) fragment2;
                            fragment.onClickEvent(view);
                        }
                    } else {
                        onClick.onClickEvent(view);
                    }

                } else if (currentFragment instanceof AppointmentMainFregment) {
                    BaseApplication.viewPagerPosition = ((AppointmentMainFregment) currentFragment).tabLayout.getSelectedTabPosition();
                    if (view.getId() == R.id.iv_imgDelete || view.getId() == R.id.iv_imgLocation || view.getId() == R.id.bt_accep || view.getId() == R.id.bt_locate) {

                        if (!BaseConstants.SELECT_SENT) {
                            if (BaseApplication.viewPagerPosition == 0) {
                                BaseApplication.viewPagerPosition = 1;
                            } else {
                                BaseApplication.viewPagerPosition = 0;
                            }
                        }

                        Fragment fragment2 = getVisibleFragment().getChildFragmentManager().getFragments().get(BaseApplication.viewPagerPosition);
                        if (fragment2 instanceof AppointmentListFragment) {
                            AppointmentListFragment fragment = (AppointmentListFragment) fragment2;
                            fragment.onClickEvent(view);
                        } else {
                            AppointmentReceivedListFragment fragment = (AppointmentReceivedListFragment) fragment2;
                            fragment.onClickEvent(view);
                        }

                    } else {
                        onClick.onClickEvent(view);
                    }
                } else {
                    onClick.onClickEvent(view);
                }

                break;
        }
    }

    public void loadFragment(Fragment fragment) {
        onClick = (ClickEvent) fragment;
        currentFragment = fragment;
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.container, fragment, fragment.getClass().getName()).commitNow();
//        addToStackEntry(fragment);
    }

    /**
     * add all the nested fragments one by one.
     * Note: This function maintains stack.
     *
     * @param fragment : object of navigationInfo includes header on all the fragments.
     */
    public void addFragment(Fragment fragment) {
        onClick = (ClickEvent) fragment;
        currentFragment = fragment;
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.container, fragment, fragment.getClass().getName());
        fragmentTransaction.addToBackStack(fragment.getClass().getName());
        fragmentTransaction.commit();
        addToStackEntry(fragment);
    }

    public Fragment getVisibleFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        @SuppressLint("RestrictedApi")
        List<Fragment> fragments = fragmentManager.getFragments();
        for (Fragment fragment : fragments) {
            if (fragment != null && fragment.isVisible()) return fragment;
        }
        return null;
    }

    private void addToStackEntry(Fragment fragment) {
        navigationStack.push(new FragmentNavigationInfo(fragment));
    }

    DataObserver dataObserver = new DataObserver() {
        @Override
        public void OnSuccess(RequestCode requestCode, Object object) {
            switch (requestCode) {
                case panic:
                    List<PanicData> panicDataList = (List<PanicData>) object;
                    if (panicDataList != null && panicDataList.size() > 0) {
//                        ToastHelper.displayCustomToast(panicDataList.get(0).getMessage());
                        panicPressed = false;
                        CustomDialog.getInstance().hide();
//                        call911();
                    }
                    startLocationTracking();

                    break;

                case checkpaymentstatus:

                    CustomDialog.getInstance().hide();
                    CustomDialog.getInstance().showAlertWithButtonClick(HomeActivity.this, MessageHelper.getInstance().getAppMessage(getString(R.string.str_call_911)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_app_name)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_ok)), new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            CustomDialog.getInstance().hide();
//                            GPSTracker gpsTracker = new GPSTracker(HomeActivity.this);
                            call911();
//                            PanicData.callPanicPressed(HomeActivity.this, dataObserver, gpsTracker.getLatitude(), gpsTracker.getLongitude());
                        }
                    }, MessageHelper.getInstance().getAppMessage(getString(R.string.str_dismiss)), new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            CustomDialog.getInstance().hide();
                        }
                    }, true);

                    break;
                case logoutCustomer:
                    String deviceToken = PrefHelper.getString(PrefHelper.KEY_DEVICE_TOKEN, "");
//                    PrefHelper.deleteAllPreferences();
                    LoginHelper.getInstance().logoutUser();
                    PrefHelper.setString(PrefHelper.KEY_DEVICE_TOKEN, deviceToken);
                    PrefHelper.setBoolean("isnotification", false);
                    PrefHelper.setInt(BaseConstants.COUNT, 0);
                    CustomDialog.getInstance().hide();
                    BaseConstants.NOTIFICATION_COUNT = 0;
                    ShortcutBadger.applyCount(HomeActivity.this, BaseConstants.NOTIFICATION_COUNT);
                    PrefHelper.deletePreference(BaseConstants.PAYMENT_STATUS);
                    clearNotifications();
                    Intent ina = new Intent(HomeActivity.this, LoginActivity.class);
                    ina.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                    try {
                        /* 02-22-2020 - Removed by TBL. Replaced with BackgroundIntentService
                        if (checkServiceRunning()) {
                            stopService(new Intent(HomeActivity.this, LocationUpdateServiceBackground.class));
                        }
                        */
//                      stopService(new Intent(HomeActivity.this, StatusService.class));

                        stopScheduleService();
                    } catch (Exception ex) {
                    }

                    /* 02-20-2020 Removed by TBL
                    try {
                        Intent intent = new Intent(HomeActivity.this, LocationUpdatesService.class);
                        intent.putExtra(ApiList.KEY_STOP, true);
                        startService(intent);
                    } catch (Exception ex) {
                    }
                    */

                    ((BaseApplication) getApplication()).bus().send(Constants.BUS_ACTION_STOP_STATUS_UPDATE);
                    Hawk.deleteAll();
                    startActivity(ina);
                    HomeActivity.this.finish();
                    break;
                case updatePaymentStatus:
                    CustomDialog.getInstance().hide();

                    String response = (String) object;
                    PrefHelper.setInt(BaseConstants.PAYMENT_STATUS, 1);
                    try {
                        JSONObject mJobjResponse = new JSONObject(response);

                        String status = mJobjResponse.getString(ApiList.KEY_STATUS);
                        String message = mJobjResponse.getString(ApiList.KEY_MESSAGE);

                        CustomDialog.getInstance().showAlertWithButtonClick(HomeActivity.this, message
                                , MessageHelper.getInstance().getAppMessage(getString(R.string.str_app_name))
                                , MessageHelper.getInstance().getAppMessage(getString(R.string.str_ok))
                                ,
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        CustomDialog.getInstance().hide();
                                        PrefHelper.setBoolean("isnotification", false);

//                                        finish();

                                    }
                                }, MessageHelper.getInstance().getAppMessage(getString(R.string.str_dismiss))
                                ,
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        CustomDialog.getInstance().hide();
                                    }
                                }, false
                        );


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    break;

            }
        }

        @Override
        public void OnFailure(RequestCode requestCode, String errorCode, String error) {

            switch (requestCode) {
                case checkpaymentstatus:
//
                    CustomDialog.getInstance().hide();
                    showAlert(HomeActivity.this, errorCode, error, MessageHelper.getInstance().getAppMessage(getString(R.string.str_ok)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_dismiss)), false);

                    break;
                case panic:
                    CustomDialog.getInstance().hide();
                    showAlert(HomeActivity.this, errorCode, error, MessageHelper.getInstance().getAppMessage(getString(R.string.str_ok)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_dismiss)), false);
                    break;
            }
        }

        @Override
        public void onOtherStatus(RequestCode requestCode, Object object) {

        }

        @Override
        public void onRetryRequest(RequestCode requestCode) {

        }
    };

    private boolean appInstalledOrNot(String uri) {
        PackageManager pm = getPackageManager();
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
        }

        return false;
    }

    public void showAlert(final Context context, String msg, String header, String positiveButton, String negativeButton, boolean isShowNegative) {

        dialog = new Dialog(context, R.style.DialogTheme);
        @SuppressLint("InflateParams") final View view = LayoutInflater.from(context).inflate(R.layout.dialog_alert_new, null);
        TextView txtHeader = view.findViewById(R.id.txtHeader);
        TextView txtMessage = view.findViewById(R.id.tv_txtMessage);
        TextView txtNegative = view.findViewById(R.id.tv_txtNegative);
        Button btn_positive = view.findViewById(R.id.btn_positive);
        txtHeader.setText(header);
        txtMessage.setText(msg);
        btn_positive.setText(positiveButton);
        txtNegative.setText(negativeButton);
        if (isShowNegative) {
            txtNegative.setVisibility(View.VISIBLE);
        }
        btn_positive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        txtNegative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(view);
        try {
            if (dialog != null) {
                if (!dialog.isShowing()) {
                    dialog.show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void setupNotificationCount() {
        try {
            if (BaseConstants.NOTIFICATION_COUNT == 0) {
                BaseConstants.NOTIFICATION_COUNT = PrefHelper.getInt(BaseConstants.COUNT, 0);
            }
            if (!Util.isTabletDevice(HomeActivity.this)) {
                Button btnNotificationCount = (Button) tabLayout.getTabAt(1).getCustomView().findViewById(R.id.bt_NotificationCount);
                if (BaseConstants.NOTIFICATION_COUNT > 0) {
                    btnNotificationCount.setVisibility(View.VISIBLE);
                    btnNotificationCount.setText(String.valueOf(BaseConstants.NOTIFICATION_COUNT));
                } else {
                    btnNotificationCount.setVisibility(View.GONE);
                }
            } else {
                if (BaseConstants.NOTIFICATION_COUNT > 0) {
                    tabactivitybinding.btNotificationCount.setVisibility(View.VISIBLE);
                    tabactivitybinding.btNotificationCount.setText(String.valueOf(BaseConstants.NOTIFICATION_COUNT));
                } else {
                    tabactivitybinding.btNotificationCount.setVisibility(View.GONE);
                }

            }
            ShortcutBadger.applyCount(HomeActivity.this, BaseConstants.NOTIFICATION_COUNT);
        } catch (Exception ecx) {
        }
    }


    public void buyClick(View view) {
        try {
            mHelper.launchSubscriptionPurchaseFlow(HomeActivity.this, BaseConstants.ITEM_SKU_PREMIUM, 10001,
                    mPurchaseFinishedListener, "mypurchasetoken1");
        } catch (IabHelper.IabAsyncInProgressException e) {
            e.printStackTrace();
        }
    }

    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener
            = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result,
                                          Purchase purchase) {
            if (result.isFailure()) {
                // Handle errorChange
                ToastHelper.displayCustomToast("Failed to subscribe ");
//                PrefHelper.setInt(BaseConstants.PAYMENT_STATUS, 1);
                return;
            } else if (purchase.getSku().equals(BaseConstants.ITEM_SKU_PREMIUM)) {
//                PrefHelper.setInt(BaseConstants.PAYMENT_STATUS, 1);
                consumeItem();
//                PlanData.updatePaymentStatus(HomeActivity.this,planList.get(selectedPosition),dataObserver);
                PlanData planData = new PlanData();
                planData.setPaymentAmount("19.99");
                PlanData.updatePaymentStatus(HomeActivity.this, planData, dataObserver);


            }
        }
    };

    public void consumeItem() {
        try {
            mHelper.queryInventoryAsync(mReceivedInventoryListener);
        } catch (IabHelper.IabAsyncInProgressException e) {
            e.printStackTrace();
        }
    }

    IabHelper.QueryInventoryFinishedListener mReceivedInventoryListener
            = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result,
                                             Inventory inventory) {

            if (result.isFailure()) {
                // Handle failure
                ToastHelper.displayCustomToast("Failed to subscribe");
            } else {
                //                    mHelper.consumeAsync(inventory.getPurchase(BaseConstants.ITEM_SKU_PREMIUM),
//                            mConsumeFinishedListener);
                PlanData planData = new PlanData();
                planData.setPaymentAmount("19.99");
                PlanData.updatePaymentStatus(HomeActivity.this, planData, dataObserver);
            }
        }
    };

    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener =
            new IabHelper.OnConsumeFinishedListener() {
                public void onConsumeFinished(Purchase purchase,
                                              IabResult result) {

                    if (result.isSuccess()) {
//
                        PlanData planData = new PlanData();
                        planData.setPaymentAmount("19.99");
                        PlanData.updatePaymentStatus(HomeActivity.this, planData, dataObserver);
                    } else {
                        ToastHelper.displayCustomToast(MessageHelper.getInstance().getAppMessage(getString(R.string.str_fail_subscribe)));
                        //handle error
                    }
                }
            };


    //energySavingSetup
    //@RequiresApi(23)
    private void energySavingSetup() {

        //same date
        long prev_time = Util.getCurrTimeLogDB(this);
        if (prev_time != Util.getToday()) {
            Util.saveCurrTimeLogDB(this, Util.getToday());

            DBHelper dbHelper = new DBHelper(this);
            dbHelper.deleteAll();
        }


        if (Util.isBatterySavingIgnored(this)) {
            return;
        }

        Util.requestBatterySavingIgnore(HomeActivity.this);

        /*
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setCancelable(false)
                .setTitle("Energy saving")
                .setMessage("Starting with Android 6.0, you need to add this application to the energy saving whitelist.")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Util.requestBatterySavingIgnore(HomeActivity.this);
                    }
                })
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ((android.app.AlertDialog) dialog).setMessage(Html.fromHtml("<font color='#FF7F27'>Must add this application to the energy saving whitelist. Press ok button.</font>"));
                        dialog.dismiss();
                    }
                });
        dialog.setOnCancelListener(
                new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        ((android.app.AlertDialog) dialog).setMessage(Html.fromHtml("<font color='#FF7F27'>Please do not cancel. Press ok button.</font>"));
                    }
                }
        );
        dialog.show();
        */
    }



    //<editor-fold desc="NEW LOCATION IMPLEMENTATION">

    private BackgroundIntentService backgroundIntentService;
    //</editor-fold>
}
