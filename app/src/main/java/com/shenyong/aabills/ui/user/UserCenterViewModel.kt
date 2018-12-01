package com.shenyong.aabills.ui.user

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.graphics.drawable.GradientDrawable
import android.util.Base64
import com.alibaba.fastjson.JSON
import com.sddy.baseui.dialog.MsgToast
import com.sddy.utils.DimenUtils
import com.sddy.utils.ViewUtils
import com.sddy.utils.log.Log
import com.shenyong.aabills.AABilsApp
import com.shenyong.aabills.R
import com.shenyong.aabills.UserManager
import com.shenyong.aabills.api.API
import com.shenyong.aabills.api.MobService
import com.shenyong.aabills.api.UserService
import com.shenyong.aabills.api.bean.MobResponse
import com.shenyong.aabills.api.bean.UserProfile
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
class UserCenterViewModel : ViewModel() {

    val userName = MutableLiveData<String>()

    val phone = MutableLiveData<String>()

    val headBg = MutableLiveData<GradientDrawable>()

    private var headColor: Int = 0

    private fun setDefaultHead() {
        // 默认主题背景
        headColor = AABilsApp.getInstance().resources.getColor(R.color.main_blue)
        updateHeadBg(headColor)
    }

    fun loadUserProfile() {
        val user = UserManager.user
        setDefaultHead()
        if (!user.isLogin) {
            userName.value = "本机"
            return
        }
        phone.value = user.mPhone
        headColor = if (user.mHeadBg == 0) headColor else user.mHeadBg
        updateHeadBg(headColor)
        userName.value = user.nickName
        // 加载本地存的设置
        Observable.create<User> {
            val userDao = BillDatabase.getInstance().userDao()
            val localUser = userDao.findLocalUser(user.mUid)
            it.onNext(localUser)
            it.onComplete()
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<User> {
                override fun onComplete() {

                }

                override fun onSubscribe(d: Disposable) {
                }

                override fun onNext(t: User) {
                    headColor = t.mHeadBg
                    headBg.value?.setColor(t.mHeadBg)
                    userName.value = t.mName
                }

                override fun onError(e: Throwable) {
                }
            })
        UserService.getUserProfile(user.mUid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<MobResponse<String>> {
                    override fun onComplete() {
                    }

                    override fun onSubscribe(d: Disposable) {
                    }

                    override fun onNext(t: MobResponse<String>) {
                        if (t.isSuccess() && !t.result.isNullOrEmpty()) {
                            try {
                                val v = String(Base64.decode(t.result ?: "", Base64.NO_WRAP))
                                Log.Http.d("${user.mPhone}-${user.mName} info:$v")
                                val profile = JSON.parseObject(v, UserProfile::class.java)
                                userName.value = profile.nickname
                                headColor = profile.headColor
                                updateHeadBg(headColor)
                                user.mName = profile.nickname
                                user.mHeadBg = profile.headColor
                                UserManager.saveLocal()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }

                    override fun onError(e: Throwable) {
                        e.printStackTrace()
                    }

                })
    }

    private fun updateHeadBg(color: Int) {
        val mNameBg = ViewUtils.getDrawableBg(R.color.main_blue)
        mNameBg.setColor(color)
        mNameBg.shape = GradientDrawable.OVAL
        val size = DimenUtils.dp2px(44f)
        mNameBg.setSize(size, size)
        headBg.value = mNameBg
    }

    fun saveUserProfile(name: String) {
        val user = UserManager.user
        if (!user.isLogin) {
            return
        }
        val validName = if (name.length > 2) name.substring(0, 2) else name
        userName.value = validName
        user.mName = validName
        user.mHeadBg = headColor
        UserManager.saveLocal()
        val profile = UserProfile(headColor, validName)
        val encode = Base64.encodeToString(JSON.toJSONString(profile).toByteArray(), Base64.NO_WRAP)
        UserService.setUserProfile(user.mToken, user.mUid, encode)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<MobResponse<String>> {
                override fun onComplete() {
                }

                override fun onSubscribe(d: Disposable) {
                }

                override fun onNext(t: MobResponse<String>) {
                    if (t.isSuccess()) {
                        MsgToast.shortToast("保存成功")
                    }
                }

                override fun onError(e: Throwable) {
                }
            })
    }
}