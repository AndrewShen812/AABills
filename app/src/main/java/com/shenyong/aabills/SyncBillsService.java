package com.shenyong.aabills;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.sddy.utils.log.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Author: sheny
 * Date: 2018/11/11
 */
public class SyncBillsService extends Service {

    private Disposable mSyncTask;
    private MulticastSocket mSendSocket;
    private MulticastSocket mRecvSocket;
    private static final int PORT = 10086;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static InetAddress getBroadcastAddress(Context context) throws UnknownHostException {
        WifiManager wifi = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcp = wifi.getDhcpInfo();
        if(dhcp==null) {
            return InetAddress.getByName("255.255.255.255");
        }
        int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++) {
            quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
        }
        return InetAddress.getByAddress(quads);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            mSendSocket = new MulticastSocket();
            mRecvSocket = new MulticastSocket(PORT);
            mRecvSocket.setBroadcast(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mSyncTask = Observable.interval(3000, TimeUnit.MILLISECONDS)
                .observeOn(Schedulers.io())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        byte[] data = "aabills 同步请求".getBytes();
                        //224.0.0.1为广播地址
//                        InetAddress address = InetAddress.getByName("255.255.255.255");
                        InetAddress address = getBroadcastAddress(SyncBillsService.this);
                        DatagramPacket packet = new DatagramPacket(data, data.length,
                                address, PORT);
                        mSendSocket.send(packet);
                        Log.Common.d("发送同步请求");
                    }
                });
        new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                while (true) {
                    try {
                        // 阻塞接受
                        mRecvSocket.receive(packet);
                        Log.Common.d("收到广" +
                                "播：" + new String(packet.getData()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mSyncTask != null) {
            mSyncTask.dispose();
        }
    }
}
