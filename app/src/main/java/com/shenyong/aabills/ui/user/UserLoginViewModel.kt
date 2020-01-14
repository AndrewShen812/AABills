package com.shenyong.aabills.ui.user

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.shenyong.aabills.UserManager
import com.shenyong.aabills.api.AAObserver
import com.shenyong.aabills.api.bean.LoginResult
import com.shenyong.aabills.api.bean.MobResponse

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
        UserManager.login(phone, pwd, object : AAObserver<MobResponse<LoginResult>>() {
            override fun onNext(response: MobResponse<LoginResult>) {
                loginSuccess.value = response.isSuccess() && response.result != null
            }
        })
    }
}