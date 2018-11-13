package com.shenyong.aabills.ui.user

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.sddy.baseui.dialog.MsgToast
import com.shenyong.aabills.UserManager
import com.shenyong.aabills.api.API
import com.shenyong.aabills.api.bean.LoginResult
import com.shenyong.aabills.api.bean.MobResponse
import com.shenyong.aabills.api.MobService
import com.shenyong.aabills.room.BillDatabase
import com.shenyong.aabills.room.User
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.function.Function

/**
 *
 *
 * Author: sheny
 * Date: 2018/11/10
 */
class UserLoginViewModel : ViewModel() {
    val phoneText = MutableLiveData<String>()
    val pwdText = MutableLiveData<String>()
    val loginSuccess = MutableLiveData<Boolean>()

    fun userLogin() {
        val phone = phoneText.value ?: ""
        val pwd = pwdText.value ?: ""
        API.mobApi.login(MobService.LOGIN, MobService.KEY, phone, pwd)
                .map {
                    if (it.isSuccess() && it.result != null) {
                        val user = User("")
                        user.mPhone = phone
                        user.mPwd = pwd
                        user.mUid = it.result?.uid ?: ""
                        BillDatabase.getInstance().userDao().insertUser(user)
                    }
                    return@map it
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<MobResponse<LoginResult>> {
                    override fun onComplete() {
                    }

                    override fun onSubscribe(d: Disposable) {
                    }

                    override fun onNext(t: MobResponse<LoginResult>) {
                        loginSuccess.value = t.isSuccess()
                        if (t.isSuccess() && t.result != null) {
                            UserManager.user.isLogin = true
                            UserManager.user.mPhone = phone
                            UserManager.user.mPwd = pwd
                            UserManager.user.mUid = t.result?.uid ?: ""
                            UserManager.user.mToken = t.result?.token ?: ""
                        }
                        if (!t.isSuccess() && t.hasMsg()) {
                            MsgToast.shortToast(t.msg)
                        }
                    }

                    override fun onError(e: Throwable) {
                        loginSuccess.value = false
                    }

                })
    }
}