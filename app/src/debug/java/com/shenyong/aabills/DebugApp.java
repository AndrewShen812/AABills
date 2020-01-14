package com.shenyong.aabills;

import com.facebook.stetho.Stetho;

public class DebugApp extends AABilsApp {

    @Override
    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);
    }
}
