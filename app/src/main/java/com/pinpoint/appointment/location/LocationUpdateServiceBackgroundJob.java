package com.pinpoint.appointment.location;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.JobIntentService;
import android.util.Log;

import com.android.volley.Request;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.pinpoint.appointment.api.ApiList;
import com.pinpoint.appointment.api.RequestCode;
import com.pinpoint.appointment.api.RequestListener;
import com.pinpoint.appointment.api.RestClient;
import com.pinpoint.appointment.interfaces.DataObserver;
import com.pinpoint.appointment.models.LoginHelper;
import com.pinpoint.appointment.utils.Debug;

import org.json.JSONObject;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class LocationUpdateServiceBackgroundJob extends JobIntentService implements DataObserver {
    private static final String TAG = LocationUpdateServiceBackgroundJob.class.getSimpleName();
    private static  long UPDATE_INTERVAL_IN_MILLISECONDS = 0;
    private static  float UPDATE_INTERVAL_IN_DISTANCE = 2;//5; //minimum distance for location update in meter
    private static  boolean IS_DISTANCE_REQUIRED = true; //
    float temp=0;
    public static LatLng lastLatLOng=null;
    private static  int userID=0;
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        try
        {
//            LeadDetails.setMessage(mContext, " From : LocationUpdateServiceBackgroundJob onTaskRemoved Method" + rootIntent.getClass() + new Date());
        }catch (Exception ex)
        {}
        super.onTaskRemoved(rootIntent);
    }

    /**
     * The fastest rate for active location updates. Updates will never be more frequent
     * than this value.
     */
    private static  long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = 2000;

    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;
    private Location mLocation;
    Context mContext;
//    File path;
//    File file;
    String locationContent="";
    Task<LocationSettingsResponse> task;

    private static final String PACKAGE_NAME =
            "com.pinpoint.appointment";
    public static final String ACTION_BROADCAST = PACKAGE_NAME + ".broadcast";
    public static final String ACTION_BROADCAST_LOCATION = PACKAGE_NAME + ".broadcast1";
    public static final String EXTRA_LOCATION = PACKAGE_NAME + ".location";
    private static String LOG_TAG = "BoundService";
    static final int JOB_ID = 1000;

//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    @Override
//    public boolean onStartJob(JobParameters jobParameters) {
//        if(jobParameters!=null&&!jobParameters.getExtras().isEmpty())
//        {
//            if(jobParameters.getExtras().getInt("max", 6)!=0) {
//                userID = jobParameters.getExtras().getInt(ApiList.KEY_USERID, Integer.parseInt(LoginHelper.getInstance().getUserID()));
//                Debug.trace("USERID : FROM INTENT"+ userID);
//            }
//            else {
//                userID = Integer.parseInt(LoginHelper.getInstance().getUserID());
//                Debug.trace("USERID : FROM PREFERENCE"+userID);
//            }
//            LeadDetails.setMessage(mContext, " From : LocationUpdateServiceBackground onStartCommand"  + new Date());        }
//        else
//        {
//            Debug.trace("USERID : FROM ELSE" +userID);
//            userID = Integer.parseInt(LoginHelper.getInstance().getUserID());
//            Debug.trace("USERID : FROM ELSE" +userID);
//        }
//        startLocationUpdates();
//        return true;
//    }

//    @Override
//    public boolean onStopJob(JobParameters jobParameters) {
//        return false;
//    }

//    @Nullable
//    @Override
//    public IBinder onBind(Intent intent) {
//        return null;
//    }
    public static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, LocationUpdateServiceBackgroundJob.class, JOB_ID, work);
    }
    @Override
    public void onCreate() {
        mContext = this;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try
        {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        } catch (Exception unlikely)
        {
            Log.e(TAG, "Lost location permission. Could not remove updates. " + unlikely);
        }
    }


    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        if(intent!=null&&!intent.getExtras().isEmpty())
        {
            if(intent.hasExtra(ApiList.KEY_USERID)) {
                userID = intent.getIntExtra(ApiList.KEY_USERID, Integer.parseInt(LoginHelper.getInstance().getUserID()));
                Debug.trace("USERID : FROM INTENT"+ userID);
            }
            else {
                userID = Integer.parseInt(LoginHelper.getInstance().getUserID());
                Debug.trace("USERID : FROM PREFERENCE"+userID);
            }
//            LeadDetails.setMessage(mContext, " From : LocationUpdateServiceBackground onStartCommand"  + new Date());
    }
        else
        {
            Debug.trace("USERID : FROM ELSE" +userID);
            userID = Integer.parseInt(LoginHelper.getInstance().getUserID());
            Debug.trace("USERID : FROM ELSE" +userID);
        }
        startLocationUpdates();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if(intent!=null&&!intent.getExtras().isEmpty())
        {
            if(intent.hasExtra(ApiList.KEY_USERID)) {
                userID = intent.getIntExtra(ApiList.KEY_USERID, Integer.parseInt(LoginHelper.getInstance().getUserID()));
                Debug.trace("USERID : FROM INTENT"+ userID);
            }
            else {
                userID = Integer.parseInt(LoginHelper.getInstance().getUserID());
                Debug.trace("USERID : FROM PREFERENCE"+userID);
            }
//            LeadDetails.setMessage(mContext, " From : LocationUpdateServiceBackgroundJob onStartCommand"  + new Date());
        }
        else
            {
                Debug.trace("USERID : FROM ELSE" +userID);
                userID = Integer.parseInt(LoginHelper.getInstance().getUserID());
                Debug.trace("USERID : FROM ELSE" +userID);
            }
        startLocationUpdates();
        return START_STICKY;
    }


    protected void startLocationUpdates() {

        // Create the location request to start receiving updates
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setSmallestDisplacement(UPDATE_INTERVAL_IN_DISTANCE);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);


        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mContext);
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                onLocationChanged(locationResult.getLastLocation());
            }
        };

        try
        {


            mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                    mLocationCallback, null);

        } catch (SecurityException unlikely) {
//          Utils.setRequestingLocationUpdates(this, false);

            Log.e(TAG, "Lost location permission. Could not request updates. " + unlikely);
        }

    }

    public void onLocationChanged(Location location) {

        if(location!=null)
        {
            Debug.trace("TAG:location"+location.getLatitude()+" , "+location.getLongitude());
            temp = (float) (temp+0.00010);

//
//            double lastlat= Double.parseDouble(PrefHelper.getString(PrefHelper.KEY_LAST_LAT,"0"));
//            double lastlong;
//            String lasttime;
//            lastLatLOng=new LatLng(location.getLatitude(),location.getLongitude());
//            if(lastlat==0)
//            {
//                PrefHelper.setString(PrefHelper.KEY_LAST_LAT,""+location.getLatitude());
//                PrefHelper.setString(PrefHelper.KEY_LAST_LONG,""+location.getLongitude());
//            }
//            else
//            {
//                lastlat=Double.parseDouble(PrefHelper.getString(PrefHelper.KEY_LAST_LAT,""));
//                lastlong=Double.parseDouble(PrefHelper.getString(PrefHelper.KEY_LAST_LONG,""));
//
//                Location startPoint=new Location("locationA");
//                startPoint.setLatitude(lastlat);
//                startPoint.setLongitude(lastlong);
//                temp+=0.001;
//                Location endPoint=new Location("locationA");
//                endPoint.setLatitude(location.getLatitude());
//                endPoint.setLongitude(location.getLongitude());
//                double distance=startPoint.distanceTo(endPoint);
                try
                {

//
//                    if(distance>=0)
//                    {
//                        Intent broadCastIntent = new Intent();
//                        broadCastIntent.setAction(ACTION_BROADCAST_LOCATION);
//                        broadCastIntent.putExtra(EXTRA_LOCATION, location);
//                        sendBroadcast(broadCastIntent);
//
//                        PrefHelper.setString(PrefHelper.KEY_LAST_LAT,""+location.getLatitude());
//                        PrefHelper.setString(PrefHelper.KEY_LAST_LONG,""+location.getLongitude());
//

                        Debug.trace("TAG:location"+location.getLatitude()+" , "+location.getLongitude());
                        updateDriverLocationAPI(mContext, String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()), this);
//                      writeToFile();
//                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
//            }

        }

    }


    @SuppressLint("MissingPermission")
    public void getLastLocation() {
        // Get last known recent location using new Google Play Services SDK (v11+)
        FusedLocationProviderClient locationClient = LocationServices.getFusedLocationProviderClient(this);

        locationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // GPS location can be null if GPS is switched off
                        if (location != null) {
                            onLocationChanged(location);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("MapDemoActivity", "Error trying to get last GPS location");
                        e.printStackTrace();
                    }
                });
    }

    public void removeLocationUpdates() {
        Log.i(TAG, "Removing location updates");
        try {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
//            Utils.setRequestingLocationUpdates(this, false);
            stopSelf();
        } catch (SecurityException unlikely) {
//            Utils.setRequestingLocationUpdates(this, true);
            Log.e(TAG, "Lost location permission. Could not remove updates. " + unlikely);
        }
    }

    public void updateDriverLocationAPI(final Context activity, final String latitude, final String longitude, final DataObserver dataObserver) {
        try {
            JSONObject param = new JSONObject();
            param.put(ApiList.KEY_USERID,userID);
            param.put(ApiList.KEY_LATITUDE, latitude);
            param.put(ApiList.KEY_LONGITUDE, longitude);

            RestClient.getInstance().postForService(activity, Request.Method.POST, ApiList.APIs.addfriendlocation.getUrl(), param, new RequestListener() {
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

    @Override
    public void OnSuccess(RequestCode requestCode, Object object) {
        Debug.trace("Location Updated");
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
}

