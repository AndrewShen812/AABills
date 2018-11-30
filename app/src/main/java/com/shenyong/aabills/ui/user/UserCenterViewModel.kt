package com.shenyong.aabills.ui.user

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.shapes.Shape
import android.util.Base64
import com.sddy.utils.DimenUtils
import com.sddy.utils.ViewUtils
import com.shenyong.aabills.AABilsApp
import com.shenyong.aabills.R
import com.shenyong.aabills.UserManager
import com.shenyong.aabills.api.API
import com.shenyong.aabills.api.MobService
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
class UserCenterViewModel : ViewModel() {

    companion object {
        const val NAME_AND_BG = "name_bg"
    }

    val userName = MutableLiveData<String>()

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
        headColor = if (user.mIconBg == 0) headColor else user.mIconBg
        updateHeadBg(headColor)
        userName.value = user.nickName
        // 加载本地存的设置
        /*Observable.create<User> {
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
                    headColor = t.mIconBg
                    headBg.value?.setColor(t.mIconBg)
                    userName.value = t.mName
                }

                override fun onError(e: Throwable) {
                }
            })*/
        API.mobApi.getUserProfile(MobService.get_PROFILE, MobService.KEY, user.mUid, NAME_AND_BG)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<MobResponse<String>> {
                    override fun onComplete() {
                    }

                    override fun onSubscribe(d: Disposable) {
                    }

                    override fun onNext(t: MobResponse<String>) {
                        if (t.isSuccess() && !t.result.isNullOrEmpty()) {
                            // 保存格式：name&argb
                            val v = String(Base64.decode(t.result ?: "", Base64.NO_WRAP)).split("&")
                            userName.value = v?.get(0)
                            headColor = v?.get(1)!!.toInt()
                            updateHeadBg(headColor)
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
        val nameBg = validName + "&" + headColor.toString()
        val encode = Base64.encodeToString(nameBg.toByteArray(), Base64.NO_WRAP)
        API.mobApi.setUserProfile(MobService.get_PROFILE, MobService.KEY, user.mToken, user.mUid,
                NAME_AND_BG, encode)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<MobResponse<String>> {
                    override fun onComplete() {
                    }

                    override fun onSubscribe(d: Disposable) {
                    }

                    override fun onNext(t: MobResponse<String>) {
                    }

                    override fun onError(e: Throwable) {
                    }

                })
    }
}