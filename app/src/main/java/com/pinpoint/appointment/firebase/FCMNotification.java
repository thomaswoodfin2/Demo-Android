package com.pinpoint.appointment.firebase;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.pinpoint.appointment.activities.SplashActivity;
import com.pinpoint.appointment.helper.PrefHelper;
import com.pinpoint.appointment.models.CheckVersion;
import com.pinpoint.appointment.utils.Debug;


import static com.android.volley.VolleyLog.TAG;

/**
 * Created by admin on 27-Nov-17.
 */

public class FCMNotification extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.

        PrefHelper.setString(PrefHelper.KEY_DEVICE_TOKEN, refreshedToken);
        try {
            CheckVersion.checkAppVersionService(getApplicationContext());
        }catch (Exception ex)
        {}
//        sendRegistrationToServer(refreshedToken);
    }
}
