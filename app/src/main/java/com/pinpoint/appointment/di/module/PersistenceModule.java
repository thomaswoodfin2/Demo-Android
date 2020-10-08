package com.pinpoint.appointment.di.module;

import android.content.Context;

import com.orhanobut.hawk.Hawk;
import com.orhanobut.hawk.HawkBuilder;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class PersistenceModule {

    @Provides
    @Singleton
    public HawkBuilder getHawk(Context context){
        HawkBuilder builder = Hawk.init(context);
        builder.build();
        return builder;
    }
}
