package com.shenyong.aabills.ui.viewmodel

import android.arch.lifecycle.ViewModel
import android.support.annotation.NonNull
import com.shenyong.aabills.UserManager
import com.shenyong.aabills.room.BillDatabase
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers

/**
 *
 *
 * Author: sheny
 * Date: 2018/11/11
 */
class MainViewModel : ViewModel() {

    fun saveLoginUser(@NonNull next: Consumer<String>) {
        val user = UserManager.user
        if (!user.isLogin) {
            next.accept("ok")
            return
        }
        Observable.create<String> {
            val userDao = BillDatabase.getInstance().userDao()
            user.isLastLogin = true
            userDao.insertUser(user)
            it.onNext("ok")
            it.onComplete()
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<String> {
                override fun onSubscribe(d: Disposable) {
                }

                override fun onComplete() {
                    next.accept("ok")
                }

                override fun onNext(t: String) {
                }

                override fun onError(e: Throwable) {
                    next.accept("ok")
                }
            })
    }
}