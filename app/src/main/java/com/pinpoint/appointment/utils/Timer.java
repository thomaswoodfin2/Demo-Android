package com.pinpoint.appointment.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import com.pinpoint.appointment.activities.AddLeadsActivity;

public class Timer extends AsyncTask<Void, Void, Void> {
    Context mContext;
    private Handler threadHandler;
    public Timer(Context context, Handler threadHandler) {
        super();
        this.threadHandler=threadHandler;
        mContext = context;
    }
    @Override
    protected Void doInBackground(Void...params) {
        try {
            Thread.sleep(AddLeadsActivity.PERIOD);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Message.obtain(threadHandler, AddLeadsActivity.DONE, "").sendToTarget();
        return null;
    }
}