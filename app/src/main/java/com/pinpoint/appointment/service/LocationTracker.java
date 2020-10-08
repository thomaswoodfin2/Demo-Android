package com.pinpoint.appointment.service;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import androidx.core.app.ActivityCompat;
import android.util.Log;

import com.android.volley.Request;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.pinpoint.appointment.api.ApiList;
import com.pinpoint.appointment.api.RequestCode;
import com.pinpoint.appointment.api.RequestListener;
import com.pinpoint.appointment.api.RestClient;
import com.pinpoint.appointment.interfaces.DataObserver;
import com.pinpoint.appointment.models.LoginHelper;
import com.pinpoint.appointment.utils.Util;

import org.json.JSONObject;


public class LocationTracker extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, SensorEventListener {

    // LogCat tag
    private static final String TAG = "LocationTracker";

    private static final String PACKAGE_NAME =
            "com.pinpoint.appointment";
    public static final String ACTION_BROADCAST = PACKAGE_NAME + ".broadcast";
    public static final String ACTION_BROADCAST_LOCATION = PACKAGE_NAME + ".broadcast1";
    public static final String EXTRA_LOCATION = PACKAGE_NAME + ".location";
    private static String LOG_TAG = "BoundService";

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    private static int UPDATE_INTERVAL = 1000; // 5 sec
    private static int FATEST_INTERVAL = 500; // 0.5 sec
    private static int DISPLACEMENT = 5; // 2 meters

    private Context mContext;
    //private SharedPreferences sp;

    // Location updates intervals in sec
    private Location mLastLocation;

    // Google client to interact with Google API

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private double latitude, longitude, altitude;
    //private boolean firstLocation = false;

    private float pressure, heading, speed;

    private SensorManager sensorManager;
    private Sensor pressureSensor, accelerometerSensor, magnetometerSensor;

    private float[] mLastAccelerometer = new float[3];
    private float[] mLastMagnetometer = new float[3];

    private float[] mR = new float[9];
    private float[] mOrientation = new float[3];

    private int userId;
    private boolean isTablet = false;

    public LocationTracker(Context ctx) {
        mContext = ctx;
        userId = Integer.parseInt(LoginHelper.getInstance().getUserID());

        //sp = BaseApplication.getInstance().getSharedPreferences(APP_PREF, ctx.PRIVATE_MODE);
        //sp = context.getSharedPreferences(Utility.SHARED_PREFS, MODE_PRIVATE);

        // First we need to check availability of play services
        if (checkPlayServices()) {

            buildGoogleApiClient();
            createLocationRequest();

            if (mGoogleApiClient != null)
                mGoogleApiClient.connect();

            sensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);

            accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            magnetometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            pressureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);

            sensorManager.registerListener(this, pressureSensor, SensorManager.SENSOR_DELAY_NORMAL);
            sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
            sensorManager.registerListener(this, magnetometerSensor, SensorManager.SENSOR_DELAY_NORMAL);

            Util.showLog(TAG, "[LocationTracker ] .");
        }
    }


    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    /**
     * Creating google api client object
     */
    protected synchronized void buildGoogleApiClient() {

        mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    /**
     * Method to verify google play services on the device
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(mContext);

        if (resultCode != ConnectionResult.SUCCESS) {

            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode))
                Util.showLog(TAG, "Something went wrong with error code :" + resultCode);
            else
                Util.showLog(TAG, "This device is not supported.");

            // stopLocationUpdates();
            return false;
        }

        return true;
    }


    /**
     * Creating location request object
     */
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT); // 10 meters
    }

    /**
     * Starting the location updates
     */
    public void startLocationUpdates() {
        Util.showLog(TAG, "Starting Location Updates");

        if (ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);

    }

    /**
     * Stopping location updates
     */
    public void stopLocationUpdates() {

        Util.showLog(TAG, "Stopping Location Updates");

        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);

            if (mGoogleApiClient.isConnected())
                mGoogleApiClient.disconnect();

        }

        stopSelf();
    }


    /**
     * Function to get latitude
     */
    public double getLatitude() {
        // return latitude
        return latitude;
    }

    /**
     * Function to get longitude
     */
    public double getLongitude() {
        // return longitude
        return longitude;
    }

    /**
     * Function to get Altitude
     */
    public double getAltitude() {
        // return altitude
        return altitude;
    }


    /**
     * Function to get Heading
     */
    public double getHeading() {

        return heading;
    }


    @Override
    public void onConnected(Bundle bundle) {
        Util.showLog(TAG, "Google API Connected");
        // Once connected with google api, get the location
        updateLocation();
        startLocationUpdates();
    }

    @Override
    public void onLocationChanged(Location location) {
        // Assign the new location
        mLastLocation = location;
        updateLocation();

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Method to display the location on UI
     */
    private void updateLocation() {
        if (ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi
                .getLastLocation(mGoogleApiClient);

        if (mLastLocation != null) {

//            if (firstLocation) {
//                sp.edit().putString("last_latitude", String.valueOf(mLastLocation.getLatitude())).commit();
//                sp.edit().putString("last_longitude", String.valueOf(mLastLocation.getLongitude())).commit();
//                sp.edit().putLong("last_location_time", System.currentTimeMillis()).commit();
//
//                firstLocation = false;
//            }

            latitude = mLastLocation.getLatitude();
            longitude = mLastLocation.getLongitude();

            heading = mLastLocation.getBearing();
            altitude = mLastLocation.getAltitude();

            Util.showLog(TAG, "Latitude : " + latitude);
            Util.showLog(TAG, "Longitude : " + longitude);

            Util.showLog(TAG, "Heading :" + heading);
            Util.showLog(TAG, "Altitude :" + altitude);

            //upload gps service
            updateDriverLocationAPI(mContext, latitude+"", longitude+"", dataObserver);


            //update for panic button
            try {
                Intent broadCastIntent = new Intent();
                broadCastIntent.setAction(ACTION_BROADCAST_LOCATION);
                broadCastIntent.putExtra(EXTRA_LOCATION, mLastLocation);
                sendBroadcast(broadCastIntent);
            }
            catch (Exception ex) {}

        } else {
            Util.showLog(TAG, "Couldn't get the location. Make sure location is enabled on the device");
        }
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == pressureSensor) {
            pressure = event.values[0];
            altitude = SensorManager.getAltitude(SensorManager.PRESSURE_STANDARD_ATMOSPHERE, pressure);
        }

        if (event.sensor == accelerometerSensor) {
            mLastAccelerometer = event.values;

        } else if (event.sensor == magnetometerSensor) {
            mLastMagnetometer = event.values;
        }
        if (mLastAccelerometer != null && mLastMagnetometer != null) {
            SensorManager.getRotationMatrix(mR, null, mLastAccelerometer, mLastMagnetometer);
            SensorManager.getOrientation(mR, mOrientation);
            float azimuthInRadians = mOrientation[0];

            float azimuthInDegrees = (float) (Math.toDegrees(azimuthInRadians) + 360) % 360;

            heading = azimuthInDegrees;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        sensorManager.unregisterListener(this, magnetometerSensor);
        sensorManager.unregisterListener(this, accelerometerSensor);
        sensorManager.unregisterListener(this, pressureSensor);

        Util.showLog(TAG, "Location Tracker Stopped");
    }

    /**
     * Function to get Speed
     */

    public double getSpeed() {
        /*
        double previousLatitude = Double.parseDouble(sp.getString("last_latitude", "0.0"));
        double previousLongitude = Double.parseDouble(sp.getString("last_longitude", "0.0"));

        Util.showLog(TAG, "Old Latitude :" + previousLatitude);
        Util.showLog(TAG, "Old Longitude :" + previousLongitude);

        Util.showLog(TAG, "New Latitude :" + latitude);
        Util.showLog(TAG, "New Longitude :" + longitude);

        Location sourceLocation = new Location("A");
        sourceLocation.setLatitude(previousLatitude);
        sourceLocation.setLongitude(previousLongitude);

        Location destinationLocation = new Location("B");
        destinationLocation.setLatitude(latitude);
        destinationLocation.setLongitude(longitude);


        double distance = sourceLocation.distanceTo(destinationLocation);

        Util.showLog(TAG, "Distance =" + distance);

        long differenceInTime = (System.currentTimeMillis() - sp.getLong("last_location_time", 0)) / 1000;

        double mps = distance / differenceInTime;
        double kph = (mps * 3600) / 1000;
        return kph;
        */

        return 0;
    }




    //upload gps data to server

    public void updateDriverLocationAPI(final Context context, final String latitude, final String longitude, final DataObserver dataObserver) {
        try {
            JSONObject param = new JSONObject();

            param.put(ApiList.KEY_USERID, userId);
            param.put(ApiList.KEY_LATITUDE, latitude);
            param.put(ApiList.KEY_LONGITUDE, longitude);
            param.put(ApiList.KEY_STATUS, "1");

            RestClient.getInstance().postForService(context, Request.Method.POST, ApiList.APIs.addfriendlocation.getUrl(), param, new RequestListener() {
                @Override
                public void onComplete(RequestCode requestCode, Object object) {

                    dataObserver.OnSuccess(requestCode, object);

                }

                @Override
                public void onException(String statusCode, String error, RequestCode requestCode) {
                    dataObserver.OnFailure(requestCode, statusCode, error);
                }

                @Override
                public void onOtherStatus(RequestCode requestCode, Object object) {
                    dataObserver.onOtherStatus(requestCode, object);
                }


            }, RequestCode.addfriendlocation , false);
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

}
