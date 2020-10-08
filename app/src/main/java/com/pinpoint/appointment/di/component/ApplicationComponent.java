package com.pinpoint.appointment.di.component;

import com.pinpoint.appointment.BaseApplication;
import com.pinpoint.appointment.di.module.ApplicationModule;
import com.pinpoint.appointment.di.module.PersistenceModule;
import com.pinpoint.appointment.di.viewmodel.ViewModelModule;
import com.pinpoint.appointment.service.BackgroundIntentService;
import com.pinpoint.appointment.workmanager.UpdateStatusWorker;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {
        ApplicationModule.class,
        PersistenceModule.class,
        ViewModelModule.class,
})
public interface ApplicationComponent {
    void inject(BaseApplication app);

    void inject(UpdateStatusWorker worker);

    void inject(BackgroundIntentService service);
}
