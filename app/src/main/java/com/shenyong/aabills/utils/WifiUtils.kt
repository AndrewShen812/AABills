package com.shenyong.aabills.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import com.shenyong.aabills.AABilsApp
import java.net.InetAddress
import java.net.UnknownHostException

/**
 *
 * @author ShenYong
 * @date 2018/11/26
 */
object WifiUtils {

    @Throws(UnknownHostException::class)
    fun getBroadcastAddress(context: Context): InetAddress {
        val wifi = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val dhcp = wifi.dhcpInfo ?: return InetAddress.getByName("255.255.255.255")
        val broadcast = dhcp.ipAddress and dhcp.netmask or dhcp.netmask.inv()
        val quads = ByteArray(4)
        for (k in 0..3) {
            quads[k] = (broadcast shr k * 8 and 0xFF).toByte()
        }
        return InetAddress.getByAddress(quads)
    }

    fun isWifiEnabled(): Boolean {
        val cm = AABilsApp.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
        val ni = cm.activeNetworkInfo
        return ni != null && ni.isConnected && ni.type == ConnectivityManager.TYPE_WIFI
    }

    fun getIpAddress(): String {
        if (!isWifiEnabled()) {
            return ""
        }
        val wm = AABilsApp.getInstance().applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val ci = wm.connectionInfo
        return intIP2StringIP(ci.ipAddress)
    }

    fun intIP2StringIP(ip: Int): String {
        return (ip and 0xFF).toString() + "." +
                (ip shr 8 and 0xFF) + "." +
                (ip shr 16 and 0xFF) + "." +
                (ip shr 24 and 0xFF)
    }
}