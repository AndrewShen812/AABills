package com.shenyong.aabills

import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.IBinder
import android.support.annotation.WorkerThread
import com.alibaba.fastjson.JSON
import com.sddy.utils.log.Log
import com.shenyong.aabills.room.BillDatabase
import com.shenyong.aabills.room.BillRecord
import com.shenyong.aabills.room.User
import com.shenyong.aabills.room.UserSyncRecord
import com.shenyong.aabills.sync.AAPacket
import com.shenyong.aabills.sync.StopEvent
import com.shenyong.aabills.utils.RxBus
import com.shenyong.aabills.utils.RxTimer
import com.shenyong.aabills.utils.TaskExecutor
import com.shenyong.aabills.utils.WifiUtils
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.io.IOException
import java.net.DatagramPacket
import java.net.InetAddress
import java.net.MulticastSocket
import java.util.concurrent.LinkedBlockingQueue


/**
 * 局域网账单同步服务
 * Author: sheny
 * Date: 2018/11/11
 */
class SyncBillsService : Service() {

    private var mSendTask: Disposable? = null
    private var mRecvTask: Disposable? = null
    private var mSendSocket: MulticastSocket? = null
    private var mRecvSocket: MulticastSocket? = null
    private val mSendQueue = LinkedBlockingQueue<ByteArray>()
    private val mSyncTimer = RxTimer()
    private val mTimeOutTimer = RxTimer()
    // 组播地址
    private lateinit var mGroupAddress: InetAddress
    private var mMyIp: String = ""
    private var mMulticastLock: WifiManager.MulticastLock? = null
    private var remainTime = TIME_OUT
    private var allowSyncBill = false
    private var allowScanUser = false
    private var isRunning = true
    // 发送记录，避免重复发送
    private val sendRecord = HashSet<String>()

    companion object {
        private const val PORT = 9999
        private const val PACK_PREFIX = "aabillsSync"
        // https://baike.baidu.com/item/%E7%BB%84%E6%92%AD%E5%9C%B0%E5%9D%80/6095039?fr=aladdin
        private const val GROUP_IP = "224.0.0.251"
//        private const val GROUP_IP = "238.255.255.1"
        private const val TIME_OUT = 60

        private fun startSerice(cmdType: Int) {
            val context = AABilsApp.getInstance().applicationContext
            val intent = Intent(context, SyncBillsService::class.java)
            intent.putExtra("cmdType", cmdType)
            context.startService(intent)
        }

        @JvmStatic
        fun startSyncBill() {
            startSerice(AAPacket.TYPE_SYNC)
        }

        fun startScanUser() {
            startSerice(AAPacket.TYPE_SCAN)
        }

        @JvmStatic
        fun stopService() {
            val context = AABilsApp.getInstance().applicationContext
            context.stopService(Intent(context, SyncBillsService::class.java))
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun enqueueData(packet: AAPacket, blocking: Boolean) {
        // TODO 2020-01-20: 发送数据前加密
        val data = "${PACK_PREFIX}_${packet.toJSONString()}".toByteArray()
        if (blocking) {
            mSendQueue.put(data)
        } else {
            mSendQueue.offer(data)
        }
    }

    /**
     * 添加请求到发送队列，包含本机ip和uid，接收方根据指令类型处理请求
     */
    private fun startBroadcast() {
        mSyncTimer.interval(3_000) {
            val user = UserManager.user

            val sync = AAPacket.syncPacket(mMyIp, user.mUid)
            enqueueData(sync, false)

            if (allowScanUser) {
                val scan = AAPacket.scanPacket(mMyIp, user.mUid)
                enqueueData(scan, false)
            }
        }
    }

    private fun startTimeOutTimer() {
        mTimeOutTimer.interval(1_000) {
            remainTime--
            if (remainTime >= 0) {
                RxBus.post(remainTime)
            } else {
                mTimeOutTimer.cancel()
                stopSelf()
                Log.Http.d("停止扫描")
            }
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
        startTimeOutTimer()
        val manager = this.getSystemService(Context.WIFI_SERVICE) as WifiManager
        mMulticastLock = manager.createMulticastLock("AABills Sync")
        mMulticastLock?.acquire()
        try {
            mGroupAddress = InetAddress.getByName(GROUP_IP)
            mSendSocket = MulticastSocket(PORT)
            mSendSocket?.reuseAddress = true
            mSendSocket?.joinGroup(mGroupAddress)
            mRecvSocket = MulticastSocket(PORT)
            mRecvSocket?.joinGroup(mGroupAddress)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        startBroadcast()

        startSendTask()
        startRecvTask()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val cmdType = intent?.getIntExtra("cmdType", 0)
        if (cmdType == AAPacket.TYPE_SYNC) {
            allowSyncBill = true
        }
        if (cmdType == AAPacket.TYPE_SCAN) {
            allowScanUser = true
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startSendTask() {
        mSendTask = Observable.create<String> {
            while (isRunning) {
                try {
                    val data = mSendQueue.poll()
                    if (data != null) {
                        mSendSocket?.let {
                            it.send(DatagramPacket(data, data.size,
                                    mGroupAddress, PORT))
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    break
                }
            }
        }
        .subscribeOn(Schedulers.io())
        .subscribe()
    }

    private fun startRecvTask() {
        mRecvTask = Observable.create(ObservableOnSubscribe<String> {
            val buffer = ByteArray(1024)
            while (true) {
                val udpPacket = DatagramPacket(buffer, buffer.size)
                try {
                    mRecvSocket?.receive(udpPacket)
                    val rcvData = String(udpPacket.data, udpPacket.offset, udpPacket.length)
                    if (!rcvData.startsWith(PACK_PREFIX)) {
                        Log.Http.d("不识别的请求：$rcvData")
                        continue
                    }
                    val packet = AAPacket.jsonToPacket(rcvData.split("_")[1])
                    onRcvPacket(packet)
                } catch (e: IOException) {
                    e.printStackTrace()
                    break
                }

            }
        })
        .subscribeOn(Schedulers.io())
        .subscribe()
    }

    /**
     * 解析收到的数据包
     */
    private fun onRcvPacket(packet: AAPacket) {
        // 过滤掉自己发出的广播包
        if (isFromMyself(packet)) {
            return
        }
        when (packet.type) {
            AAPacket.TYPE_SYNC -> {
                // 1、收到aabillsSync的同步请求
                Log.Http.d("同步请求：$packet")
                handleSyncRequest(packet)
            }
            AAPacket.TYPE_DATA -> {
                // 收到账单数据
                Log.Http.d("局域网账单：$packet")
                recvBill(packet)
            }
            AAPacket.TYPE_USER -> {
                // 收到局域网用户信息
                Log.Http.d("局域网用户：$packet")
                recvUser(packet)
            }
            AAPacket.TYPE_SCAN -> {
                // 收到其他用户的扫描请求，需要把自己的信息发送给对方
                Log.Http.d("扫描请求：$packet")
                sendUser(packet, UserManager.user)
            }
            else -> {
                Log.Http.d("不识别的请求：$packet")
            }
        }
    }

    private fun isFromMyself(packet: AAPacket): Boolean {
        val user = UserManager.user
        return mMyIp == packet.orgIp || user.mUid == packet.orgUid
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        if (mSendTask != null) {
            mSendTask!!.dispose()
        }
        if (mRecvTask != null) {
            mRecvTask!!.dispose()
        }
        mSendSocket?.close()
        mRecvSocket?.close()
        mSyncTimer.cancel()
        mTimeOutTimer.cancel()
        mMulticastLock?.release()
        // 清除个人中心秒数显示
        RxBus.post(0)
        // 通知好友列表页面停止扫描动画
        RxBus.post(StopEvent())
        Log.Http.d("服务已停止")
    }

    /**
     * 处理接收到的同步请求，检查是否有需要发送给对方的账单
     */
    private fun handleSyncRequest(packet: AAPacket) {
        if (packet.orgUid.isEmpty()) {
            return
        }

        TaskExecutor.diskIO().execute {
            val user = UserManager.user
            val userDao = BillDatabase.getInstance().userDao()
            val billDao = BillDatabase.getInstance().billDao()
            var syncRecord = userDao.getSyncRecord(user.mUid, packet.orgUid)
            val bills = if (syncRecord == null) {
                // 同步全部
                billDao.needSyncBills
            } else {
                // 部分同步
                billDao.getNeedSyncBills(syncRecord.mLastSentBillAddTime)
            }
            if (bills.isNotEmpty()) {
                val sendUsers = userDao.queryOtherUsers(packet.orgUid)
                if (sendUsers.isNotEmpty()) {
                    // 把用户信息发送给对方
                    sendUsers(sendUsers, packet.orgIp, packet.orgUid)
                }

                sendBills(bills, packet.orgIp, packet.orgUid)
                // 更新本机的同步记录
                val bill = bills.first()
                syncRecord = UserSyncRecord()
                syncRecord.mMyUid = user.mUid
                syncRecord.mLANUid = packet.orgUid
                syncRecord.mLastSentBillId = bill.mId
                syncRecord.mLastSentBillAddTime = bill.mAddTime
                userDao.updateSyncRecord(syncRecord)
            }
        }
    }

    @WorkerThread
    private fun sendUsers(users: List<User>, dstIp: String, dstUid: String) {
        val user = UserManager.user
        users.forEach  {
            val key = "user_${dstIp}_${it.mUid}"
            if (!sendRecord.contains(key)) {
                sendRecord.add(key)
                val data = AAPacket.userPacket(mMyIp, user.mUid)
                data.dstIp = dstIp
                data.dstUid = dstUid
                data.data = JSON.toJSONString(it)
                Thread.sleep(10)
                Log.Http.d("用户排队：${JSON.toJSONString(data)}")
                enqueueData(data, true)
            }
        }
    }

    private fun sendUser(packet: AAPacket, sendUser: User) {
        val key = "user_${packet.orgIp}_${sendUser.mUid}"
        // 收到发送用户请求，一定是对方在好友列表也发出了SCAN请求，所以只给对方发送一次是合理的
        if (sendRecord.contains(key)) {
            return
        }
        sendRecord.add(key)

        val user = UserManager.user
        val data = AAPacket.userPacket(mMyIp, user.mUid)
        data.dstIp = packet.orgIp
        data.dstUid = packet.orgUid
        data.data = JSON.toJSONString(sendUser)
        Log.Http.d("用户排队：${JSON.toJSONString(data)}")
        enqueueData(data, true)
    }

    /** 在工作线程处理，可能会阻塞 */
    @WorkerThread
    private fun sendBills(bills: List<BillRecord>, dstIp: String, dstUid: String) {
        val user = UserManager.user
        bills.forEach {
            val key = "sync_${dstIp}_${it.mId}"
            if (!sendRecord.contains(key)) {
                sendRecord.add(key)
                val data = AAPacket.dataPacket(mMyIp, user.mUid)
                data.dstIp = dstIp
                data.dstUid = dstUid
                data.data = JSON.toJSONString(it)
                Thread.sleep(10)
                Log.Http.d("账单排队：${JSON.toJSONString(data)}")
                enqueueData(data, true)
            }
        }
    }

    private fun recvBill(packet: AAPacket) {
        TaskExecutor.diskIO().execute {
            val billDao = BillDatabase.getInstance().billDao()
            val bill = JSON.parseObject(packet.data, BillRecord::class.java)
            billDao.insertBill(bill)
        }
    }

    private fun recvUser(packet: AAPacket) {
        TaskExecutor.diskIO().execute {
            val userDao = BillDatabase.getInstance().userDao()
            val rcvUser = JSON.parseObject(packet.data, User::class.java)
            val local = userDao.findLocalUser(rcvUser.mUid)
            // 是否上次登录信息以本地为准
            rcvUser.isLastLogin = local != null && local.isLastLogin
            // AA设置以本地为准
            rcvUser.isAaMember = local != null && local.isAaMember
            userDao.insertUser(rcvUser)
            if (allowScanUser && RxBus.hasObservers()) {
                // 通知好友列表页面，扫描到一个局域网用户
                RxBus.post(packet)
            }
        }
    }

}
