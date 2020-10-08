package com.pinpoint.appointment.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.pinpoint.appointment.helper.DBHelper;
import com.pinpoint.appointment.models.LoginHelper;
import com.pinpoint.appointment.utils.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SensorService extends Service {

    public long Seconds = 5000;
    private Handler handler = new Handler();
    private LocationTracker locationTracker;

    private SharedPreferences sp;
    private String TAG = "Sensor Service";

    private int userId;

    Runnable runnable = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub

            if (Util.checkConnectivity(SensorService.this)) {

//                postSensorValues(SensorService.this, locationTracker.getLatitude(),
//                        locationTracker.getLongitude(), locationTracker.getAltitude(),
//                        locationTracker.getHeading());


            } else
                Util.showLog(TAG, "No Internet Connection");

            // gpsTracker.stopUsingGPS();

            locationTracker.stopLocationUpdates();

            handler.removeCallbacks(runnable);

        }
    };
    private Context context;
    private float speed;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();

        context = this;

        Util.showLog(TAG, "Sensor Service Started");

//        sp = getSharedPreferences(Util.SHARED_PREFS, 0);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub

        userId = Integer.parseInt(LoginHelper.getInstance().getUserID());
        Util.showLog(TAG, "Service Started [User Id]" + userId);

//        DBHelper dbHelper = new DBHelper(this);
//        dbHelper.insertLogItem("Service Started [User Id]" + userId);

        //startGPSService();

        /*
        handler.postDelayed(runnable, Seconds);
        if (!Util.checkGPS(this)) {
            if (!Util.isNotificationVisible(this)) {
                Util.displayGPSNotification(this);
                stopSelf();
            }
        } else {
            if (Util.isNotificationVisible(context))
                Util.cancelNotification(context);
        }
        */

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();

        if (handler != null)
            handler.removeCallbacks(runnable);

        Util.showLog(TAG, "Sensor Service Stopped");
    }


    //locationTracker
    private void startGPSService()
    {
        stopGPSService();

        locationTracker = new LocationTracker(this);
    }

    //locationTracker
    private void stopGPSService()
    {
        if(locationTracker != null){
            locationTracker.stopLocationUpdates();
            locationTracker = null;
        }
    }




    /*
    public void postSensorValues(Context mContext, Double latitude, Double longitude, Double altitude, Double headings) {

        Log.d(TAG, "Latitude :" + latitude + "");
        Log.d(TAG, "Longitude :" + longitude + "");
        Log.d(TAG, "Altitude :" + altitude + "");
        Log.d(TAG, "Heading :" + headings + "");

        double speed;

        if ((Double.compare(latitude, 0.0) == 0)
                && (Double.compare(longitude, 0.0) == 0))
            stopSelf();
        else {

            speed = locationTracker.getSpeed();

            if (speed == Double.POSITIVE_INFINITY || speed == Double.NaN)
                speed = 0.0;

            Util.showLog(TAG, "Speed :" + speed);

            final String parameters = getJson(sp.getString("user_id", "NA"),
                    sp.getString("pass", "NA"),
                    sp.getString("mobile", "NA"), String.valueOf(latitude),
                    String.valueOf(longitude), String.valueOf(altitude),
                    String.valueOf(speed), String.format("%.1f", headings),
                    Util.getIMEI(mContext),
                    Util.getMyBatteryChargeLevel(mContext));

            Log.d(TAG, parameters);


            StringRequest sensorRequest = new StringRequest(com.android.volley.Request.Method.POST,
                    Util.webservice_url + "/AASensorData",
                    new com.android.volley.Response.Listener<String>() {

                        @Override
                        public void onResponse(String response) {
                            // TODO Auto-generated method stub

                            Util.showLog(TAG, response);

                            String sensorResponse = ParseJsonResponse(response);

                            if (sensorResponse != null) {

                                String result[] = sensorResponse.split("/");

                                if (Boolean.parseBoolean(result[0])) {

                                    Util.showLog(TAG, result[1]);

                                } else
                                    Util.showLog(TAG, result[1]);

                            }

                            stopSelf();
                        }

                    }, new com.android.volley.Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError vError) {
                    // TODO Auto-generated method stub

                    if (vError instanceof TimeoutError
                            || vError instanceof NoConnectionError)
                        Log.d(TAG, "Request Timeout,Please try again.");
                    else if (vError instanceof NetworkError)
                        Log.d(TAG, "Network error, Please try again");
                    else
                        Log.d(TAG, vError.getLocalizedMessage());

                    stopSelf();
                }

            }) {

                @Override
                public Map<String, String> getHeaders() {

                    Map<String, String> headers = new HashMap<String, String>();
                    headers.put("Accept", "application/json");
                    return headers;
                }

                @Override
                public String getBodyContentType() {
                    // TODO Auto-generated method stub

                    return "application/x-www-form-urlencoded;charset=UTF-8";
                }

                public byte[] getBody() {

                    return parameters.getBytes();

                }

            };


            sensorRequest.setShouldCache(false);
            VolleySingleton.getInstance().addToRequestQueue(sensorRequest, TAG);

        }

    }

    private String ParseJsonResponse(String res) {

        String result = null;

        try {

            JSONObject jObject = new JSONObject(res);

            result = jObject.getString("Result") + "/"
                    + jObject.getString("Message");

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return result;

    }

    private String getJson(String userId, String password, String mobile,
                           String lat, String lon, String alt, String speedy, String head,
                           String imei, String batteryLevel) {

        JSONObject json = null;

        try {
            json = new JSONObject();
            json.put("UserId", userId);
            json.put("cellPhone", mobile);
            json.put("password", password);
            json.put("IMEI", imei);
            json.put("batteryLevel", batteryLevel);
            json.put("uptime", Util.convertSecondsToHHMMSS(SystemClock.uptimeMillis()));
            json.put("PhoneName", Build.BRAND);
            json.put("ModelNumber", Build.MODEL);

            JSONObject newObject;

            JSONArray jArray = new JSONArray();

            for (int i = 0; i < 1; i++) {

                newObject = new JSONObject();

                newObject.put("latitude", lat);
                newObject.put("longitude", lon);
                newObject.put("altitude", alt);
                newObject.put("speed", speedy);
                newObject.put("heading", head);

                jArray.put(newObject);

            }

            json.put("GPS", jArray);

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Log.d("Json Request", json.toString());

        return json.toString();

    }
    */
}
