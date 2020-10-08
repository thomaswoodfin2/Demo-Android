package com.pinpoint.appointment.activities;

import android.app.Activity;
import android.os.Bundle;
import androidx.fragment.app.FragmentActivity;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import com.pinpoint.appointment.R;
import com.pinpoint.appointment.interfaces.ClickEvent;

public class GlobalActivity extends FragmentActivity implements ClickEvent, TextWatcher{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        /*Any Exception fire automatically Crash report API will call*/
        //Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
    }

    @Override
    public void onClickEvent(View view) {

    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {

    }


    public Activity getCurrContext()
    {
        return this;
    }

    //setVisibilityBy
    public void setVisibilityBy(int id, int vstatus)
    {
        View v = findViewById(id);
        v.setVisibility(vstatus);
    }

    //setVisibilityState
    public void setVisibilityState(View v, int vstatus)
    {
        v.setVisibility(vstatus);
    }

}
