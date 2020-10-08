package com.pinpoint.appointment.workmanager;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.ListenableWorker;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.pinpoint.appointment.di.Provider;
import com.pinpoint.appointment.di.component.ApplicationComponent;

public class UpdateStatusWorker extends Worker {

    public UpdateStatusWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        //init();
        //process task here

        return ListenableWorker.Result.success();
    }

    private void init(){
        ApplicationComponent applicationComponent = Provider.INSTANCE.getAppComponent();
        if(applicationComponent != null) applicationComponent.inject(this);
    }
}
