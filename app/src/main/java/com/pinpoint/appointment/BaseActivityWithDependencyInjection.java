package com.pinpoint.appointment;

import com.pinpoint.appointment.di.component.ApplicationComponent;
import com.pinpoint.appointment.di.viewmodel.ViewModelFactory;

import javax.inject.Inject;

public class BaseActivityWithDependencyInjection extends BaseActivity {

    @Inject
    ViewModelFactory viewModelFactory;

    protected ApplicationComponent getAppComponent(){
        return ((BaseApplication) getApplication()).getAppComponent();
    }

}
