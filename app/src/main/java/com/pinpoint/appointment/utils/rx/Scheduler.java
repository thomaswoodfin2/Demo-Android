package com.pinpoint.appointment.utils.rx;

public interface Scheduler {
    io.reactivex.Scheduler mainThread();
    io.reactivex.Scheduler io();
}
