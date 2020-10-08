package com.pinpoint.appointment.di.module;

import android.app.Application;
import android.app.NotificationManager;
import android.content.Context;
import android.location.Location;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.pinpoint.appointment.BaseApplication;
import com.pinpoint.appointment.utils.rx.AppScheduler;
import com.pinpoint.appointment.utils.rx.Scheduler;

import javax.inject.Singleton;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;


@Module
public class ApplicationModule {

    private final static long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    private final static float UPDATE_INTERVAL_IN_DISTANCE = 5;

    private Context context;

    public ApplicationModule(Context context) {
        this.context = context;
    }

    @Provides
    @Singleton
    public Context providesContext() {
        return context;
    }

    @Provides
    @Singleton
    public static Scheduler provideScheduler() {
        return new AppScheduler();
    }

    @Provides
    @Singleton
    public static LocationRequest provideLocationRequest() {
        return LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
                //.setSmallestDisplacement(UPDATE_INTERVAL_IN_DISTANCE);
    }

    @Provides
    @Singleton
    public FusedLocationProviderClient provideFusedLocationProviderClient(Context context){
        return LocationServices.getFusedLocationProviderClient(context);
    }
}
