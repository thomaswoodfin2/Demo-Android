package com.pinpoint.appointment.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.pinpoint.appointment.models.LoginHelper;
import com.pinpoint.appointment.service.ServiceNoDelay;
import com.pinpoint.appointment.utils.Constants;
import com.pinpoint.appointment.utils.Util;

public class ServiceRestartReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent)
    {
        try {
            Log.i(ServiceRestartReceiver.class.getSimpleName(), "Service Stops! Oops!!!!");
            if (LoginHelper.getInstance().isLoggedIn()) {
                context.startService(new Intent(context, ServiceNoDelay.class));

                //by sarfaraj
                Util.stopAlaramSensor(context);
                // Start New Sensor Reporting time
                Util.startSensorAlaram(context, Constants.SENSOR_REPORTING_5_MINUTES);
            }
        }catch (Exception ex)
        {}
    }
}