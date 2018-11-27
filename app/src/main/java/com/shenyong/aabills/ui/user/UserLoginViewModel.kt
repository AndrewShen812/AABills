package com.shenyong.aabills.ui.user

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.sddy.baseui.dialog.MsgToast
import com.shenyong.aabills.SyncBillsService
import com.shenyong.aabills.UserManager
import com.shenyong.aabills.api.AAObserver
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
        UserManager.login(phone, pwd, object : AAObserver<MobResponse<LoginResult>>() {
            override fun onNext(response: MobResponse<LoginResult>) {
                loginSuccess.value = response.isSuccess() && response.result != null
            }
        })
    }
}