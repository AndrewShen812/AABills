package com.shenyong.aabills;

import com.mob.MobSDK;
import com.sddy.baseui.BaseApplication;
import com.sddy.utils.log.Log;
import com.sddy.utils.log.Logger;
import com.shenyong.aabills.room.BillDatabase;
import com.shenyong.aabills.room.User;
import com.shenyong.aabills.room.UserDao;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Emitter;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class AABilsApp extends BaseApplication {

    @Override
    public void onCreate() {
        super.onCreate();

        MobSDK.init(this);
        prepareUsers();
    }

    private void prepareUsers() {
        Observable observable = Observable.generate(new Consumer<Emitter<String>>() {
            @Override
            public void accept(Emitter<String> emitter) throws Exception {
                UserDao userDao = BillDatabase.getInstance().userDao();
                List<User> usersToInsert = new ArrayList<>();
                User user = userDao.queryUser("申勇");
                if (user == null) {
                    Log.Db.d("没有找到申勇，将插入该用户");
                    usersToInsert.add(new User("申勇"));
                }
                user = userDao.queryUser("廷玉");
                if (user == null) {
                    Log.Db.d("没有找到廷玉，将插入该用户");
                    usersToInsert.add(new User("廷玉"));
                }
                user = userDao.queryUser("漆英");
                if (user == null) {
                    Log.Db.d("没有找到漆英，将插入该用户");
                    usersToInsert.add(new User("漆英"));
                }
                user = userDao.queryUser("世麟");
                if (user == null) {
                    Log.Db.d("没有找到世麟，将插入该用户");
                    usersToInsert.add(new User("世麟"));
                }
                if (!usersToInsert.isEmpty()) {
                    userDao.insertUsers(usersToInsert);
                }
                emitter.onNext("数据插入完成，插入了" + usersToInsert.size() + "个用户");
                emitter.onComplete();
            }
        });
        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        Log.Db.d(s);
                    }
                });
    }
}
