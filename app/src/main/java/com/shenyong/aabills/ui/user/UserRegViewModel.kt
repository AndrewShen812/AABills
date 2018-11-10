package com.shenyong.aabills.ui.user

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.sddy.baseui.dialog.MsgToast
import com.shenyong.aabills.api.API
import com.shenyong.aabills.api.bean.MobResponse
import com.shenyong.aabills.api.MobService
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
class UserRegViewModel : ViewModel() {
    val regPhone = MutableLiveData<String>()
    val regCode = MutableLiveData<String>()
    val regPwd = MutableLiveData<String>()
    val regConfirmPwd = MutableLiveData<String>()
    val regSuccess = MutableLiveData<Boolean>()

    fun getSmsCode(phone: String) {

    }

    fun register() {
        val phone = regPhone.value ?: ""
        val pwd = regPwd.value ?: ""
        val confirmPwd = regConfirmPwd.value ?: ""
        if (pwd != confirmPwd) {
            MsgToast.shortToast("两次输入的密码不一致")
            return
        }
        API.mobApi.register(MobService.REG, MobService.KEY, phone, pwd)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<MobResponse<Int>> {
                    override fun onComplete() {
                    }

                    override fun onSubscribe(d: Disposable) {
                    }

                    override fun onNext(t: MobResponse<Int>) {
                        regSuccess.value = t.isSuccess()
                        if (!t.isSuccess() && t.hasMsg()) {
                            MsgToast.shortToast(t.msg)
                        }
                    }

                    override fun onError(e: Throwable) {
                        regSuccess.value = false
                    }

                })
    }
}