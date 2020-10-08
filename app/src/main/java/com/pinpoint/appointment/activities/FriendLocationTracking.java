package com.pinpoint.appointment.activities;

import android.Manifest;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;

import androidx.databinding.DataBindingUtil;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

import com.google.android.material.snackbar.Snackbar;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.cardview.widget.CardView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

//import com.crashlytics.android.Crashlytics;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.pinpoint.appointment.BaseActivity;
import com.pinpoint.appointment.BaseApplication;
import com.pinpoint.appointment.BaseConstants;
import com.pinpoint.appointment.R;
import com.pinpoint.appointment.api.ApiList;
import com.pinpoint.appointment.api.RequestCode;
import com.pinpoint.appointment.customviews.CustomDialog;
import com.pinpoint.appointment.databinding.ActivityLocationtrackingBinding;
import com.pinpoint.appointment.helper.MessageHelper;
import com.pinpoint.appointment.interfaces.ClickEvent;
import com.pinpoint.appointment.interfaces.DataObserver;
import com.pinpoint.appointment.location.LocationUpdateServiceBackground;
import com.pinpoint.appointment.models.AppointmentData;
import com.pinpoint.appointment.models.FriendDetails;
import com.pinpoint.appointment.models.LeadDetails;
import com.pinpoint.appointment.models.LoginHelper;
import com.pinpoint.appointment.models.NotificationDetails;
import com.pinpoint.appointment.models.TrackingFriend;
import com.pinpoint.appointment.permissionUtils.PermissionClass;
import com.pinpoint.appointment.utils.Debug;
import com.pinpoint.appointment.utils.GPSTracker;
import com.pinpoint.appointment.utils.Util;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import io.reactivex.disposables.CompositeDisposable;

//import com.google.firebase.firestore.DocumentReference;
//import com.google.firebase.firestore.DocumentSnapshot;
//import com.google.firebase.firestore.FirebaseFirestore;

public class FriendLocationTracking extends BaseActivity implements ClickEvent {

    private static final String TAG = LocationTracking.class.getSimpleName();

    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    private static final long TIMER_INTERVAL = 3000L;
    private static final long ANIMATION_MARKER_DURATION = 2000L;
    private static int ZOOM_LEVEL = 14;
    private boolean mBound = false;
    String friendId = "0";
    MapView mapView;
    GoogleMap googleMap;
    private Handler handler1;
    private Marker marker, userPositionMarker;

    boolean cameraAnimated = false;
    private ValueAnimator valueAnimator;
    long animationDuration = 100;
    int minDistanceForAnimation = 2;
    private float v;
    private double lng;
    private double lat;
    public ImageView iv_back, iv_settings;
    CardView mCardView;
    public TextView tv_header;
    ActivityLocationtrackingBinding locationtrackingBinding;
    TimerTask timerTask;
    Timer timer;
    final Handler handler = new Handler();
    FriendDetails frienddetails;
    AppointmentData appointmentDetails;
    NotificationDetails notificationDetails;
    String oneAddress = "", secondAddress = "", mSubAdminArea = "";
    GPSTracker gpsTracker;
    double currentLatitude = 0, currentLongitude = 0;
    Marker myLocationMarker = null;
    Dialog dialog;
    int counter = 0;

    //private MyReceiver myReceiver;

    //    File path;
//    File file;
//    String locationContent="";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_locationtracking);
        locationtrackingBinding = DataBindingUtil.setContentView(this, R.layout.activity_locationtracking);
        if (Util.isTabletDevice(this)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            ZOOM_LEVEL = 17;
        }

        //02-22-2020 - Removed by TBL. Replaced location update listening using RxBus
        //myReceiver = new MyReceiver();

        iv_back = (ImageView) findViewById(R.id.iv_imgMenu_Back);
        iv_settings = (ImageView) findViewById(R.id.iv_imgSetting);
        tv_header = (TextView) findViewById(R.id.tv_txtTitle);
        mCardView = (CardView) findViewById(R.id.card_details);
        tv_header.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_SEMIBOLD));
        tv_header.setText(MessageHelper.getInstance().getAppMessage(getString(R.string.str_friend_location)));
        iv_settings.setVisibility(View.INVISIBLE);
        locationtrackingBinding.btGetDirection.setVisibility(View.GONE);
//      myReceiver = new MyReceiver();
        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();


//      FirebaseCrash.report();
//      Crashlytics.logException(new Exception("Testing Crash reporting"));
//      Crashlytics.log(1, "Crash Testing df","Crashed successfully Again");
//      Crashlytics.getInstance().crash();

    }

    @SuppressLint("MissingPermission")
    private void getLatLongFromAddress(String address) {
        double lat = 0.0, lng = 0.0;
        LatLng p1 = null;
        Geocoder geoCoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresss;
            try {
                // May throw an IOException
                addresss = geoCoder.getFromLocationName(address, 5);

                Address location = addresss.get(0);
                if (location != null) {
                    DecimalFormat df = new DecimalFormat("#.######");
                    p1 = new LatLng(Double.parseDouble(df.format(location.getLatitude())), Double.parseDouble(df.format(location.getLongitude())));


                    lat = p1.latitude;
                    lng = p1.longitude;
                    final LatLng sydney1 = new LatLng(p1.latitude, p1.longitude);

                    if (appointmentDetails.getAppointDirection().equalsIgnoreCase("sent")) {

                        Marker markerAppointment = googleMap.addMarker(new MarkerOptions().position(sydney1).title(appointmentDetails.getName()).snippet(appointmentDetails.getAddress()).icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(R.drawable.marker_appointment))));

                    } else {
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(p1.latitude,
                                p1.longitude), ZOOM_LEVEL), (int) animationDuration, new GoogleMap.CancelableCallback() {
                            @Override
                            public void onFinish() {
                                //Here you can take the snapshot or whatever you want
                                cameraAnimated = true;


                                marker = googleMap.addMarker(new MarkerOptions().position(sydney1).title(appointmentDetails.getName()).snippet(appointmentDetails.getAddress()).icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(R.drawable.marker_appointment))));
                                marker.showInfoWindow();
                                locationtrackingBinding.btnMyLocation.setVisibility(View.GONE);
                                locationtrackingBinding.mapView.setVisibility(View.VISIBLE);
//                              CustomDialog.getInstance().hide();
//                            }
//                                    startTimer();


                            }

                            @Override
                            public void onCancel() {

                            }
                        });
                    }

                }
            } catch (IOException ex) {

                ex.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Bitmap getMarkerBitmapFromView(@DrawableRes int resId) {

        View customMarkerView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.marker_icon, null);
        ImageView markerImageView = (ImageView) customMarkerView.findViewById(R.id.profile_image);
        markerImageView.setImageResource(resId);
        customMarkerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        customMarkerView.layout(0, 0, customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight());
        customMarkerView.buildDrawingCache();
        Bitmap returnedBitmap = Bitmap.createBitmap(customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN);
        Drawable drawable = customMarkerView.getBackground();
        if (drawable != null)
            drawable.draw(canvas);
        customMarkerView.draw(canvas);
        return returnedBitmap;
    }

    @Override
    public void onClickEvent(View view) {
        if (checkPermissions()) {
            switch (view.getId()) {
                case R.id.iv_imgMenu_Back:
                    googleMap.clear();
                    stopTimerTask();
                /* 02-22-2020 - Removed by TBL. Replaced location update listening using RxBus
                try {
                    LocalBroadcastManager.getInstance(this).unregisterReceiver(myReceiver);
                } catch (Exception unlikely) {
                    Log.e(TAG, "Lost location permission. Could not remove updates. " + unlikely);
                }
                */
                    disposables.clear();
                    finish();
                    break;
                case R.id.btn_myLocation:
                    setUserCurrentLocation();
                    break;
            }
        } else {
            finish();
        }
    }

    /*This Method Main Purpose Set UserCurrentLocation*/
    private void setUserCurrentLocation() {
//        if (gpsTracker != null)
//        {
        gpsTracker = new GPSTracker(this);
        if (gpsTracker.canGetLocation()) {
            currentLatitude = gpsTracker.getLatitude();
            currentLongitude = gpsTracker.getLongitude();
            gpsTracker.stopUsingGPS();

            stopService(new Intent(this, GPSTracker.class));
            final LatLng Mlatitude = new LatLng(currentLatitude, currentLongitude);

            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLatitude, currentLongitude), ZOOM_LEVEL), (int) 100, new GoogleMap.CancelableCallback() {

                @Override
                public void onFinish() {
                    //Here you can take the snapshot or whatever you want
                    cameraAnimated = true;
                    if (myLocationMarker == null) {
                        myLocationMarker = googleMap.addMarker(new MarkerOptions().position(Mlatitude).title(LoginHelper.getInstance().getName()).icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(R.drawable.marker))));
                    } else {
                        myLocationMarker.setPosition(Mlatitude);
                    }
                    myLocationMarker.showInfoWindow();
                    if (dialog != null && dialog.isShowing())
                        dialog.dismiss();
//                                    startTimer();
                }

                @Override
                public void onCancel() {

                }
            });

        }
//        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (PermissionClass.checkPermission(FriendLocationTracking.this, PermissionClass.REQUEST_CODE_RUNTIME_PERMISSION, Collections.singletonList(Manifest.permission.ACCESS_FINE_LOCATION))) {
            try {
                if (getIntent().getExtras() != null) {
                    Bundle bundle = getIntent().getExtras();
//                fromActivity = bundle.getString(BaseConstants.KEY_FROM);

//                    CustomDialog.getInstance().showProgressBar(FriendLocationTracking.this);
                    showProgressBar(FriendLocationTracking.this);
                    if (getIntent().hasExtra(ApiList.KEY_FRIENDID)) {
                        friendId = getIntent().getStringExtra(ApiList.KEY_FRIENDID);
                    }
                    if (getIntent().hasExtra(BaseConstants.KEY_OBJECT)) {
                        frienddetails = (FriendDetails) getIntent().getSerializableExtra(BaseConstants.KEY_OBJECT);
                    }
                    if (frienddetails != null) {
                        mCardView.setVisibility(View.VISIBLE);
                        locationtrackingBinding.tvMessage.setText(frienddetails.getName());
                        locationtrackingBinding.tvPhone.setText(frienddetails.getPhone());
//                        locationtrackingBinding.tvTime.setText(frienddetails.getEmail());
                        locationtrackingBinding.tvTime.setVisibility(View.GONE);
                        Picasso.get().load(frienddetails.getProfileimage()).noFade().placeholder(R.drawable.user1)
                                .into(locationtrackingBinding.ivImgProfile);
                        tv_header.setText(Util.getAppKeyValue(FriendLocationTracking.this, R.string.str_friend_location));
                    }


                }
            } catch (Exception ex) {
            }


            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    if (!isFinishing()) {
                        MapsInitializer.initialize(FriendLocationTracking.this);


                        mapView.getMapAsync(new OnMapReadyCallback() {
                            @SuppressLint("MissingPermission")
                            @Override
                            public void onMapReady(GoogleMap mMap) {
                                googleMap = mMap;

//                                final LatLng latLng = new LatLng(currentLatitude, currentLongitude);
//                                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 25);
//                                googleMap.animateCamera(cameraUpdate);

                                googleMap.setMyLocationEnabled(false);
                                googleMap.getUiSettings().setMyLocationButtonEnabled(false);
                                googleMap.getUiSettings().setZoomControlsEnabled(true);
//                                googleMap.getUiSettings().setMapToolbarEnabled(false);

                                TrackingFriend.getLastTrackingDetails(FriendLocationTracking.this, friendId, dataObserver, true);

                            }
                        });
                    }
                }
            }, 50);


        } else {
            requestPermissions();
        }
    }

    /*getAddress From Latitude And Longitude Value*/
    private void getAddressFromLocation(final double latitude, final double longitude, final Context context) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addressList = geocoder.getFromLocation(latitude, longitude, 5);
            if (addressList != null && addressList.size() > 0) {
                Address address = addressList.get(0);
                oneAddress = address.getSubThoroughfare();
                secondAddress = address.getThoroughfare();
                mSubAdminArea = address.getSubAdminArea();

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private CompositeDisposable disposables = new CompositeDisposable();

    @Override
    protected void onResume() {
        super.onResume();
        if (googleMap != null) {
            if (cameraAnimated) {
                if (appointmentDetails != null || frienddetails != null) {
                    startTimer();
                }
                handler1 = null;
                locationtrackingBinding.mapView.setVisibility(View.GONE);
                locationtrackingBinding.btnMyLocation.setVisibility(View.GONE);

                /* 02-22-2020 - Removed by TBL. Replaced location update listening using RxBus
                if(myReceiver!=null)
                {
//                    if(marker!=null)
//                    {
                        IntentFilter intentFilter = new IntentFilter();
                        intentFilter.addAction(LocationUpdateServiceBackground.ACTION_BROADCAST_LOCATION);
                        registerReceiver(myReceiver, intentFilter);
//                    }
                }
                */

                startLocationUpdateBus();

            }

        }

    }

    private void startLocationUpdateBus(){
        disposables.clear();
        disposables.add(
                ((BaseApplication) getApplication()).locationBus().toObservable().subscribe(
                        location -> {if(location != null && googleMap != null) handleLocationUpdate(location);}
                )
        );
    }

    private void handleLocationUpdate(Location location){

        if (handler1 != null) {
            handler1.postDelayed(() -> {
                final LatLng endPosition = new LatLng(location.getLatitude(), location.getLongitude());
                if (myLocationMarker == null) {
                    myLocationMarker = googleMap.addMarker(new MarkerOptions().position(endPosition).title(LoginHelper.getInstance().getName()).icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(R.drawable.marker))));
                }

                final LatLng startPosition = myLocationMarker.getPosition();

                Location startLocation = new Location("A");
                startLocation.setLatitude(startPosition.latitude);
                startLocation.setLongitude(startPosition.longitude);

                Location endLocation = new Location("B");
                endLocation.setLatitude(endPosition.latitude);
                endLocation.setLongitude(endPosition.longitude);
                double distance = startLocation.distanceTo(endLocation);
                if (distance > 1) {

                    valueAnimator = ValueAnimator.ofFloat(0, 1);
                    valueAnimator.setDuration(2000); //dg
                    valueAnimator.setInterpolator(new LinearInterpolator());
                    valueAnimator.addUpdateListener(valueAnimator -> {

                        v = (float) valueAnimator.getAnimatedValue();
                        lng = v * endPosition.longitude + (1 - v) * startPosition.longitude;
                        lat = v * endPosition.latitude + (1 - v) * startPosition.latitude;
                        LatLng newPos = new LatLng(lat, lng);
                        myLocationMarker.setPosition(newPos);

                    });
                    valueAnimator.start();
                }
            }, 16);
        } else {
            LatLng sydney1 = new LatLng(location.getLatitude(), location.getLongitude());
            if (myLocationMarker == null) {
                myLocationMarker = googleMap.addMarker(new MarkerOptions().position(sydney1).title("").snippet(""));
            } else {
                myLocationMarker.setPosition(sydney1);
            }

            handler1 = new Handler();


        }

    }

    @Override
    public void onPause() {
        super.onPause();
        stopTimerTask();

        /* 02-22-2020 - Removed by TBL. Replaced location update listening using RxBus
        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(myReceiver);
        } catch (Exception unlikely) {
            Log.e(TAG, "Lost location permission. Could not remove updates. " + unlikely);
        }
        */
        disposables.clear();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        stopTimerTask();

        /* 02-22-2020 - Removed by TBL. Replaced location update listening using RxBus
        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(myReceiver);
        } catch (Exception unlikely) {
            Log.e(TAG, "Lost location permission. Could not remove updates. " + unlikely);
        }
        */
        disposables.clear();
    }

    public void stopTimerTask() {
        //stop the timer, if it's not already null

        if (timer != null) {
            counter = 0;
            timer.cancel();
            timer = null;
        }
    }

    @Override
    protected void onStop() {
        if (mBound) {
            // Unbind from the service. This signals to the service that this activity is no longer
            // in the foreground, and the service can respond by promoting itself to a foreground
            // service.
//            unbindService(mServiceConnection);
            mBound = false;
        }
        stopTimerTask();
        /* 02-22-2020 - Removed by TBL. Replaced location update listening using RxBus
        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(myReceiver);
        } catch (Exception unlikely) {
            Log.e(TAG, "" + unlikely);
        }
        */

        disposables.clear();

        super.onStop();
    }

    private boolean checkPermissions() {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");

        } else {
            Log.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(FriendLocationTracking.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionResult");
        if (requestCode == PermissionClass.REQUEST_CODE_RUNTIME_PERMISSION) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                if (PermissionClass.checkPermission(FriendLocationTracking.this, PermissionClass.REQUEST_CODE_RUNTIME_PERMISSION, Collections.singletonList(Manifest.permission.ACCESS_FINE_LOCATION))) {

                }

            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                restartActivity(FriendLocationTracking.this);
            } else {
                // Permission denied.
//                setButtonsState(false);
                handlePermission();

            }
        }
    }

    public void handlePermission() {
        if (ContextCompat.checkSelfPermission(FriendLocationTracking.this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // This is Case 1. Now we need to check further if permission was shown before or not

            if (ActivityCompat.shouldShowRequestPermissionRationale(FriendLocationTracking.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                displaySnackBar(PermissionClass.REQUEST_CODE_PERMISSION_SETTING);

            } else {
                displaySnackBar(PermissionClass.REQUEST_CODE_PERMISSION_SETTING);

            }

        } else {
//            call911();
            // This is Case 2. You have permission now you can do anything related to it
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case PermissionClass.REQUEST_CODE_PERMISSION_SETTING:

                if (checkPermissions()) {
                    restartActivity(FriendLocationTracking.this);
                } else {
                    displaySnackBar(PermissionClass.REQUEST_CODE_PERMISSION_SETTING);
                }
                break;
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
                        }
                )

                .show();
    }

    public static void restartActivity(Activity activity) {
//        if (Build.VERSION.SDK_INT >= 11) {
//            activity.recreate();
//        } else {
        activity.finish();
        activity.startActivity(activity.getIntent());
    }

    public boolean CheckGpsStatus() {

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    DataObserver dataObserver = new DataObserver() {
        @Override
        public void OnSuccess(RequestCode requestCode, Object object) {

            switch (requestCode) {
                case getfriendlocation:
                    locationtrackingBinding.btnMyLocation.setVisibility(View.GONE);
                    locationtrackingBinding.mapView.setVisibility(View.VISIBLE);
                    final List<TrackingFriend> trackDataList = (List<TrackingFriend>) object;
                    if (trackDataList != null && trackDataList.size() > 0 && trackDataList.get(0).getLatitude() != null) {
                        final TrackingFriend trackData = trackDataList.get(0);

                        String text = locationtrackingBinding.tvMessage.getText().toString();


                        try {

                        } catch (Exception ex) {
                        }
                        if (handler1 != null) {
                            //
                            handler1.postDelayed(() -> {
                                if (marker != null) {
                                    final LatLng startPosition = marker.getPosition();
                                    final LatLng endPosition = new LatLng(Double.parseDouble(trackData.getLatitude()), Double.parseDouble(trackData.getLongitude()));
                                    Location startLocation = new Location("A");
                                    startLocation.setLatitude(startPosition.latitude);
                                    startLocation.setLongitude(startPosition.longitude);

                                    Location endLocation = new Location("B");
                                    endLocation.setLatitude(endPosition.latitude);
                                    endLocation.setLongitude(endPosition.longitude);

                                    if (trackData.getOnlineStatus().equalsIgnoreCase("0")) {
//                                          locationtrackingBinding.tvMessage.setText(frienddetails.getName() + " (" + MessageHelper.getInstance().getAppMessage(getString(R.string.str_user_offline)) + ")");
                                        locationtrackingBinding.tvMessage.setText(frienddetails.getName());
                                        locationtrackingBinding.tvPhone.setText(MessageHelper.getInstance().getAppMessage(getString(R.string.str_user_offline)));
                                        locationtrackingBinding.cardContainer.setBackground(getResources().getDrawable(R.drawable.offline_red_btn_background));
                                        marker.setSnippet(MessageHelper.getInstance().getAppMessage(getString(R.string.str_user_offline)));
                                        marker.hideInfoWindow();
                                        marker.showInfoWindow();
                                    } else {
                                        locationtrackingBinding.tvMessage.setText(frienddetails.getName());
                                        locationtrackingBinding.tvPhone.setText(frienddetails.getPhone());
                                        locationtrackingBinding.cardContainer.setBackground(getResources().getDrawable(R.drawable.white_dialog));
                                        marker.setSnippet(frienddetails.getPhone());
                                        marker.hideInfoWindow();
                                        marker.showInfoWindow();
                                    }

                                    double distance = startLocation.distanceTo(endLocation);
                                    if (distance > 1) {
                                        valueAnimator = ValueAnimator.ofFloat(0, 1);
                                        valueAnimator.setDuration(TIMER_INTERVAL);
                                        valueAnimator.setInterpolator(new LinearInterpolator());
                                        valueAnimator.addUpdateListener(valueAnimator -> {
                                            v = (float) valueAnimator.getAnimatedValue();
                                            lng = v * endPosition.longitude + (1 - v) * startPosition.longitude;
                                            lat = v * endPosition.latitude + (1 - v) * startPosition.latitude;
                                            LatLng newPos = new LatLng(lat, lng);
                                            marker.setPosition(newPos);
                                        });
                                        valueAnimator.start();
                                    }
//
                                }
                            }, 16);
                        } else {
                            final LatLng sydney1 = new LatLng(Double.parseDouble(trackData.getLatitude()), Double.parseDouble(trackData.getLongitude()));


                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Double.parseDouble(trackData.getLatitude()),
                                    Double.parseDouble(trackData.getLongitude())), ZOOM_LEVEL), (int) animationDuration, new GoogleMap.CancelableCallback() {
                                @Override
                                public void onFinish() {
                                    //Here you can take the snapshot or whatever you want
                                    cameraAnimated = true;
                                    if (appointmentDetails != null) {
                                        startTimer();
                                    } else if (frienddetails != null) {
                                        startTimer();
                                    }
                                    /* 02-22-2020 - Removed by TBL. Replaced location update listening using RxBus
                                    IntentFilter intentFilter = new IntentFilter();
                                    intentFilter.addAction(LocationUpdateServiceBackground.ACTION_BROADCAST_LOCATION);
                                    registerReceiver(myReceiver, intentFilter);
                                    */
                                    startLocationUpdateBus();

//                                    LocalBroadcastManager.getInstance(FriendLocationTracking.this).registerReceiver(myReceiver,
//                                            new IntentFilter(LocationUpdateServiceBackground.ACTION_BROADCAST_LOCATION));
//                                    if (appointmentDetails != null)
//                                    {
//                                      locationtrackingBinding.btnMyLocation.setVisibility(View.VISIBLE);
//                                      locationtrackingBinding.mapView.setVisibility(View.VISIBLE);
//                                      marker = googleMap.addMarker(new MarkerOptions().position(sydney1).title(appointmentDetails.getName()).snippet(appointmentDetails.getPhone()).icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(R.drawable.red_marker))));
//                                      getLatLongFromAddress(appointmentDetails.getAddress());
//                                    }
//                                    else
//                                    {
//                                            setUserCurrentLocation();
                                    gpsTracker = new GPSTracker(FriendLocationTracking.this);
                                    if (gpsTracker.canGetLocation()) {
                                        currentLatitude = gpsTracker.getLatitude();
                                        currentLongitude = gpsTracker.getLongitude();
                                        gpsTracker.stopUsingGPS();
                                        stopService(new Intent(FriendLocationTracking.this, GPSTracker.class));
                                        final LatLng Mlatitude = new LatLng(currentLatitude, currentLongitude);
                                        if (myLocationMarker == null) {
                                            myLocationMarker = googleMap.addMarker(new MarkerOptions().position(Mlatitude).title(LoginHelper.getInstance().getName()).icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(R.drawable.marker))));
                                        } else {

                                            myLocationMarker.setPosition(Mlatitude);
                                        }
//                                      myLocationMarker=googleMap.addMarker(new MarkerOptions().position(Mlatitude).title(LoginHelper.getInstance().getName()).icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(R.drawable.marker))));
                                    }

                                    if (marker == null) {
                                        marker = googleMap.addMarker(new MarkerOptions().position(sydney1).title(frienddetails.getName()).snippet(frienddetails.getPhone()).icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(R.drawable.red_marker))));
                                    }

//                                  }

                                    marker.showInfoWindow();
//                                  CustomDialog.getInstance().hide();
                                    if (dialog != null && dialog.isShowing())
                                        dialog.dismiss();
                                }

                                @Override
                                public void onCancel() {

                                }
                            });
                            handler1 = new Handler();
//
                        }
                    }
                    break;


            }
        }

        @Override
        public void OnFailure(RequestCode requestCode, String errorCode, String error) {
            switch (requestCode) {

                case getfriendlocation:

                    if (appointmentDetails != null || frienddetails != null) {
                        if ((!errorCode.contains("ppol")) && (!error.contains("ppol"))) {
                            CustomDialog.getInstance().showAlert(FriendLocationTracking.this, errorCode, error, Util.getAppKeyValue(FriendLocationTracking.this, R.string.lblOk), Util.getAppKeyValue(FriendLocationTracking.this, R.string.str_dismiss), false);
                        }
                    }
                    locationtrackingBinding.btnMyLocation.setVisibility(View.GONE);
                    locationtrackingBinding.mapView.setVisibility(View.VISIBLE);
                    gpsTracker = new GPSTracker(FriendLocationTracking.this);
                    if (gpsTracker.canGetLocation()) {
                        currentLatitude = gpsTracker.getLatitude();
                        currentLongitude = gpsTracker.getLongitude();
                        gpsTracker.stopUsingGPS();
                        stopService(new Intent(FriendLocationTracking.this, GPSTracker.class));
                        final LatLng Mlatitude = new LatLng(currentLatitude, currentLongitude);
                        if (myLocationMarker == null) {
                            myLocationMarker = googleMap.addMarker(new MarkerOptions().position(Mlatitude).title(LoginHelper.getInstance().getName()).icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(R.drawable.marker))));
                        } else {
                            myLocationMarker.setPosition(Mlatitude);
                        }
//                myLocationMarker=googleMap.addMarker(new MarkerOptions().position(Mlatitude).title(LoginHelper.getInstance().getName()).icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(R.drawable.marker))));
                    }

                    if (frienddetails != null || appointmentDetails != null) {
                        if (marker == null) {
                            startTimer();
                        } else {
                            if (dialog != null && dialog.isShowing()) {
                                dialog.dismiss();
                            }
                        }
//                        setUserCurrentLocation();
                    }
//                    setUserCurrentLocation();
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


    public void startTimer() {
        //set a new Timer
        if (timer == null) {
            counter++;
            timer = new Timer();

            //initialize the TimerTask's job
            initializeTimerTask();

            //schedule the timer, after the first 5000ms the TimerTask will run every 10000ms
            timer.schedule(timerTask, 0, TIMER_INTERVAL);
        }
    }

    public void initializeTimerTask() {
        timerTask = new TimerTask() {
            public void run() {
                //use a handler to run a toast that shows the current timestamp
                handler.post(new Runnable() {
                    public void run() {

                        if (counter > 0) {
                            TrackingFriend.getLastTrackingDetails(FriendLocationTracking.this, frienddetails.getUserid(), dataObserver, false);
                        }
//                        }

                    }
                });
            }
        };
    }

    /* 02-22-2020 - Removed by TBL. Replaced location update listening using RxBus
    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (intent.getExtras() != null && intent.hasExtra(LocationUpdateServiceBackground.EXTRA_LOCATION)) {
                    final Location location = intent.getParcelableExtra(LocationUpdateServiceBackground.EXTRA_LOCATION);
                    Debug.trace("Receiver" + location.getLatitude() + "," + location.getLongitude());
                    if (location != null) {
                        if (handler1 != null) {
                            handler1.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    final LatLng endPosition = new LatLng(location.getLatitude(), location.getLongitude());
                                    if (myLocationMarker == null) {
                                        myLocationMarker = googleMap.addMarker(new MarkerOptions().position(endPosition).title(LoginHelper.getInstance().getName()).icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(R.drawable.marker))));
                                    }

                                    final LatLng startPosition = myLocationMarker.getPosition();


                                    Location startLocation = new Location("A");
                                    startLocation.setLatitude(startPosition.latitude);
                                    startLocation.setLongitude(startPosition.longitude);

                                    Location endLocation = new Location("B");
                                    endLocation.setLatitude(endPosition.latitude);
                                    endLocation.setLongitude(endPosition.longitude);
                                    double distance = startLocation.distanceTo(endLocation);
                                    if (distance > 1) {

                                        valueAnimator = ValueAnimator.ofFloat(0, 1);
                                        valueAnimator.setDuration(2000); //dg
                                        valueAnimator.setInterpolator(new LinearInterpolator());
                                        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                            @Override
                                            public void onAnimationUpdate(ValueAnimator valueAnimator) {

                                                v = (float) valueAnimator.getAnimatedValue();
                                                lng = v * endPosition.longitude + (1 - v) * startPosition.longitude;
                                                lat = v * endPosition.latitude + (1 - v) * startPosition.latitude;
                                                LatLng newPos = new LatLng(lat, lng);
                                                myLocationMarker.setPosition(newPos);

                                            }
                                        });
                                        valueAnimator.start();
                                    }
                                }
                            }, 16);
                        } else {
                            LatLng sydney1 = new LatLng(location.getLatitude(), location.getLongitude());
                            if (myLocationMarker == null) {
                                myLocationMarker = googleMap.addMarker(new MarkerOptions().position(sydney1).title("").snippet(""));
                            } else {
                                myLocationMarker.setPosition(sydney1);
                            }

                            handler1 = new Handler();


                        }
//                Toast.makeText(MainActivity.this, (int) location.getLatitude(),
//                        Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (Exception ex) {
                LeadDetails.setMessage(FriendLocationTracking.this, " From : FriendLocationTracking MyReceiver " + ex.toString());
            }
        }
    }
    */


    //
    public void showProgressBar(Context context) {
        dialog = new Dialog(context, R.style.DialogTheme);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setContentView(R.layout.custom_progressbar);

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


}
