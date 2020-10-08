package com.pinpoint.appointment.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;

import com.android.volley.Request;
import com.pinpoint.appointment.BaseApplication;
import com.pinpoint.appointment.BaseConstants;
import com.pinpoint.appointment.activities.HomeActivity;
import com.pinpoint.appointment.api.ApiList;
import com.pinpoint.appointment.api.RequestCode;
import com.pinpoint.appointment.api.RequestListener;
import com.pinpoint.appointment.api.RestClient;
import com.pinpoint.appointment.helper.PrefHelper;
import com.pinpoint.appointment.helper.ToastHelper;
import com.pinpoint.appointment.interfaces.DataObserver;
//import com.pinpoint.appointment.location.LocationUpdateServiceBackground;
import com.pinpoint.appointment.models.LeadDetails;
import com.pinpoint.appointment.service.BackgroundIntentService;
import com.pinpoint.appointment.utils.Constants;
import com.pinpoint.appointment.utils.Debug;
import com.pinpoint.appointment.utils.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

import static android.content.Context.ACTIVITY_SERVICE;

public class AirplaneModeReceiver extends BroadcastReceiver {
    Context mContext;

    public void onReceive(Context context, Intent intent) {
        mContext = context;
        if (isAirplaneModeOn(context)) {
//            ToastHelper.displayCustomToast("Offline");
            SimpleDateFormat sdf = new SimpleDateFormat(BaseConstants.PICKDATETIMEFORMAT);
            String datetime = sdf.format(new Date());
            PrefHelper.setString(PrefHelper.KEY_SWITCHOFFTIME, datetime);
        } else {
            //ToastHelper.displayCustomToast("Online");
            final android.os.Handler handler = new android.os.Handler();
            handler.postDelayed(() -> {
                SimpleDateFormat sdf = new SimpleDateFormat(BaseConstants.PICKDATETIMEFORMAT);
                String datetime = sdf.format(new Date());

                setUserOnLineStatus(mContext, datetime, dataObserver, false);

                if (!Util.isTabletDevice(mContext)) {
                    BackgroundIntentService.Companion.startLocationTracking(mContext.getApplicationContext(), null, null);
                    new Handler().postDelayed(() -> {
                        ((BaseApplication) context.getApplicationContext()).bus().send(Constants.BUS_ACTION_START_LOCATION_TRACKING);
                    }, 1000);
                }
                /*
                try {
                    if (!Util.isTabletDevice(mContext)) {
                        if (!checkServiceRunning()) {

                            startServiceIntent();
                            Debug.trace("Location Service Restarted");

                        } else {
                            Debug.trace("Location Service Running");
                        }
                    }
                } catch (Exception ex) {

                }
                */
                //Do something after 100ms
            }, 1000 * 60);


        }


//        Intent startActivityIntent = new Intent(context, MainActivity.class);
//        startActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        context.startActivity(startActivityIntent);
    }

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

    /* 02-22-2020 - Removed by TBL. Replaced with BackgroundIntentService
    private void startServiceIntent() {
        try {
            Intent ina = new Intent(mContext, LocationUpdateServiceBackground.class);
            ina.putExtra("updateTimeInterval", 2000L);//in milliseconds
            ina.putExtra("updateDistance", 1f);//in meters
            ina.putExtra("isDistanceRequired", false);//To enable minimum distance for location check
            mContext.startService(ina);
        } catch (Exception ex) {
            LeadDetails.setMessage(mContext, " From : AirplaneModeReceiver startserviceintent Exception:" + ex.toString() + new Date());
        }
    }
    */

    public static boolean isAirplaneModeOn(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return Settings.System.getInt(context.getContentResolver(),
                    Settings.System.AIRPLANE_MODE_ON, 0) != 0;
        } else {
            return Settings.Global.getInt(context.getContentResolver(),
                    Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
        }
    }

    public static void setUserOnLineStatus(final Context activity, String datetime, final DataObserver dataObserver, boolean isDialogRequired) {
        try {

//            {"userId":787,"switchofftime":"2018-06-19 20:10:10","switchontime":"2018-06-19 20:10:10","description":"Reason for offline"}
            JSONObject param = new JSONObject();
            param.put("userId", Integer.parseInt(com.pinpoint.appointment.models.LoginHelper.getInstance().getUserID()));
            param.put("switchofftime", PrefHelper.getString(PrefHelper.KEY_SWITCHOFFTIME, ""));
            param.put("switchontime", datetime);
            param.put("description", "In Airplane Mode");
//

            RestClient.getInstance().postForService(activity, Request.Method.POST, ApiList.APIs.setofflinereason.getUrl(), param,
                    new RequestListener() {
                        @Override
                        public void onComplete(RequestCode requestCode, Object object) {
                            PrefHelper.setString(PrefHelper.KEY_SWITCHOFFTIME, "");
//                            LeadDetails.setMessage(activity, " From : Airplanemode Receiver After status update Method"  + new Date());
                            dataObserver.OnSuccess(requestCode, object);
                        }

                        @Override
                        public void onException(String error, String status, RequestCode requestCode) {
                            dataObserver.OnFailure(requestCode, status, error);
                        }

                        @Override
                        public void onOtherStatus(RequestCode requestCode, Object object) {

                        }
                    }, RequestCode.setofflinereason, isDialogRequired);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    DataObserver dataObserver = new DataObserver() {
        @Override
        public void OnSuccess(RequestCode requestCode, Object object) {

        }

        @Override
        public void OnFailure(RequestCode requestCode, String errorCode, String error) {

        }

        @Override
        public void onOtherStatus(RequestCode requestCode, Object object) {

        }

        @Override
        public void onRetryRequest(RequestCode requestCode) {

        }
    };
}
