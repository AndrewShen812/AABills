package com.shenyong.aabills

import android.os.Build
import android.util.Base64
import com.alibaba.fastjson.JSON
import com.sddy.baseui.dialog.MsgToast
import com.sddy.utils.log.Log
import com.shenyong.aabills.api.AAObserver
import com.shenyong.aabills.api.API
import com.shenyong.aabills.api.MobService
import com.shenyong.aabills.api.UserService
import com.shenyong.aabills.api.bean.LoginResult
import com.shenyong.aabills.api.bean.MobResponse
import com.shenyong.aabills.api.bean.UserProfile
import com.shenyong.aabills.room.BillDatabase
import com.shenyong.aabills.room.User
import com.shenyong.aabills.rx.RxExecutor
import com.shenyong.aabills.utils.RxUtils
import com.shenyong.aabills.utils.TaskExecutor
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

/**
 *
 *
 * Author: sheny
 * Date: 2018/11/10
 */
object UserManager {

    @JvmField
    var  user = User(Build.MODEL)

    fun autoLogin() {
        RxExecutor.backgroundWork {
            val userDao = BillDatabase.getInstance().userDao()
            val localUser = userDao.findLastLoginUser()
            if (localUser != null) {
                user = localUser
                user.isLogin = true
                // 2020年1月14日 fixbug: 修复已存在记录因账单所属用户Uid为空显示“佚名”问题
                markNoUidBillAsMine()
            } else {
                // 新安装使用的用户，添加到数据库
                user.isLogin = true
                user.isLastLogin = true
                userDao.insertUser(user)
            }
        }.subscribe {
            SyncBillsService.startSyncBill()
        }
    }

    fun login(phone: String, pwd: String, callback: AAObserver<MobResponse<LoginResult>>?) {
        API.mobApi.login(MobService.LOGIN, MobService.KEY, phone, pwd)
            .compose(RxUtils.ioMainScheduler())
            .subscribe(object : AAObserver<MobResponse<LoginResult>>() {
                override fun onNext(response: MobResponse<LoginResult>) {
                    if (response.isSuccess() && response.result != null) {
                        user.isLogin = true
                        user.mPhone = phone
                        user.mPwd = pwd
                        user.mUid = response.result!!.uid
                        user.mToken = response.result!!.token
                        updateLastLoginUser()
                        // TODO 2018/11/27: 是否应该先提示
                        markNoUidBillAsMine()
                        updateOtherUserInfo()
                        SyncBillsService.startSyncBill()
                    }
                    if (!response.isSuccess() && response.hasMsg()) {
                        MsgToast.shortToast(response.msg)
                    }
                    callback?.onNext(response)
                }
            })
    }

    private fun updateLastLoginUser() {
        TaskExecutor.diskIO().execute {
            val userDao = BillDatabase.getInstance().userDao()
            val users = userDao.queryOtherUsers(user.mUid)
            users.add(user)
            users.forEach {
                it.isLastLogin = it.mUid == user.mUid
                userDao.updateLastLogin(it.mUid, it.isLastLogin)
            }
        }
    }

    /**
     * 将本地登录用户前记录的账单mUid标记为当前登录用户
     */
    private fun markNoUidBillAsMine() {
        TaskExecutor.diskIO().execute {
            val billDao = BillDatabase.getInstance().billDao()
            val noUidBills = billDao.noUidBills
            noUidBills.forEach {
                it.mUid = user.mUid
            }
            billDao.updateBills(noUidBills)
        }
    }

    /**
     * 更新本地的其他用户信息，同步别人的头像和昵称修改
     */
    private fun updateOtherUserInfo() {
        val userDao = BillDatabase.getInstance().userDao()
        Observable.create<List<User>> {
            it.onNext(userDao.queryOtherUsers(user.mUid))
            it.onComplete()
        }
            .flatMap {
                return@flatMap Observable.fromIterable(it)
            }
            .map {
                UserService.getUserProfile(it.mUid)
                .subscribe(object : Observer<MobResponse<String>> {
                    override fun onComplete() {}

                    override fun onSubscribe(d: Disposable) {}

                    override fun onNext(t: MobResponse<String>) {
                        try {
                            val v = String(Base64.decode(t.result ?: "", Base64.NO_WRAP))
                            Log.Http.d("${it.mPhone}-${it.mName} info:$v")
                            val profile = JSON.parseObject(v, UserProfile::class.java)
                            it.mName = profile.nickname
                            it.mHeadBg = profile.headColor
                            userDao.insertUser(it)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    override fun onError(e: Throwable) {}
                })
                return@map "OK"
            }
            .subscribeOn(Schedulers.io())
            .subscribe()
    }

    fun addLanUser(uid: String) {
        val userDao = BillDatabase.getInstance().userDao()
        UserService.getUserProfile(uid)
            .observeOn(Schedulers.io())
            .subscribe(object : Observer<MobResponse<String>> {
                override fun onComplete() {}

                override fun onSubscribe(d: Disposable) {}

                override fun onNext(t: MobResponse<String>) {
                    try {
                        val newUser = User("")
                        newUser.mUid = uid
                        val v = String(Base64.decode(t.result ?: "", Base64.NO_WRAP))
                        Log.Http.d("${newUser.mPhone}-${newUser.mName} info:$v")
                        val profile = JSON.parseObject(v, UserProfile::class.java)
                        newUser.mName = profile.nickname
                        newUser.mHeadBg = profile.headColor
                        userDao.insertUser(newUser)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                override fun onError(e: Throwable) {}
            })
    }
}