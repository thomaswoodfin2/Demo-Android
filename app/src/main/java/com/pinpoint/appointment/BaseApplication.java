package com.pinpoint.appointment;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.multidex.MultiDex;
import android.text.TextUtils;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.orhanobut.hawk.Hawk;
import com.pinpoint.appointment.di.Provider;
import com.pinpoint.appointment.di.component.ApplicationComponent;
import com.pinpoint.appointment.di.component.DaggerApplicationComponent;
import com.pinpoint.appointment.di.module.ApplicationModule;
import com.pinpoint.appointment.helper.PrefHelper;
import com.pinpoint.appointment.utils.Constants;
import com.pinpoint.appointment.utils.MyLifecycleHandler;
import com.pinpoint.appointment.utils.Util;
import com.pinpoint.appointment.utils.rx.RxBus;
import com.pinpoint.appointment.utils.rx.RxLocationBus;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import io.fabric.sdk.android.Fabric;

public class BaseApplication extends Application {

    public static final String TAG = BaseApplication.class.getSimpleName();
    public static HashMap<String, String> msgHashMap;

    public static HashMap<Integer, String> languageHashMap;
    public static AtomicInteger ATOMIC_INTEGER;
    public static int viewPagerPosition = 0;
    @SuppressLint("StaticFieldLeak")
    public static BaseApplication appInstance;
    public static HashMap<String, Typeface> mTypefaceMap;
    private RequestQueue mRequestQueue;
    public static boolean isAppRunning = false;
    public static FirebaseAnalytics mFirebaseAnalytics;
    private BroadcastReceiver mNetworkReceiver;
    public static synchronized BaseApplication getInstance() {
        return appInstance;
    }

    private RxBus bus;
    private RxLocationBus busLocation;

    @Override
    public void onCreate() {
        super.onCreate();

        appInstance = this;
        Util.deleteCache(this);

        msgHashMap = new HashMap<>();
        languageHashMap = new HashMap<>();
        ATOMIC_INTEGER = new AtomicInteger();
        mTypefaceMap = new HashMap<>();
        Crashlytics crashlyticsKit = new Crashlytics.Builder()
                .core(new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build())
                .build();

// Initialize Fabric with the debug-disabled crashlytics.
        Fabric.with(this, crashlyticsKit);
        Fabric.with(this, new Crashlytics());

//      Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(appInstance));
//        Thread.setDefaultUncaughtExceptionHandler(new DefaultExceptionHandler(this));

        getTypeface(BaseConstants.OPENSANS_LIGHT);
        getTypeface(BaseConstants.OPENSANS_SEMIBOLD);
        getTypeface(BaseConstants.OPENSANS_REGULAR);
        getTypeface(BaseConstants.OPENSANS_BOLD);

        registerActivityLifecycleCallbacks(new MyLifecycleHandler());
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        PrefHelper.setBoolean(BaseConstants.NOTIFICATION_RECEIVED,true);
        try
        {
            registerNetworkBroadcastForNougat();
//            MapsInitializer.initialize(this);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        setupRxBus();
        initHawk();
    }

    private void initHawk() {
        Hawk.init(this).build();
    }

    private void setupRxBus() {
        bus = new RxBus();
        busLocation = new RxLocationBus();
    }

    public RxBus bus() {
        return bus;
    }

    public RxLocationBus locationBus(){
        return busLocation;
    }

    public ApplicationComponent applicationComponent;

    @NonNull
    public ApplicationComponent getAppComponent(){
        if(applicationComponent == null){
            applicationComponent = DaggerApplicationComponent.builder()
                    .applicationModule(new ApplicationModule(this)).build();
            applicationComponent.inject(this);
            Provider.INSTANCE.setAppComponent(applicationComponent);
        }
        return applicationComponent;
    }

    private void registerNetworkBroadcastForNougat() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            registerReceiver(mNetworkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            registerReceiver(mNetworkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }
    }
    protected void unregisterNetworkChanges() {
        try {
            unregisterReceiver(mNetworkReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    public <T> void addToRequestQueue(String tag, Request<T> req) {
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
//        getRequestQueue().getCache().clear();
        getRequestQueue().add(req);

    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }
        return mRequestQueue;
    }

    public void cancelPendingRequests(String tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }

    public <T> void setRequestTimeout(Request<T> req) {
        int requestTimeout = Constants.REQUEST_TIMEOUT;
        req.setRetryPolicy(new DefaultRetryPolicy(
                requestTimeout,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    public <T> void setRequestTimeoutForService(Request<T> req) {
        int requestTimeout = 5000;
        req.setRetryPolicy(new DefaultRetryPolicy(
                requestTimeout,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    public Typeface getTypeface(final String file) {
        Typeface result = mTypefaceMap.get(file);
        if (result == null) {
            result = Typeface.createFromAsset(getAssets(), file);
            mTypefaceMap.put(file, result);
        }
        return result;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }


}