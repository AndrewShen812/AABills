package com.shenyong.aabills

import com.sddy.baseui.dialog.MsgToast
import com.shenyong.aabills.api.AAObserver
import com.shenyong.aabills.api.API
import com.shenyong.aabills.api.MobService
import com.shenyong.aabills.api.bean.LoginResult
import com.shenyong.aabills.api.bean.MobResponse
import com.shenyong.aabills.room.BillDatabase
import com.shenyong.aabills.room.User
import com.shenyong.aabills.utils.RxUtils
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
object UserManager {

    var user = User("")

    fun autoLogin() {
        Observable.create<User> { emitter ->
            val userDao = BillDatabase.getInstance().userDao()
            val localUser = userDao.findLastLoginUser()
            if (localUser != null) {
                login(localUser.mPhone, localUser.mPwd, null)
            }
            emitter.onComplete()
        }
            .compose(RxUtils.ioMainScheduler())
            .subscribe()

    }

    fun login(phone: String, pwd: String, callback: AAObserver<MobResponse<LoginResult>>?) {
        API.mobApi.login(MobService.LOGIN, MobService.KEY, phone, pwd)
            .compose(RxUtils.ioMainScheduler())
            .subscribe(object : AAObserver<MobResponse<LoginResult>>() {
                override fun onNext(response: MobResponse<LoginResult>) {
                    if (response.isSuccess() && response.result != null) {
                        val loginUser = User("")
                        loginUser.isLogin = true
                        loginUser.mPhone = phone
                        loginUser.mPwd = pwd
                        loginUser.mUid = response.result!!.uid
                        loginUser.mToken = response.result!!.token
                        user = loginUser
                        updateLastLoginUser()
                        // TODO 2018/11/27: 是否应该先提示
                        markNoUidBillAsMine()
                        SyncBillsService.startService()
                    }
                    if (!response.isSuccess() && response.hasMsg()) {
                        MsgToast.shortToast(response.msg)
                    }
                    callback?.onNext(response)
                }
            })
    }

    private fun updateLastLoginUser() {
        Observable.create<String> {
            val userDao = BillDatabase.getInstance().userDao()
            val users = userDao.queryOtherUsers(user.mUid)
            users.add(user)
            users.forEach {
                it.isLastLogin = it.mUid == user.mUid
            }
            userDao.updateLastLogin(users)
        }
            .compose(RxUtils.ioMainScheduler())
            .subscribe()
    }

    /**
     * 将本地登录用户前记录的账单mUid标记为当前登录用户
     */
    private fun markNoUidBillAsMine() {
        Observable.create<String> {
            val billDao = BillDatabase.getInstance().billDao()
            val noUidBills = billDao.noUidBills
            noUidBills.forEach {
                it.mUid = user.mUid
            }
            billDao.updateBills(noUidBills)
        }
                .compose(RxUtils.ioMainScheduler())
                .subscribe()
    }
}