package com.shenyong.aabills;

import android.content.Intent;

import com.facebook.stetho.Stetho;
import com.mob.MobSDK;
import com.sddy.baseui.BaseApplication;
import com.sddy.baseui.dialog.MsgToast;
import com.sddy.utils.log.Log;
import com.sddy.utils.log.Logger;
import com.shenyong.aabills.api.API;
import com.shenyong.aabills.api.MobService;
import com.shenyong.aabills.api.bean.LoginResult;
import com.shenyong.aabills.api.bean.MobResponse;
import com.shenyong.aabills.room.BillDatabase;
import com.shenyong.aabills.room.User;
import com.shenyong.aabills.room.UserDao;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Emitter;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class AABilsApp extends BaseApplication {

    private Intent mServiceIntent;

    @Override
    public void onCreate() {
        super.onCreate();

        MobSDK.init(this);
        Stetho.initializeWithDefaults(this);
        UserManager.Companion.autoLogin();
        mServiceIntent = new Intent(this, SyncBillsService.class);
        startService(mServiceIntent);
    }

    @Override
    public void exitApp() {
        stopService(mServiceIntent);
        super.exitApp();
    }
}
