package com.shenyong.aabills

import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.IBinder
import com.alibaba.fastjson.JSON
import com.sddy.utils.log.Log
import com.shenyong.aabills.UserManager.user
import com.shenyong.aabills.api.AAObserver
import com.shenyong.aabills.room.BillDatabase
import com.shenyong.aabills.room.BillRecord
import com.shenyong.aabills.room.UserSyncRecord
import com.shenyong.aabills.sync.AAPacket
import com.shenyong.aabills.utils.RxTimer
import com.shenyong.aabills.utils.WifiUtils
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.MulticastSocket
import java.util.*
import java.util.concurrent.LinkedBlockingQueue


/**
 * Author: sheny
 * Date: 2018/11/11
 */
class SyncBillsService : Service() {

    private var mSendTask: Disposable? = null
    private var mRecvTask: Disposable? = null
    private var mUdpSocket: MulticastSocket? = null
    private var mRecvSocket: MulticastSocket? = null
    private val mSendQueue = LinkedBlockingQueue<ByteArray>(100)
    private val mSyncTimer = RxTimer()
    private lateinit var mBroadcastAddress: InetAddress
    private var mMyIp: String = ""
    private var mMulticastLock: WifiManager.MulticastLock? = null

    companion object {
        private const val SEND_PORT = 10085
        private const val PORT = 10086
        private const val PACK_PREFIX = "aabillsSync"

        fun startService() {
            val context = AABilsApp.getInstance().applicationContext
            context.startService(Intent(context, SyncBillsService::class.java))
        }

        fun stopService() {
            val context = AABilsApp.getInstance().applicationContext
            context.stopService(Intent(context, SyncBillsService::class.java))
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun enqueueData(packet: AAPacket, blocking: Boolean) {
        val data = "${PACK_PREFIX}_${packet.toJSONString()}".toByteArray()
        if (blocking) {
            mSendQueue.put(data)
        } else {
            mSendQueue.offer(data)
        }
    }

    private fun syncBroadcast() {
        mSyncTimer.interval(3_000) {
            // 发送同步请求，包含本机ip和uid，如果有需要同步给本机用户的账单，会收到TYPE_DATA数据包
            val sync = AAPacket.syncPacket(mMyIp, user.mUid)
            enqueueData(sync, false)
        }
    }

    override fun onCreate() {
        super.onCreate()
        mMyIp = WifiUtils.getIpAddress()
        val user = UserManager.user
        if (!WifiUtils.isWifiEnabled() || mMyIp.isEmpty() || !user.isLogin) {
            stopSelf()
            return
        }
        val manager = this.getSystemService(Context.WIFI_SERVICE) as WifiManager
        mMulticastLock = manager.createMulticastLock("AABills Sync")
        mMulticastLock?.acquire()
        try {
//            mBroadcastAddress = WifiUtils.getBroadcastAddress(this)
            mBroadcastAddress = InetAddress.getByName("239.255.255.250")
            mUdpSocket = MulticastSocket(PORT)
//            mUdpSocket = DatagramSocket()
            //设置本MulticastSocket发送的数据报被回送到自身
            mUdpSocket?.loopbackMode = false
            mUdpSocket?.reuseAddress = true
//            mUdpSocket?.broadcast = true
            mUdpSocket!!.joinGroup(mBroadcastAddress)
            mRecvSocket = MulticastSocket(PORT)
//            mRecvSocket?.broadcast = true
            mRecvSocket!!.joinGroup(mBroadcastAddress)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        syncBroadcast()

        mSendTask = Observable.create<String> {
            while (true) {
                val data = mSendQueue.poll()
                if (data != null) {
                    mUdpSocket?.let {
                        it.send(DatagramPacket(data, data.size,
                                mBroadcastAddress, SEND_PORT))
                    }
                }
            }
        }
            .subscribeOn(Schedulers.io())
            .subscribe()
        mRecvTask = Observable.create(ObservableOnSubscribe<String> {
            val buffer = ByteArray(1024)
            loop@ while (true) {
                val packet = DatagramPacket(buffer, buffer.size)
                try {
                    mRecvSocket?.receive(packet)
                    val rcvData = String(packet.data, packet.offset, packet.length)
                    Log.Http.d("收到数据：$rcvData")
                    if (!rcvData.startsWith(PACK_PREFIX)) {
                        Log.Http.d("不识别的请求：$rcvData")
                        continue
                    }
                    val packet = AAPacket.jsonToPacket(rcvData.split("_")[1])
                    when (packet.type) {
                        AAPacket.TYPE_SYNC -> {
                            // 过滤掉自己发出的广播包
                            if (isFromMyself(packet)) {
                                continue@loop
                            }
                            // 1、收到aabillsSync的同步请求
                            Log.Http.d("同步请求：$packet")
                            handleSyncRequest(packet)
                        }
                        AAPacket.TYPE_DATA -> {
                            if (isFromMyself(packet)) {
                                continue@loop
                            }
                            // 收到账单数据
                            Log.Http.d("局域网账单：$packet")
                            handleBillData(packet)
                        }
                        else -> {
                            Log.Http.d("不识别的请求：$rcvData")
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
        })
                .subscribeOn(Schedulers.io())
                .subscribe()
    }

    private fun isFromMyself(packet: AAPacket): Boolean {
        val user = UserManager.user
        return mMyIp == packet.orgIp || user.mUid == packet.orgUid
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mSendTask != null) {
            mSendTask!!.dispose()
        }
        if (mRecvTask != null) {
            mRecvTask!!.dispose()
        }
        mSyncTimer.cancel()
        mMulticastLock?.release()
    }

    /**
     * 处理接收到的同步请求，检查是否有需要发送给对方的账单
     */
    private fun handleSyncRequest(packet: AAPacket) {
        val user = UserManager.user
        Observable.create<String> {
            val userDao = BillDatabase.getInstance().userDao()
            val billDao = BillDatabase.getInstance().billDao()
            val syncRecord = userDao.getSyncRecord(user.mUid, packet.orgUid)
            var bills = if (syncRecord == null) {
                // 同步全部
                billDao.allBills
            } else {
                // 部分同步
                billDao.getLaterBills(syncRecord.mLastSentBillTimestamp)
            }
            if (bills.isNotEmpty()) {
                sendBills(bills, packet.orgIp, packet.orgUid)
                // 更新本机的同步记录
                val bill = bills.last()
                bills.sortWith(Comparator { b1, b2 ->
                    return@Comparator (b1.mTimestamp - b2.mTimestamp).toInt()
                })
                val syncRecord = UserSyncRecord()
                syncRecord.mMyUid = user.mUid
                syncRecord.mLANUid = packet.orgUid
                syncRecord.mLastSentBillId = bill.mId
                syncRecord.mLastSentBillTimestamp = bill.mTimestamp
                userDao.updateSyncRecord(syncRecord)
            }
        }
            .subscribeOn(Schedulers.io())
            .subscribe()
    }

    /** 在工作线程处理，可能会阻塞 */
    private fun sendBills(bills: List<BillRecord>, dstIp: String, dstUid: String) {
        val user = UserManager.user
        bills.forEach {
            val data = AAPacket.dataPacket(mMyIp, user.mUid)
            data.dstIp = dstIp
            data.dstUid = dstUid
            data.data = JSON.toJSONString(it)
            Thread.sleep(20)
            enqueueData(data, true)
        }
    }

    private fun handleBillData(packet: AAPacket) {
        Observable.create<String> {
            val billDao = BillDatabase.getInstance().billDao()
            val bill = JSON.parseObject(packet.data, BillRecord::class.java)
            billDao.insertBill(bill)
        }
            .subscribeOn(Schedulers.io())
            .subscribe(object : AAObserver<String>() {
                override fun onNext(t: String) {

                }
            })
    }
}
