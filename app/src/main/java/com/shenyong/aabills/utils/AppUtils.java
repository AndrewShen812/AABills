package com.shenyong.aabills.utils;

import android.content.Context;
import android.content.pm.PackageManager;

import com.shenyong.aabills.AABilsApp;

public class AppUtils {
    public static String getVersionName() {
        String versionName = "";
        Context context = AABilsApp.getInstance();
        try {
            versionName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionName;
    }

    public static int getVersionCode() {
        int versionCodee = 0;
        Context context = AABilsApp.getInstance();
        try {
            versionCodee = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionCodee;
    }

}
