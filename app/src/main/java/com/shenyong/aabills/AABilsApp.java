package com.shenyong.aabills;

import com.facebook.stetho.Stetho;
import com.mob.MobSDK;
import com.sddy.baseui.BaseApplication;

public class AABilsApp extends BaseApplication {

    @Override
    public void onCreate() {
        super.onCreate();

        MobSDK.init(this);
        Stetho.initializeWithDefaults(this);
        UserManager.Companion.autoLogin();
    }

    @Override
    public void exitApp() {
        SyncBillsService.Companion.stopService();
        super.exitApp();
    }
}
