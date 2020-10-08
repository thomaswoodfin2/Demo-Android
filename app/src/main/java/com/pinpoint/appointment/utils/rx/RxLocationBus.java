package com.pinpoint.appointment.utils.rx;

import android.location.Location;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

public class RxLocationBus {

    public RxLocationBus() {
    }

    private PublishSubject<Location> bus = PublishSubject.create();

    public void send(Location loc) {
        bus.onNext(loc);
    }

    public Observable<Location> toObservable() {
        return bus;
    }

}