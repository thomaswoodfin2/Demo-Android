package com.pinpoint.appointment.utils.rx;

import androidx.annotation.NonNull;
import androidx.work.impl.model.WorkSpec;

import com.pinpoint.appointment.utils.rx.Scheduler;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class AppScheduler implements Scheduler {

    @Override
    public io.reactivex.Scheduler mainThread() {
        return AndroidSchedulers.mainThread();
    }

    @Override
    public io.reactivex.Scheduler io() {
        return Schedulers.io();
    }
}
