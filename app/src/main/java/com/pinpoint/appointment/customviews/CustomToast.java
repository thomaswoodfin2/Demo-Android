package com.pinpoint.appointment.customviews;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.pinpoint.appointment.BaseApplication;
import com.pinpoint.appointment.R;

public class CustomToast {
    private static CustomToast instance;

    public static CustomToast getInstance() {
        if (instance == null) {
            instance = new CustomToast();
        }
        return instance;
    }

    public void setCustomToast(String message) {
        View toastRoot = LayoutInflater.from(BaseApplication.appInstance).inflate(R.layout.custom_toast, null);
        TextView textView = toastRoot.findViewById(R.id.my_custom_toast);
        textView.setText(message);
        final Toast toast = new Toast(BaseApplication.appInstance);
        toast.setView(toastRoot);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.show();
        new Handler().postDelayed(new Runnable() {
            public void run() {
                toast.cancel();
            }
        }, 2000);
    }
}
