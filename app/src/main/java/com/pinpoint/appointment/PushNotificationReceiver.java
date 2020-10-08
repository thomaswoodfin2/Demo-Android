package com.pinpoint.appointment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.pinpoint.appointment.api.ApiList;
import com.pinpoint.appointment.location.LocationUpdateServiceBackground;

public class PushNotificationReceiver extends BroadcastReceiver
{

    @Override
    public void onReceive(Context context, Intent intent)
    {
        if (intent.getExtras() != null)
        {
            try {
                String message = intent.getStringExtra(ApiList.KEY_MESSAGE);
                if (message != null && !message.equalsIgnoreCase(""))
                    BaseActivity.baseActivity.showMessagePopup(context, intent);
            }catch (Exception ex)
            {}
                try
                {
                    final Location location = intent.getParcelableExtra(LocationUpdateServiceBackground.EXTRA_LOCATION);
                    if(location!=null)
                    {
                        //BaseConstants.lastLatLOng=new LatLng(location.getLatitude(),location.getLongitude());
                    }
                }
                catch (Exception ex)
                {

                }
        }
    }
}
