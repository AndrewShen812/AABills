package com.shenyong.aabills.ui.user

import android.annotation.SuppressLint
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.graphics.drawable.GradientDrawable
import com.sddy.baseui.dialog.MsgToast
import com.sddy.utils.DimenUtils
import com.sddy.utils.ViewUtils
import com.shenyong.aabills.AABilsApp
import com.shenyong.aabills.R
import com.shenyong.aabills.UserManager
import com.shenyong.aabills.room.BillDatabase
import com.shenyong.aabills.utils.RxUtils
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe

/**
 *
 *
 * Author: sheny
 * Date: 2018/11/10
 */
class UserCenterViewModel : ViewModel() {

    val userName = MutableLiveData<String>()

    val shortName = MutableLiveData<String>()

    val phone = MutableLiveData<String>()

    val headBg = MutableLiveData<GradientDrawable>()

    private var headColor: Int = AABilsApp.getInstance().resources.getColor(R.color.main_blue)


    fun loadUserProfile() {
        val user = UserManager.user
        phone.value = user.mPhone
        headColor = if (user.mHeadBg == 0) headColor else user.mHeadBg
        updateHeadBg(headColor)
        shortName.value = user.shortName
        userName.value = user.name
    }

    private fun updateHeadBg(color: Int) {
        val mNameBg = ViewUtils.getDrawableBg(R.color.main_blue)
        mNameBg.setColor(color)
        mNameBg.shape = GradientDrawable.OVAL
        val size = DimenUtils.dp2px(44f)
        mNameBg.setSize(size, size)
        headBg.value = mNameBg
    }

    @SuppressLint("CheckResult")
    fun saveUserProfile(name: String) {
        val user = UserManager.user
        if (!user.isLogin) {
            return
        }
        val validName = if (name.length > 2) name.substring(0, 2) else name
        userName.value = validName
        user.mName = validName
        shortName.value = user.shortName
        user.mHeadBg = headColor

        Observable.create(ObservableOnSubscribe<String> { emitter ->
            BillDatabase.getInstance().userDao().updateUser(user)
            emitter.onNext("")
            emitter.onComplete()
        })
        .compose(RxUtils.ioMainScheduler())
        .subscribe {
            MsgToast.shortToast("修改成功")
        }
    }
}