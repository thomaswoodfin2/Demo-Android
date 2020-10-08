package com.pinpoint.appointment.utils;

import android.app.Activity;
import android.app.Application.ActivityLifecycleCallbacks;
import android.os.Bundle;
import android.util.Log;

import com.pinpoint.appointment.BaseApplication;


/**
 * This class use to maintain application state that application is in foreground or in background.
 * <br>There are so many cases handle like running state, destroy state,sleep and more.</br>
 * <p><strong>Example: </strong></p>
 * it will be use to handle push notification action.
 *
 * @see <a href="https://developer.android.com/reference/android/app/Activity.html#ActivityLifecycle"> ActivityLifecycle</a>
 * @see <a href="https://developer.android.com/training/basics/activity-lifecycle/starting.html">ActivityLifecycle state</a>
 */
public class MyLifecycleHandler implements ActivityLifecycleCallbacks {
    //  Used four separate variables here. You can, of course, just use two and
    // increment/decrement them instead of using four and incrementing them all.

    // variable declaration
//    private int resumed;
//    private int paused;
//    private int started;
//    private int stopped;

    // class object declaration
    private static Activity currentActivity;
    // If you want a static function you can use to check if your application is
    // foreground/background, you can use the following:
    // Replace the four variables above with these four
    private static int resumed;
    private static int paused;
    private static int started;
    private static int stopped;

    public static Activity getCurrentActivity() {
        return currentActivity;
    }

    // And these two public static functions
    public static boolean isApplicationVisible() {
        return started > stopped;
    }

    public static boolean isApplicationInForeground() {
        return resumed > paused;
    }

    public static boolean isApplicationInBackGround() {
        return paused > resumed;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

        currentActivity = activity;
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        Log.w("test", "onActivityDestroyed: started" + started + "resumed: " + resumed + " paused: " + paused + " stopped" + stopped);

        if (resumed == paused && resumed == started && resumed == stopped) {
            Log.e("MyLifecycleHandler", "App is closed");
            BaseApplication.isAppRunning = false;

            /*On back press app is close but MyApplication variables are not reset, So reset variable here*/
            /*if(MyApplication.appInstance!=null)
                MyApplication.appInstance.showRecFollowers=true;*/

//            ToastHelper.displayInfo("App is closed", Gravity.CENTER);
            //Toast.makeText(activity,"App is closed",Toast.LENGTH_SHORT).show();

            /*Clean Temp Directory, on close app */
            //cleanImageDirectories();
        } else
            BaseApplication.isAppRunning = true;

    }

    @Override
    public void onActivityResumed(Activity activity) {
        ++resumed;
        BaseApplication.isAppRunning = true;
        Log.w("MyActivityLifeCycle", "onActivityResumed isApplicationInForeground() " + isApplicationInForeground());

    }

    @Override
    public void onActivityPaused(Activity activity) {
        ++paused;
        Log.w("test", "application is in foreground: " + (resumed > paused));
        Log.w("MyActivityLifeCycle", "onActivityPaused isApplicationInForeground() " + isApplicationInForeground());
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

    @Override
    public void onActivityStarted(Activity activity) {
        ++started;

    }

    @Override
    public void onActivityStopped(Activity activity) {
        ++stopped;
        Log.w("test", "application is visible: " + (started > stopped));
        Log.w("MyActivityLifeCycle", "onActivityStopped isApplicationInForeground() " + isApplicationInForeground());
    }
}