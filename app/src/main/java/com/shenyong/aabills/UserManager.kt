package com.shenyong.aabills

import com.sddy.baseui.dialog.MsgToast
import com.shenyong.aabills.api.API
import com.shenyong.aabills.api.MobService
import com.shenyong.aabills.api.bean.LoginResult
import com.shenyong.aabills.api.bean.MobResponse
import com.shenyong.aabills.room.BillDatabase
import com.shenyong.aabills.room.User
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

/**
 *
 *
 * Author: sheny
 * Date: 2018/11/10
 */
class UserManager {

    companion object {
        var user = User("")

        fun autoLogin() {
            Observable.create<User> { emitter ->
                val userDao = BillDatabase.getInstance().userDao()
                val localUser = userDao.findLastLoginUser()
                if (localUser != null) {
                    API.mobApi.login(MobService.LOGIN, MobService.KEY, localUser.mPhone, localUser.mPwd)
                        .subscribe(object : Observer<MobResponse<LoginResult>> {
                            override fun onComplete() {
                            }

                            override fun onSubscribe(d: Disposable) {
                            }

                            override fun onNext(response: MobResponse<LoginResult>) {
                                if (response.isSuccess() && response.result != null) {
                                    val loginUser = User("")
                                    loginUser.isLogin = true
                                    loginUser.mPhone = localUser.mPhone
                                    loginUser.mPwd = localUser.mPwd
                                    loginUser.mUid = response.result!!.uid
                                    loginUser.mToken = response.result!!.token
                                    emitter.onNext(loginUser)
                                }
                            }

                            override fun onError(e: Throwable) {
                            }
                        })
                }
                emitter.onComplete()
            }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<User> {
                    override fun onComplete() {
                    }

                    override fun onSubscribe(d: Disposable) {
                    }

                    override fun onNext(t: User) {
                        MsgToast.shortToast("登录成功")
                        SyncBillsService.startService()
                        user = t
                    }

                    override fun onError(e: Throwable) {
                    }
                })

        }
    }
}