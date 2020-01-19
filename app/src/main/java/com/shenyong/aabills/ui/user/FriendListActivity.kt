package com.shenyong.aabills.ui.user

import android.annotation.SuppressLint
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import com.sddy.baseui.BaseActivity
import com.sddy.baseui.dialog.MsgToast
import com.sddy.baseui.recycler.databinding.SimpleBindingAdapter
import com.shenyong.aabills.R
import com.shenyong.aabills.SyncBillsService
import com.shenyong.aabills.listdata.FriendListData
import com.shenyong.aabills.room.BillDatabase
import com.shenyong.aabills.rx.RxExecutor
import com.shenyong.aabills.sync.AAPacket
import com.shenyong.aabills.sync.StopEvent
import com.shenyong.aabills.utils.AppUtils
import com.shenyong.aabills.utils.RxBus
import com.shenyong.aabills.utils.ShareUtils
import com.shenyong.aabills.utils.WifiUtils
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import kotlinx.android.synthetic.main.activity_friend_list.*

class FriendListActivity : BaseActivity() {

    private val adapter = SimpleBindingAdapter()
    private var userEvent: Disposable? = null
    private var stopEvent: Disposable? = null
    private val userList = ArrayList<FriendListData>()
    private lateinit var viewModel: FriendListViewModel
    private var stopServiceWhenFinish = false
    private var scanCnt = 0

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friend_list)
        setTitle("AA好友")
        setFuncBtn("保存设置") {
            RxExecutor.backgroundWork {
                val userDao = BillDatabase.getInstance().userDao()
                userList.forEach {
                    userDao.setAaMember(it.uid, it.checked.get() ?: false)
                }
            }.subscribe {
                MsgToast.centerToast("已保存AA设置")
            }

        }

        rv_friends_list.adapter = adapter
        rv_friends_list.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)

        viewModel = ViewModelProviders.of(this).get(FriendListViewModel::class.java)
        viewModel.friendList.observe(this, Observer {
            if (it != null) {
                adapter.updateData(it)
                userList.clear()
                userList.addAll(it)
            }
        })
        viewModel.loadFriends()
        userEvent = RxBus.register(AAPacket::class.java, Consumer {
            scanCnt++
            viewModel.addScanUser(it)
        })
        stopEvent = RxBus.register(StopEvent::class.java, Consumer {
            if (!stopServiceWhenFinish) {
                // 进入页面时已打开同步服务，但运行完一次了，在本页面再次启动的服务要在离开时停止
                stopServiceWhenFinish = true
            }
            // 提示引导安装应用
            if (scanCnt == 0) {
                pb_scan.visibility = View.GONE
                val text = SpannableStringBuilder("未搜索到好友？\n提醒好友连接到同一WiFi，打开")
                val installText = SpannableString("AA账单")
                val clickSpan = object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        showShareDialog()
                    }
                }
                val colorSpan = ForegroundColorSpan(resources.getColor(R.color.main_blue))
                installText.setSpan(clickSpan, 0, installText.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
                installText.setSpan(colorSpan, 0, installText.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
                text.append(installText)
                text.append("并开启同步。")
                tv_scan_desc.text = text
                tv_scan_desc.movementMethod = LinkMovementMethod.getInstance()
//                tv_scan_desc.setText(R.string.scan_help)
                tv_scan_again.visibility = View.VISIBLE
            } else {
                ll_friend_list_scan.visibility = View.GONE
            }
        })
        tv_scan_again.setOnClickListener {
            pb_scan.visibility = View.VISIBLE
            tv_scan_again.visibility = View.GONE
            tv_scan_desc.setText(R.string.scanning)
            startScan()
        }
        // 进入页面时如果没启动服务，关闭页面时也关闭服务
        stopServiceWhenFinish = !AppUtils.isServiceRunning(this, SyncBillsService::class.java.name)
        startScan()
    }

    private fun showShareDialog() {
        ShareUtils.showShareDialog(this)
    }

    private fun startScan() {
        val myIp = WifiUtils.getIpAddress()
        if (WifiUtils.isWifiEnabled() && myIp.isNotEmpty()) {
            SyncBillsService.startScanUser()
        } else {
            MsgToast.centerToast("请先连上WiFi")
            ll_friend_list_scan.visibility = View.GONE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        RxBus.unregister(userEvent)
        RxBus.unregister(stopEvent)
        if (stopServiceWhenFinish) {
            SyncBillsService.stopService()
        }
    }
}
