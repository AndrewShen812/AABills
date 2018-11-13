package com.sddy.baseui;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.os.Bundle;

import java.util.LinkedList;

public class BaseApplication extends Application implements Application.ActivityLifecycleCallbacks {

    protected static BaseApplication mInstance;

    private LinkedList<Activity> mActivities = new LinkedList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;

        registerActivityLifecycleCallbacks(this);
    }

    public static BaseApplication getInstance() {
        return mInstance;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        mActivities.add(activity);
    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {

    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        mActivities.remove(activity);
    }

    public void exitApp() {
        for (Activity activity : mActivities) {
            activity.finish();
        }
        mActivities.clear();

        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        if (manager != null) {
            manager.killBackgroundProcesses(getPackageName());
        }

        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }
}
