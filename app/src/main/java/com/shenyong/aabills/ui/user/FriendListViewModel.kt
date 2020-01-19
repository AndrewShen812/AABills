package com.shenyong.aabills.ui.user

import android.annotation.SuppressLint
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.graphics.drawable.GradientDrawable
import com.alibaba.fastjson.JSON
import com.sddy.utils.DimenUtils
import com.sddy.utils.ViewUtils
import com.shenyong.aabills.R
import com.shenyong.aabills.UserManager
import com.shenyong.aabills.listdata.FriendListData
import com.shenyong.aabills.room.BillDatabase
import com.shenyong.aabills.room.User
import com.shenyong.aabills.rx.RxExecutor
import com.shenyong.aabills.sync.AAPacket
import com.shenyong.aabills.utils.WifiUtils

class FriendListViewModel : ViewModel() {

    val friendList = MutableLiveData<List<FriendListData>>()

    private val innerList = ArrayList<FriendListData>()

    @SuppressLint("CheckResult")
    fun loadFriends() {
        RxExecutor.backgroundWork {
            val users = BillDatabase.getInstance().userDao().queryAllUsers()
            users.forEach {
                var data = getUser(it.mUid)
                if (data == null) {
                    data = FriendListData()
                    innerList.add(data)
                }
                data.uid = it.mUid
                data.name = it.name
                data.shortName = it.shortName
                data.isMyself = UserManager.user.mUid == it.mUid
                data.ip = if (data.isMyself) "IP: ${WifiUtils.getIpAddress()}" else ""
                data.nameBg = getBg(it.mHeadBg)
                data.checked.set(data.isMyself || it.isAaMember)
            }
            return@backgroundWork  innerList
        }.subscribe {
            friendList.value = innerList
        }
    }

    fun addScanUser(packet: AAPacket) {
        val newUser = JSON.parseObject(packet.data, User::class.java)
        var data = getUser(packet.orgUid)
        if (data == null) {
            data = FriendListData()
            data.checked.set(false)
            innerList.add(data)
        }
        data.uid = packet.orgUid
        data.name = newUser.name
        data.shortName = newUser.shortName
        data.isMyself = UserManager.user.mUid == newUser.mUid
        data.ip = "IP: ${packet.orgIp}"
        data.nameBg = getBg(newUser.mHeadBg)
        friendList.value = innerList
    }

    private fun getBg(color: Int): GradientDrawable {
        val drawable = ViewUtils.getDrawableBg(R.color.main_blue)
        drawable.setColor(color)
        drawable.shape = GradientDrawable.OVAL
        val size = DimenUtils.dp2px(44f)
        drawable.setSize(size, size)

        return drawable
    }

    private fun getUser(uid: String): FriendListData? {
        innerList.forEach {
            if (uid == it.uid) {
                return it
            }
        }

        return null
    }
}